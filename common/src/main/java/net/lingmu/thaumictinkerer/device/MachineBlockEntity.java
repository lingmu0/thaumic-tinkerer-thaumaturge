/* Modern port of Thaumic Tinkerer devices. Original authors: Vazkii, pixlepix, nekosune. CC BY-NC-SA 3.0. */
package net.lingmu.thaumictinkerer.device;

import java.util.*;
import com.leclowndu93150.thaumaturge.api.aspect.*;
import com.leclowndu93150.thaumaturge.api.essentia.*;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import net.lingmu.thaumictinkerer.TTContent;
import net.lingmu.thaumictinkerer.enchantment.EnchantmentCatalog;
import net.lingmu.thaumictinkerer.focus.MobAspects;
import net.lingmu.thaumictinkerer.focus.SoulMouldItem;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;

/** Shared, server-authoritative device logic; save formats and transfer APIs live in VersionMachine/VersionDevices. */
public abstract class MachineBlockEntity extends BlockEntity implements WorldlyContainer, IEssentiaTransport {
    public final NonNullList<ItemStack> inventory = NonNullList.withSize(9, ItemStack.EMPTY);
    public BlockPos target;
    public UUID owner;
    public boolean alternate;
    public boolean adult = true;
    public boolean lastPowered;
    public int distance = 1;
    public int progress;
    public float miningProgress;
    public BlockState miningState;
    public String enchantment = "";
    public int enchantmentLevel = 1;
    public BlockState camouflage = Blocks.OAK_PLANKS.defaultBlockState();
    protected MachineBlockEntity(BlockPos pos, BlockState state) { super(DeviceContent.MACHINE.get(), pos, state); }
    public DeviceContent.Kind kind() { return ((VersionDeviceBlock)getBlockState().getBlock()).kind; }
    public Direction facing() { return getBlockState().getValue(VersionDeviceBlock.FACING); }
    public void setTarget(BlockPos pos) {
        target = pos.immutable(); setChanged();
        if (level != null) level.invalidateCapabilities(worldPosition);
    }
    public boolean validTarget() {
        return target != null && !target.equals(worldPosition) && target.closerThan(worldPosition, 33)
            && level != null && level.hasChunkAt(target) && !(level.getBlockEntity(target) instanceof MachineBlockEntity other && other.kind() == DeviceContent.Kind.INTERFACE);
    }
    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, VersionMachine machine) {
        if (!level.isClientSide) machine.serverTick((ServerLevel) level);
    }
    protected void serverTick(ServerLevel server) {
        boolean powered = server.hasNeighborSignal(worldPosition);
        boolean pulse = powered && !lastPowered;
        if (powered != lastPowered) { lastPowered = powered; setChanged(); }
        long time = server.getGameTime();
        switch (kind()) {
            case MAGNET, MOB_MAGNET -> magnet(server);
            case REPAIRER -> { if (time % 10 == 0) repair(); }
            case FUNNEL -> { if (time % 5 == 0) funnel(); }
            case DISLOCATOR -> { if (pulse) dislocate(server); }
            case REMOTE_PLACER -> { if (pulse) VersionDevices.act(this, true); }
            case TABLET -> { if (powered && time % 20 == 0) VersionDevices.act(this, !alternate); }
            case ENCHANTER -> tickEnchanter(server);
            case MOBILIZER -> { if (!powered && time % 100 == 0) mobilize(server); }
            case GOLEM_CONNECTOR -> { if (pulse && target != null) VersionDevices.commandGolems(this); }
            case SUMMON -> { if (!powered && time % 300 == 0) SummoningRecipes.summon(this, server); }
            case FORCEFIELD -> { if (++progress >= 60) server.removeBlock(worldPosition, false); }
            default -> { }
        }
    }
    public boolean interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof ConnectorItem) return false;
        owner = player.getUUID();
        if (player.isShiftKeyDown() && held.is(Items.COMPASS)) {
            Direction direction = Direction.values()[(facing().ordinal() + 1) % Direction.values().length];
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(VersionDeviceBlock.FACING, direction));
            level.invalidateCapabilities(worldPosition);
            player.displayClientMessage(Component.translatable("message.thaumictinkerer.device.facing", direction.getName()), true);
            setChanged(); return true;
        }
        if (kind() == DeviceContent.Kind.ENCHANTER) return interactEnchanter(player, held, hand);
        if (player.isShiftKeyDown()) {
            if (held.isEmpty()) {
                alternate = !alternate;
                player.displayClientMessage(Component.translatable(alternate ? "message.thaumictinkerer.device.alternate" : "message.thaumictinkerer.device.normal"), true);
            } else if (held.is(Items.STICK)) {
                distance = distance % 16 + 1;
                player.displayClientMessage(Component.translatable("message.thaumictinkerer.device.distance", distance), true);
            } else if (held.is(Items.WHEAT) && kind() == DeviceContent.Kind.MOB_MAGNET) {
                adult = !adult;
                player.displayClientMessage(Component.translatable(adult ? "message.thaumictinkerer.device.adults" : "message.thaumictinkerer.device.children"), true);
            } else if (held.getItem() instanceof BlockItem block && (kind() == DeviceContent.Kind.CAMO || kind() == DeviceContent.Kind.PLATFORM || kind() == DeviceContent.Kind.REMOTE_PLACER)) {
                camouflage = block.getBlock().defaultBlockState();
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(VersionDeviceBlock.DISGUISED, true));
                player.displayClientMessage(Component.translatable("message.thaumictinkerer.device.camouflage", held.getHoverName()), true);
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            setChanged(); return true;
        }
        if (kind() == DeviceContent.Kind.ANALYZER) {
            ItemStack sample = held.isEmpty() ? inventory.getFirst() : held;
            StringBuilder result = new StringBuilder();
            for (AspectInstance entry : AspectIndexAccess.of(sample).entries()) {
                if (!result.isEmpty()) result.append(", ");
                result.append(entry.aspect().unwrapKey().orElseThrow().location().getPath()).append(": ").append(entry.amount());
            }
            player.displayClientMessage(Component.literal(result.isEmpty() ? "—" : result.toString()), false);
            return true;
        }
        if (kind() == DeviceContent.Kind.GOLEM_CONNECTOR) {
            VersionDevices.describeGolems(this, player); return true;
        }
        if (kind() == DeviceContent.Kind.INTERFACE && validTarget()) {
            VersionDevices.openTarget(this, player); return true;
        }
        if (hasInventory()) {
            if (held.isEmpty()) {
                for (int slot = slotCount() - 1; slot >= 0; slot--) if (!inventory.get(slot).isEmpty()) {
                    player.setItemInHand(hand, inventory.get(slot)); inventory.set(slot, ItemStack.EMPTY); setChanged(); return true;
                }
            } else for (int slot = 0; slot < slotCount(); slot++) if (inventory.get(slot).isEmpty() && canPlaceItem(slot, held)) {
                inventory.set(slot, held.split(Math.min(held.getCount(), kind() == DeviceContent.Kind.REMOTE_PLACER ? 64 : 1)));
                setChanged(); return true;
            }
        }
        player.displayClientMessage(Component.translatable("message.thaumictinkerer.device.status", facing().getName(), distance,
            target == null ? "—" : target.toShortString()), true);
        return true;
    }
    private boolean hasInventory() { return switch(kind()) { case REPAIRER,FUNNEL,MOB_MAGNET,TABLET,REMOTE_PLACER,ANALYZER,SUMMON -> true; default -> false; }; }
    private int slotCount() { return kind() == DeviceContent.Kind.SUMMON ? 3 : kind() == DeviceContent.Kind.ENCHANTER ? 2 : hasInventory() ? 1 : 0; }
    private void magnet(ServerLevel server) {
        int signal = server.getBestNeighborSignal(worldPosition);
        if (signal == 0) return;
        double radius = signal / 2.0;
        Vec3 center = Vec3.atCenterOf(worldPosition);
        for (Entity entity : server.getEntities((Entity)null, new AABB(worldPosition).inflate(radius), e -> magnetAccepts(e))) {
            Vec3 delta = center.subtract(entity.position());
            if (alternate || delta.lengthSqr() > 1) {
                entity.setDeltaMovement(delta.normalize().scale(alternate ? -0.25 : 0.25));
                entity.hasImpulse = true;
            }
        }
    }
    private boolean magnetAccepts(Entity entity) {
        if (kind() == DeviceContent.Kind.MAGNET) return entity instanceof ItemEntity;
        if (!(entity instanceof LivingEntity) || entity instanceof Player) return false;
        if (entity instanceof AgeableMob ageable && adult == ageable.isBaby()) return false;
        var pattern = SoulMouldItem.pattern(inventory.getFirst());
        return pattern == null || pattern.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }
    private void repair() {
        ItemStack item = inventory.getFirst();
        if (!item.isDamaged()) return;
        for (String[] choice : new String[][]{{"instrumentum","8"},{"fabrico","5"},{"ordo","3"}}) {
            Holder<IAspect> aspect = aspect(choice[0]);
            if (aspect != null && draw(aspect)) {
                item.setDamageValue(Math.max(0, item.getDamageValue() - Integer.parseInt(choice[1]))); setChanged(); return;
            }
        }
    }
    public Holder<IAspect> aspect(String path) {
        return level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(ResourceKey.create(IAspect.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("thaumaturge", path))).orElse(null);
    }
    private boolean draw(Holder<IAspect> aspect) {
        Direction side = facing();
        BlockPos source = worldPosition.relative(side);
        var transport = level.getCapability(EssentiaCapabilities.TRANSPORT, source, side.getOpposite());
        if (transport != null) return transport.isConnectable(side.getOpposite()) && transport.canOutputTo(side.getOpposite())
            && transport.getSuctionAmount(side.getOpposite()) < 128 && transport.getMinimumSuction() <= 128
            && transport.takeEssentia(aspect, 1, side.getOpposite()) == 1;
        var container = level.getCapability(AspectCapabilities.CONTAINER, source, side.getOpposite());
        return container != null && container.drain(aspect, 1);
    }
    private void funnel() {
        ItemStack jar = inventory.getFirst();
        IItemEssentia carried = jar.getCapability(EssentiaCapabilities.CONTAINER);
        if (carried == null || carried.getAspects().isEmpty()) return;
        BlockPos destination = worldPosition.below();
        BlockState below = level.getBlockState(destination);
        if (below.getBlock() instanceof HopperBlock) destination = destination.relative(below.getValue(HopperBlock.FACING));
        IAspectContainer container = level.getCapability(AspectCapabilities.CONTAINER, destination, Direction.UP);
        if (container == null) return;
        AspectInstance entry = carried.getAspects().entries().getFirst();
        if (container.accepts(entry.aspect()) && container.fill(entry.aspect(), 1) == 0) {
            carried.setAspects(carried.getAspects().reduce(entry.aspect(), 1)); setChanged();
        }
    }
    private void dislocate(ServerLevel server) {
        if (target == null || target.equals(worldPosition) || !target.closerThan(worldPosition, 33) || !server.hasChunkAt(target)) return;
        BlockPos destination = worldPosition.relative(facing(), distance);
        if (destination.equals(target) || destination.equals(worldPosition) || !server.hasChunkAt(destination)) return;
        if (alternate) {
            VersionDevices.swapBlocks(this, target, destination);
        } else {
            Vec3 a = Vec3.atBottomCenterOf(target), b = Vec3.atBottomCenterOf(destination);
            var first = List.copyOf(server.getEntities((Entity)null, new AABB(target), Entity::isAlive));
            var second = List.copyOf(server.getEntities((Entity)null, new AABB(destination), Entity::isAlive));
            for (Entity entity : first) VersionDevices.teleport(entity, entity.position().add(b.subtract(a)));
            for (Entity entity : second) VersionDevices.teleport(entity, entity.position().add(a.subtract(b)));
        }
    }
    private void mobilize(ServerLevel server) {
        if (target == null || !target.closerThan(worldPosition, 33) || !server.hasChunkAt(target)
            || !server.getBlockState(target).is(DeviceContent.MACHINES.get(DeviceContent.Kind.RELAY).get())) return;
        Direction direction = alternate ? facing().getOpposite() : facing();
        if (direction.getAxis().isVertical()) return;
        BlockPos next = worldPosition.relative(direction);
        if (!server.isEmptyBlock(next) || !server.isEmptyBlock(next.above())) { alternate = !alternate; setChanged(); return; }
        VersionDevices.moveMobilizer(this, next);
    }
    private void repel(ServerLevel server) {
        double radius = Math.max(2, server.getBestNeighborSignal(worldPosition));
        Vec3 center = Vec3.atCenterOf(worldPosition);
        for (LivingEntity entity : server.getEntitiesOfClass(LivingEntity.class, new AABB(worldPosition).inflate(radius), e -> !(e instanceof Player))) {
            Vec3 delta = entity.position().subtract(center);
            if (delta.lengthSqr() < radius * radius) { entity.setDeltaMovement(delta.normalize().scale(.35)); entity.hasImpulse = true; }
        }
    }
    private List<Holder.Reference<Enchantment>> enchantmentOptions() {
        ItemStack tool = inventory.getFirst();
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElements()
            .filter(holder -> EnchantmentCatalog.supports(holder.key().location()) && holder.value().canEnchant(tool))
            .filter(holder -> compatible(tool, holder)).sorted(Comparator.comparing(h -> h.key().location().toString())).toList();
    }
    private static boolean compatible(ItemStack tool, Holder<Enchantment> candidate) {
        for (var existing : tool.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).keySet())
            if (!existing.equals(candidate) && !Enchantment.areCompatible(existing, candidate)) return false;
        return true;
    }
    private boolean interactEnchanter(Player player, ItemStack held, InteractionHand hand) {
        if (progress > 0) { player.displayClientMessage(Component.translatable("message.thaumictinkerer.device.enchanting", progress), true); return true; }
        if (player.isShiftKeyDown()) {
            if (held.is(Items.STICK)) {
                var selected = selectedEnchantment();
                enchantmentLevel = selected == null ? 1 : enchantmentLevel % selected.value().getMaxLevel() + 1;
            } else {
                var options = enchantmentOptions();
                if (!options.isEmpty()) {
                    int index = -1;
                    for (int i = 0; i < options.size(); i++) if (options.get(i).key().location().toString().equals(enchantment)) index = i;
                    enchantment = options.get((index + 1) % options.size()).key().location().toString(); enchantmentLevel = 1;
                }
            }
            describeEnchant(player); setChanged(); return true;
        }
        if (held.getItem() instanceof ItemWand) { startEnchanter(player, held); return true; }
        if (held.is(Items.ENCHANTED_BOOK)) {
            for (var entry : held.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).entrySet()) {
                ResourceLocation id = entry.getKey().unwrapKey().orElseThrow().location();
                if (EnchantmentCatalog.supports(id)) { enchantment = id.toString(); enchantmentLevel = entry.getIntValue(); break; }
            }
            describeEnchant(player); setChanged(); return true;
        }
        if (held.isEmpty()) { player.setItemInHand(hand, inventory.getFirst()); inventory.set(0, ItemStack.EMPTY); }
        else if (inventory.getFirst().isEmpty()) { inventory.set(0, held.split(1)); var choices = enchantmentOptions(); if (!choices.isEmpty()) enchantment = choices.getFirst().key().location().toString(); }
        describeEnchant(player); setChanged(); return true;
    }
    private Holder.Reference<Enchantment> selectedEnchantment() {
        ResourceLocation id = ResourceLocation.tryParse(enchantment);
        return id == null ? null : level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(ResourceKey.create(Registries.ENCHANTMENT, id)).orElse(null);
    }
    private void describeEnchant(Player player) {
        var selected = selectedEnchantment();
        if (selected != null) player.displayClientMessage(Component.translatable("message.thaumictinkerer.device.selected_enchant", Enchantment.getFullname(selected, enchantmentLevel), EnchantmentCatalog.cost(selected.key().location(), enchantmentLevel).toString()), false);
    }
    private void startEnchanter(Player player, ItemStack wand) {
        var selected = selectedEnchantment();
        if (selected == null || inventory.getFirst().isEmpty() || !selected.value().canEnchant(inventory.getFirst()) || !compatible(inventory.getFirst(), selected)) return;
        enchantmentLevel = Math.max(1, Math.min(enchantmentLevel, selected.value().getMaxLevel()));
        if (EnchantmentHelper.getItemEnchantmentLevel(selected, inventory.getFirst()) >= enchantmentLevel) return;
        ResourceLocation research = EnchantmentCatalog.research(selected.key().location());
        if (research != null && !KnowledgeAccess.of(player).isResearchComplete(research)) {
            player.displayClientMessage(Component.translatable("message.thaumictinkerer.device.research"), true); return;
        }
        if (!hasPillars()) { player.displayClientMessage(Component.translatable("message.thaumictinkerer.device.pillars"), true); return; }
        Map<ResourceKey<IAspect>, Integer> cost = new LinkedHashMap<>();
        EnchantmentCatalog.cost(selected.key().location(), enchantmentLevel).forEach((path, amount) ->
            cost.put(ResourceKey.create(IAspect.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("thaumaturge", path)), amount * 100));
        if (!VersionDevices.payWand(wand, player, cost)) {
            player.displayClientMessage(Component.translatable("message.thaumictinkerer.device.vis"), true); return;
        }
        progress = 100; setChanged();
    }
    private boolean hasPillars() {
        int found = 0;
        for (int x = -4; x <= 4; x++) for (int z = -4; z <= 4; z++) {
            int height = 0;
            BlockPos pos = worldPosition.offset(x, 0, z);
            while (height < 12 && isPillar(level.getBlockState(pos.above(height)))) height++;
            if (height >= 2) {
                ResourceLocation cap = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos.above(height)).getBlock());
                if (cap.getNamespace().equals("thaumaturge") && cap.getPath().contains("nitor") && ++found >= 6) return true;
            }
        }
        return false;
    }
    private boolean isPillar(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return state.is(Blocks.OBSIDIAN) || id.getNamespace().equals("thaumaturge") && (id.getPath().contains("stone_arcane") || id.getPath().contains("pillar_arcane"));
    }
    private void tickEnchanter(ServerLevel server) {
        if (progress <= 0) return;
        if (inventory.getFirst().isEmpty() || !hasPillars()) { progress = 0; setChanged(); return; }
        if (--progress == 0) {
            var selected = selectedEnchantment();
            if (selected != null && compatible(inventory.getFirst(), selected)) EnchantmentHelper.updateEnchantments(inventory.getFirst(), mutable -> mutable.upgrade(selected, enchantmentLevel));
        }
        setChanged();
    }
    public Container targetInventory() { return validTarget() && level.getBlockEntity(target) instanceof Container container ? container : null; }
    @Override public int getContainerSize() { Container remote = kind() == DeviceContent.Kind.INTERFACE ? targetInventory() : null; return remote != null ? remote.getContainerSize() : slotCount(); }
    @Override public boolean isEmpty() { for (int i=0;i<getContainerSize();i++) if (!getItem(i).isEmpty()) return false; return true; }
    @Override public ItemStack getItem(int slot) { Container remote = kind() == DeviceContent.Kind.INTERFACE ? targetInventory() : null; return remote != null ? remote.getItem(slot) : slot >= 0 && slot < slotCount() ? inventory.get(slot) : ItemStack.EMPTY; }
    @Override public ItemStack removeItem(int slot, int count) { Container remote = kind() == DeviceContent.Kind.INTERFACE ? targetInventory() : null; if(remote != null)return remote.removeItem(slot,count); ItemStack result = ContainerHelper.removeItem(inventory,slot,count); setChanged();return result; }
    @Override public ItemStack removeItemNoUpdate(int slot) { Container remote = kind() == DeviceContent.Kind.INTERFACE ? targetInventory() : null; return remote != null ? remote.removeItemNoUpdate(slot) : ContainerHelper.takeItem(inventory,slot); }
    @Override public void setItem(int slot, ItemStack stack) { Container remote = kind() == DeviceContent.Kind.INTERFACE ? targetInventory() : null; if(remote != null)remote.setItem(slot,stack);else inventory.set(slot,stack);setChanged(); }
    @Override public boolean stillValid(Player player) { return level != null && level.getBlockEntity(worldPosition) == this && player.distanceToSqr(Vec3.atCenterOf(worldPosition)) <= 64; }
    @Override public void clearContent() { if(kind()==DeviceContent.Kind.INTERFACE){Container remote=targetInventory();if(remote!=null)remote.clearContent();}else inventory.replaceAll(s -> ItemStack.EMPTY);setChanged(); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { Container remote=kind()==DeviceContent.Kind.INTERFACE?targetInventory():null;return remote!=null?remote.canPlaceItem(slot,stack):kind() != DeviceContent.Kind.ENCHANTER && (kind() != DeviceContent.Kind.REPAIRER || stack.isDamageableItem()); }
    @Override public int getMaxStackSize() { return kind()==DeviceContent.Kind.REMOTE_PLACER || kind()==DeviceContent.Kind.TABLET || kind()==DeviceContent.Kind.ANALYZER ? 64 : 1; }
    @Override public int[] getSlotsForFace(Direction side) {
        if(kind()==DeviceContent.Kind.INTERFACE && targetInventory() instanceof WorldlyContainer remote)return remote.getSlotsForFace(side);
        if(kind()==DeviceContent.Kind.ENCHANTER || kind()==DeviceContent.Kind.FUNNEL && side==Direction.DOWN)return new int[0];
        return java.util.stream.IntStream.range(0,getContainerSize()).toArray();
    }
    @Override public boolean canPlaceItemThroughFace(int slot,ItemStack stack,Direction side) { if(kind()==DeviceContent.Kind.INTERFACE && targetInventory() instanceof WorldlyContainer remote)return remote.canPlaceItemThroughFace(slot,stack,side);return kind()!=DeviceContent.Kind.ENCHANTER && !(kind()==DeviceContent.Kind.FUNNEL && side==Direction.DOWN) && canPlaceItem(slot,stack); }
    @Override public boolean canTakeItemThroughFace(int slot,ItemStack stack,Direction side) { if(kind()==DeviceContent.Kind.INTERFACE && targetInventory() instanceof WorldlyContainer remote)return remote.canTakeItemThroughFace(slot,stack,side);return kind()!=DeviceContent.Kind.ENCHANTER && !(kind()==DeviceContent.Kind.FUNNEL && side==Direction.DOWN); }
    // The interface delegates the target's essentia contract; repairers generate strong directional suction.
    private IEssentiaTransport remoteEssentia() { return kind()==DeviceContent.Kind.INTERFACE && validTarget() ? level.getCapability(EssentiaCapabilities.TRANSPORT,target,facing().getOpposite()) : null; }
    @Override public boolean isConnectable(Direction side) { var remote=remoteEssentia();return remote!=null?remote.isConnectable(side):kind()==DeviceContent.Kind.REPAIRER && side==facing(); }
    private boolean wantsRepair() { return kind()==DeviceContent.Kind.REPAIRER && inventory.getFirst().isDamaged(); }
    @Override public boolean canInputFrom(Direction side) { var remote=remoteEssentia();return remote!=null?remote.canInputFrom(side):wantsRepair() && side==facing(); }
    @Override public boolean canOutputTo(Direction side) { var remote=remoteEssentia();return remote!=null && remote.canOutputTo(side); }
    @Override public void setSuction(Holder<IAspect> aspect,int amount) { var remote=remoteEssentia();if(remote!=null)remote.setSuction(aspect,amount); }
    // Untyped suction lets the supply network deliver any of the three supported repair aspects.
    // Like Thaumaturge's EssentiaIntake, this terminal pulls directly and has no externally fillable buffer.
    @Override public Holder<IAspect> getSuctionType(Direction side) { var remote=remoteEssentia();return remote!=null?remote.getSuctionType(side):null; }
    @Override public int getSuctionAmount(Direction side) { var remote=remoteEssentia();return remote!=null?remote.getSuctionAmount(side):wantsRepair() && (side==null || side==facing())?128:0; }
    @Override public int getMinimumSuction() { var remote=remoteEssentia();return remote!=null?remote.getMinimumSuction():0; }
    @Override public Holder<IAspect> getEssentiaType(Direction side) { var remote=remoteEssentia();return remote!=null?remote.getEssentiaType(side):null; }
    @Override public int getEssentiaAmount(Direction side) { var remote=remoteEssentia();return remote!=null?remote.getEssentiaAmount(side):0; }
    @Override public int takeEssentia(Holder<IAspect> aspect,int amount,Direction side) { var remote=remoteEssentia();return remote!=null?remote.takeEssentia(aspect,amount,side):0; }
    @Override public int addEssentia(Holder<IAspect> aspect,int amount,Direction side) { var remote=remoteEssentia();return remote!=null?remote.addEssentia(aspect,amount,side):0; }
    @Override public int spaceFor(Holder<IAspect> aspect,Direction side) { var remote=remoteEssentia();return remote!=null?remote.spaceFor(aspect,side):0; }
}
