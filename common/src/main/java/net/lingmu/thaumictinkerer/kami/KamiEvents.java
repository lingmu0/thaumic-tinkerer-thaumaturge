/* Gameplay port of the original KAMI systems, CC BY-NC-SA 3.0. */
package net.lingmu.thaumictinkerer.kami;

import java.util.*;
import net.lingmu.thaumictinkerer.StackData;
import net.lingmu.thaumictinkerer.TTContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class KamiEvents {
    @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof KamiToolItem tool) tool.addTooltip(stack, event.getToolTip());
        if (stack.getItem() instanceof KamiPearl pearl) pearl.addTooltip(stack, event.getToolTip());
        if (stack.getItem() instanceof KamiUtilityItem utility) utility.addTooltip(stack, event.getToolTip());
    }
    private static final Set<UUID> MINING = new HashSet<>();
    private static final Set<UUID> ATTACKING = new HashSet<>();
    private static final Set<UUID> FLIGHT_GRANTED = new HashSet<>();
    private static final Set<UUID> PORTAL_VISITORS = new HashSet<>();
    private static final Map<UUID, Long> FELINE_WARD = new HashMap<>();
    private static final ResourceKey<Level> BEDROCK = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, TTContent.id("bedrock"));

    static boolean active(Player player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        return stack.getItem() instanceof KamiArmorItem armor && armor.awakened && StackData.integer(stack, "tt_disabled") == 0;
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        boolean portal = player.level().getBlockState(player.blockPosition()).is(KamiContent.BEDROCK_PORTAL.get()) || player.level().getBlockState(player.blockPosition().below()).is(KamiContent.BEDROCK_PORTAL.get());
        if (portal) { if (PORTAL_VISITORS.add(player.getUUID())) bedrockTravel(player, player.blockPosition().below()); }
        else PORTAL_VISITORS.remove(player.getUUID());
        boolean chest = active(player, EquipmentSlot.CHEST);
        if (chest && !player.getAbilities().mayfly) {
            FLIGHT_GRANTED.add(player.getUUID()); player.getAbilities().mayfly = true; player.onUpdateAbilities();
        } else if (!chest && FLIGHT_GRANTED.remove(player.getUUID()) && !player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false; player.getAbilities().flying = false; player.onUpdateAbilities();
        }
        if (active(player, EquipmentSlot.HEAD)) {
            if (player.isUnderWater()) { player.setAirSupply(player.getMaxAirSupply()); player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, true, false)); }
            int food = player.getFoodData().getFoodLevel();
            if (food > 0 && food < 18 && player.tickCount % 80 == 0) player.heal(1F);
        }
        if (chest && player.tickCount % 2 == 0) {
            for (Projectile projectile : player.level().getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(3), p -> p.getOwner() != player)) {
                Vec3 away = projectile.position().subtract(player.position()).normalize();
                if (projectile.getDeltaMovement().dot(away) < 0) { projectile.setDeltaMovement(away.scale(Math.max(.5, projectile.getDeltaMovement().length()))); projectile.setOwner(player); projectile.hasImpulse = true; }
            }
        }
        if (active(player, EquipmentSlot.FEET)) {
            player.fallDistance = 0;
            if (player.tickCount % 10 == 0) player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20, 1, true, false));
            modifier(player, Attributes.MOVEMENT_SPEED, "kami_boots_speed", .15, true);
            modifier(player, Attributes.STEP_HEIGHT, "kami_boots_step", player.isShiftKeyDown() ? 0 : .4, true);
            modifier(player, Attributes.JUMP_STRENGTH, "kami_boots_jump", .3, true);
            BlockPos floor = player.blockPosition().below();
            if (player.level().getBlockState(floor).is(Blocks.DIRT) && player.level().mayInteract(player, floor)) player.level().setBlockAndUpdate(floor, Blocks.GRASS_BLOCK.defaultBlockState());
        } else {
            modifier(player, Attributes.MOVEMENT_SPEED, "kami_boots_speed", 0, false);
            modifier(player, Attributes.STEP_HEIGHT, "kami_boots_step", 0, false);
            modifier(player, Attributes.JUMP_STRENGTH, "kami_boots_jump", 0, false);
        }
        if (player.tickCount % 5 == 0) {
            if (active(player, EquipmentSlot.LEGS)) light(player);
            for (int slot = 0; slot < 36; slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (stack.is(KamiContent.PROTOCLAY.get())) smartTools(player);
            }
        }
    }
    private static void modifier(Player player, net.minecraft.core.Holder<Attribute> attribute, String name, double amount, boolean enabled) {
        AttributeInstance instance = player.getAttribute(attribute); if (instance == null) return;
        var id = TTContent.id(name); AttributeModifier current = instance.getModifier(id);
        if (!enabled || amount == 0) { if (current != null) instance.removeModifier(id); return; }
        if (current == null || current.amount() != amount) {
            if (current != null) instance.removeModifier(id);
            instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }
    private static void light(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        for (int step = 0; step <= 5; step++) {
            BlockPos pos = BlockPos.containing(player.getX() + look.x * step, player.getY() + 1, player.getZ() + look.z * step);
            if (player.level().isEmptyBlock(pos)) { player.level().setBlock(pos, KamiContent.KAMI_LIGHT.get().defaultBlockState(), 3); player.level().scheduleTick(pos, KamiContent.KAMI_LIGHT.get(), 20); }
        }
    }
    private static void smartTools(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof KamiToolItem tool) || !tool.awakened || tool.kind == KamiToolItem.Kind.SWORD) return;
        if (!(player.pick(5, 0F, false) instanceof BlockHitResult hit)) return;
        BlockState state = player.level().getBlockState(hit.getBlockPos()); if (tool.effective(state)) return;
        for (int i = 0; i < 36; i++) {
            ItemStack candidate = player.getInventory().getItem(i);
            if (candidate.getItem() instanceof KamiToolItem other && other.awakened && other.kind != KamiToolItem.Kind.SWORD && other.effective(state)) {
                int selected = KamiVersion.selected(player); player.getInventory().setItem(selected, candidate); player.getInventory().setItem(i, held); player.containerMenu.broadcastChanges(); return;
            }
        }
    }
    static void repelMobs(ServerPlayer player) {
        FELINE_WARD.put(player.getUUID(), player.level().getGameTime() + 1);
        if (player.tickCount % 5 != 0) return;
        for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(10, 4, 10))) {
            if (mob.getTarget() == player) mob.setTarget(null);
            if (mob instanceof Creeper creeper) { creeper.setSwellDir(-1); Vec3 away = mob.position().subtract(player.position()).normalize().scale(8); mob.getNavigation().moveTo(mob.getX() + away.x, mob.getY(), mob.getZ() + away.z, 1.2); }
        }
    }
    @SubscribeEvent public static void target(LivingChangeTargetEvent event) {
        if (KamiVersion.newTarget(event) instanceof ServerPlayer player && Math.abs(event.getEntity().getY() - player.getY()) <= 4 && event.getEntity().distanceToSqr(player) <= 100 && FELINE_WARD.getOrDefault(player.getUUID(), Long.MIN_VALUE) >= player.level().getGameTime()) event.setCanceled(true);
    }
    @SubscribeEvent public static void incoming(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && active(player, EquipmentSlot.LEGS) && event.getSource().is(DamageTypeTags.IS_FIRE)) {
            player.heal(event.getAmount()); event.setCanceled(true);
        }
    }
    @SubscribeEvent public static void fall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && active(player, EquipmentSlot.FEET)) event.setCanceled(true);
    }
    @SubscribeEvent public static void attack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer) || ATTACKING.contains(player.getUUID()) || !(event.getTarget() instanceof LivingEntity living) || living.hurtTime > 0 || !living.isAlive()) return;
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof KamiToolItem tool) || !tool.awakened || tool.kind != KamiToolItem.Kind.SWORD) return;
        int mode = StackData.integer(stack, "tt_mode");
        if (mode == 1) {
            ATTACKING.add(player.getUUID());
            try { for (LivingEntity other : player.level().getEntitiesOfClass(LivingEntity.class, living.getBoundingBox().inflate(3), e -> e != player && e != living && e.getClass() == living.getClass())) player.attack(other); }
            finally { ATTACKING.remove(player.getUUID()); }
        } else if (mode == 2) { modifier(player, Attributes.MAX_ABSORPTION, "kami_soul_hearts", 20, true); player.setAbsorptionAmount(Math.min(20F, player.getAbsorptionAmount() + 1F)); living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1, 1)); }
    }
    @SubscribeEvent public static void leftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !event.getLevel().getBlockState(event.getPos()).is(Blocks.BEDROCK)) return;
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof KamiToolItem tool) || tool.kind != KamiToolItem.Kind.PICK || !tool.awakened) return;
        BlockPos pos = event.getPos(); ServerLevel level = KamiVersion.level(player);
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, event.getFace(), held)) return;
        if (net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, level.getBlockState(pos), player)).isCanceled()) return;
        if (level.dimension() == Level.OVERWORLD && pos.getY() <= KamiVersion.minY(level) + 5 || level.dimension() == BEDROCK && pos.getY() >= 253) {
            level.setBlockAndUpdate(pos, KamiContent.BEDROCK_PORTAL.get().defaultBlockState()); event.setCanceled(true);
        } else if (level.dimension() == BEDROCK) {
            MINING.add(player.getUUID());
            try {
                int mode = StackData.integer(held, "tt_mode");
                Direction face = event.getFace();
                if (mode == 1) {
                    for (int a = -2; a <= 2; a++) for (int b = -1; b <= 3; b++) {
                        BlockPos target = switch(face.getAxis()) { case X -> pos.offset(0, b, a); case Y -> pos.offset(a, 0, b); case Z -> pos.offset(a, b, 0); };
                        mineBedrock(player, tool, pos, target);
                    }
                } else if (mode == 2) for (int i = 0; i < 10; i++) mineBedrock(player, tool, pos, pos.relative(face.getOpposite(), i));
                else level.removeBlock(pos, false);
            } finally { MINING.remove(player.getUUID()); }
            event.setCanceled(true);
        }
    }
    private static void mineBedrock(ServerPlayer player, KamiToolItem tool, BlockPos origin, BlockPos pos) {
        ServerLevel level = KamiVersion.level(player);
        if (pos.getY() >= 253 || !level.hasChunkAt(pos) || !level.mayInteract(player, pos) || !player.mayUseItemAt(pos, Direction.UP, player.getMainHandItem())) return;
        if (level.getBlockState(pos).is(Blocks.BEDROCK)) {
            if (pos.equals(origin) || !net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, level.getBlockState(pos), player)).isCanceled()) level.removeBlock(pos, false);
        } else mine(player, tool, origin, pos);
    }
    @SubscribeEvent public static void breakBlock(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || MINING.contains(player.getUUID())) return;
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof KamiToolItem tool) || !tool.awakened || !tool.effective(event.getState()) || tool.kind == KamiToolItem.Kind.SWORD) return;
        int mode = StackData.integer(stack, "tt_mode"); if (mode == 0) return;
        MINING.add(player.getUUID());
        try {
            ServerLevel level = KamiVersion.level(player); BlockPos origin = event.getPos();
            Direction face = player.pick(8, 0, false) instanceof BlockHitResult hit ? hit.getDirection() : Direction.UP;
            if (mode == 1) {
                for (int a = -2; a <= 2; a++) for (int b = -1; b <= 3; b++) {
                    BlockPos pos = switch(face.getAxis()) { case X -> origin.offset(0, b, a); case Y -> origin.offset(a, 0, b); case Z -> origin.offset(a, b, 0); };
                    mine(player, tool, origin, pos);
                }
            } else if (tool.kind == KamiToolItem.Kind.PICK) {
                for (int i = 1; i < 10; i++) mine(player, tool, origin, origin.relative(face.getOpposite(), i));
            } else if (tool.kind == KamiToolItem.Kind.SHOVEL) {
                for (int i = -8; i < 8; i++) { BlockPos pos = origin.offset(0, i, 0); if (level.getBlockState(pos).is(event.getState().getBlock())) mine(player, tool, origin, pos); }
            } else if (tool.kind == KamiToolItem.Kind.AXE && event.getState().is(BlockTags.LOGS)) {
                ArrayDeque<BlockPos> pending = new ArrayDeque<>(); Set<BlockPos> visited = new HashSet<>(); pending.add(origin); int count = 0;
                while (!pending.isEmpty() && count < 1024) {
                    BlockPos pos = pending.removeFirst(); if (!visited.add(pos) || Math.abs(pos.getY() - origin.getY()) > 64 || Math.abs(pos.getX() - origin.getX()) > 16 || Math.abs(pos.getZ() - origin.getZ()) > 16 || !level.getBlockState(pos).is(BlockTags.LOGS)) continue;
                    if (!pos.equals(origin)) { mine(player, tool, origin, pos); count++; }
                    for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++) if (dx != 0 || dy != 0 || dz != 0) pending.add(pos.offset(dx, dy, dz));
                }
                for (net.minecraft.world.entity.item.ItemEntity drop : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(origin).inflate(5, 64, 5), e -> e.tickCount < 10)) {
                    drop.setPos(origin.getX() + .5, origin.getY() + .5, origin.getZ() + .5); drop.setNoPickUpDelay(); drop.hasImpulse = true;
                }
            }
        } finally { MINING.remove(player.getUUID()); }
    }
    private static void mine(ServerPlayer player, KamiToolItem tool, BlockPos origin, BlockPos pos) {
        ServerLevel level = KamiVersion.level(player); BlockState state = level.getBlockState(pos);
        if (!pos.equals(origin) && level.hasChunkAt(pos) && state.getDestroySpeed(level, pos) >= 0 && tool.effective(state) && level.mayInteract(player, pos) && !state.hasBlockEntity()) player.gameMode.destroyBlock(pos);
    }
    @SubscribeEvent public static void gateInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) return;
        ServerLevel level = KamiVersion.level(player); BlockPos pos = event.getPos();
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, event.getFace(), player.getMainHandItem())) return;
        if (level.getBlockState(pos).is(KamiContent.BEDROCK_PORTAL.get())) { bedrockTravel(player, pos); event.setCanceled(true); return; }
        if (!(level.getBlockEntity(pos) instanceof KamiGateBase gate)) return;
        ItemStack held = player.getMainHandItem();
        if (held.is(KamiContent.SKY_PEARL.get())) {
            if (!KamiPearl.bound(held)) { KamiPearl.bind(held, level, pos); }
            else if (player.isShiftKeyDown()) {
                for (int i = 0; i < 10; i++) if (gate.pearls.get(i).isEmpty()) { gate.pearls.set(i, held.copyWithCount(1)); held.shrink(1); gate.setChanged(); break; }
            }
        } else if (player.isShiftKeyDown()) {
            if (held.is(net.minecraft.world.item.Items.REDSTONE)) { gate.locked = !gate.locked; KamiVersion.status(player, Component.translatable("message.thaumictinkerer.gate_locked", gate.locked)); }
            else { gate.selected = (gate.selected + 1) % 10; KamiVersion.status(player, Component.translatable("message.thaumictinkerer.gate_destination", gate.selected + 1)); }
            gate.setChanged();
        } else {
            boolean ok = KamiPearl.teleport(player, gate.pearls.get(gate.selected));
            if (!ok) KamiVersion.status(player, Component.translatable("message.thaumictinkerer.gate_unavailable"));
        }
        event.setCanceled(true);
    }
    private static void bedrockTravel(ServerPlayer player, BlockPos source) {
        ServerLevel current = KamiVersion.level(player);
        boolean entering = current.dimension() != BEDROCK;
        ServerLevel destination = current.getServer().getLevel(entering ? BEDROCK : Level.OVERWORLD);
        if (destination == null) return;
        PORTAL_VISITORS.add(player.getUUID());
        int y = entering ? 252 : KamiVersion.minY(destination) + 3;
        BlockPos gate = new BlockPos(source.getX(), y, source.getZ());
        destination.setBlockAndUpdate(gate, KamiContent.BEDROCK_PORTAL.get().defaultBlockState());
        destination.setBlockAndUpdate(gate.above(), Blocks.AIR.defaultBlockState()); destination.setBlockAndUpdate(gate.above(2), Blocks.AIR.defaultBlockState());
        KamiVersion.teleport(player, destination, gate.above());
    }
    @SubscribeEvent public static void gateDrops(net.neoforged.neoforge.event.level.BlockDropsEvent event) {
        if (event.getBlockEntity() instanceof KamiGateBase gate) for (ItemStack pearl : gate.pearls) if (!pearl.isEmpty()) event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(event.getLevel(), event.getPos().getX() + .5, event.getPos().getY() + .5, event.getPos().getZ() + .5, pearl.copy()));
    }
    private KamiEvents() {}
}
