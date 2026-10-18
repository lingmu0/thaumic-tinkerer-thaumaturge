/* Adapted from Thaumic Tinkerer ModEnchantmentHandler by Vazkii and contributors.
 * CC BY-NC-SA 3.0; see LICENSE. */
package net.lingmu.thaumictinkerer.enchantment;

import net.lingmu.thaumictinkerer.StackData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

public final class TinkererEnchantments {
    public static void register() {
        NeoForge.EVENT_BUS.addListener(TinkererEnchantments::damage);
        NeoForge.EVENT_BUS.addListener(TinkererEnchantments::jump);
        NeoForge.EVENT_BUS.addListener(TinkererEnchantments::fall);
        NeoForge.EVENT_BUS.addListener(TinkererEnchantments::tick);
        NeoForge.EVENT_BUS.addListener(TinkererEnchantments::draw);
        NeoForge.EVENT_BUS.addListener(TinkererEnchantments::breakSpeed);
        NeoForge.EVENT_BUS.addListener(TinkererEnchantments::blockBroken);
        NeoForge.EVENT_BUS.addListener(TinkererEnchantments::drops);
    }

    public static int level(ItemStack stack, String id) {
        for (var entry : stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).entrySet()) {
            var key = entry.getKey().unwrapKey();
            if (key.isPresent() && key.get().location().getNamespace().equals("thaumictinkerer")
                    && key.get().location().getPath().equals(id)) return Math.min(255, Math.max(0, entry.getIntValue()));
        }
        return 0;
    }

    private static void damage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker) || event.getAmount() <= 0) return;
        ItemStack held = attacker.getMainHandItem();
        float amount = event.getAmount();
        int pounce = level(attacker.getItemBySlot(EquipmentSlot.LEGS), "pounce");
        if (pounce > 0 && !attacker.onGround()) amount *= 1 + 0.25F * pounce;
        int finalStrike = level(held,"final_strike");
        if (finalStrike > 0 && attacker.getRandom().nextInt(Math.max(1,20-finalStrike)) == 0) amount *= 3;
        int valiance = level(held,"valiance");
        if (valiance > 0 && attacker.getHealth() < attacker.getMaxHealth()*0.5F) amount *= 1+0.1F*valiance;
        int focused = level(held,"focused_strikes"), dispersed=level(held,"dispersed_strikes");
        if (focused > 0 || dispersed > 0) {
            String target=event.getEntity().getUUID().toString();
            int successive=target.equals(StackData.string(held,"tt_last_target")) ? Math.min(32,StackData.integer(held,"tt_successive_hits")+1) : 0;
            StackData.string(held,"tt_last_target",target);
            StackData.integer(held,"tt_successive_hits",successive);
            if (focused>0) amount=amount*0.5F*(1+0.5F*successive*focused);
            if (dispersed>0) amount=amount*(1+successive/5)/(1+2F*successive);
        }
        event.setAmount(amount);
        int vampirism=level(held,"vampirism");
        if (vampirism>0) attacker.heal(vampirism);
    }

    private static void jump(LivingEvent.LivingJumpEvent event) {
        var entity=event.getEntity();
        int boost=level(entity.getItemBySlot(EquipmentSlot.LEGS),"ascent_boost");
        if (boost>0 && !entity.isShiftKeyDown()) {
            var motion=entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x,motion.y*(boost+2)/2D,motion.z);
        }
    }

    private static void tick(PlayerTickEvent.Pre event) {
        Player player=event.getEntity();
        int slow=level(player.getItemBySlot(EquipmentSlot.FEET),"slow_fall");
        if (slow>0 && !player.isShiftKeyDown()) EnchantmentVersion.slowFall(player,slow);
    }

    private static void fall(LivingFallEvent event) {
        var entity=event.getEntity();
        if (!(entity.level() instanceof ServerLevel) || event.getDistance()<=3) return;
        int shock=level(entity.getItemBySlot(EquipmentSlot.FEET),"shockwave");
        if (shock>0) for (LivingEntity target : entity.level().getEntitiesOfClass(LivingEntity.class,entity.getBoundingBox().inflate(10))) {
            if (target!=entity) EnchantmentVersion.shockwave(target,(float)(0.1F*shock*event.getDistance()));
        }
    }

    private static void draw(LivingEntityUseItemEvent.Tick event) {
        if (!(event.getItem().getItem() instanceof BowItem)) return;
        int quick=level(event.getItem(),"quick_draw");
        if (quick>0 && event.getDuration()>1 && event.getEntity().getTicksUsingItem()%Math.max(1,6-quick)==0)
            event.setDuration(event.getDuration()-1);
    }

    private static void blockBroken(BlockEvent.BreakEvent event) {
        ItemStack stack=event.getPlayer().getMainHandItem();
        if (level(stack,"tunnel")>0) {
            StackData.integer(stack,"tt_tunnel_direction",Math.round(event.getPlayer().getYRot()*100));
            StackData.integer(stack,"tt_tunnel_has_direction",1);
        }
    }

    private static void breakSpeed(PlayerEvent.BreakSpeed event) {
        Player player=event.getEntity();
        ItemStack held=player.getMainHandItem();
        BlockPos position=event.getPosition().orElse(player.blockPosition());
        float hardness=event.getState().getDestroySpeed(player.level(),position);
        float speed=event.getNewSpeed();
        int shatter=level(held,"shatter");
        if (shatter>0) speed*=hardness>20 ? 3*shatter : 0.8F;
        int tunnel=level(held,"tunnel");
        if (tunnel>0 && StackData.integer(held,"tt_tunnel_has_direction")!=0) {
            float difference=Math.abs(Mth.wrapDegrees(player.getYRot()-StackData.integer(held,"tt_tunnel_direction")/100F));
            speed*=difference<50 ? 1+0.2F*tunnel : 0.3F;
        }
        int disintegrate=level(held,"disintegrate"), smelt=level(held,"auto_smelt");
        boolean disintegrates=disintegrate>0 && hardness>=0 && hardness<=1.5F;
        boolean smelts=smelt>0 && event.getState().is(BlockTags.MINEABLE_WITH_AXE);
        if (disintegrates || smelts) speed=Math.max(speed,100000F);
        else if (disintegrate>0 || smelt>0) speed=0;
        event.setNewSpeed(speed);
    }

    private static void drops(BlockDropsEvent event) {
        if (level(event.getTool(),"disintegrate")>0) {
            event.getDrops().clear(); event.setDroppedExperience(0);
        } else if (level(event.getTool(),"auto_smelt")>0) {
            java.util.List<net.minecraft.world.entity.item.ItemEntity> additional=new java.util.ArrayList<>();
            for (var item : event.getDrops()) {
                ItemStack original=item.getItem();
                ItemStack smelted=EnchantmentVersion.smelt(event.getLevel(),original);
                if (!smelted.isEmpty()) {
                    int quantity=Math.multiplyExact(smelted.getCount(),original.getCount());
                    int first=Math.min(quantity,smelted.getMaxStackSize());
                    item.setItem(smelted.copyWithCount(first));
                    for (int left=quantity-first;left>0;) {
                        int count=Math.min(left,smelted.getMaxStackSize());
                        additional.add(new net.minecraft.world.entity.item.ItemEntity(event.getLevel(),item.getX(),item.getY(),item.getZ(),smelted.copyWithCount(count)));
                        left-=count;
                    }
                }
            }
            event.getDrops().addAll(additional);
        }
    }
    private TinkererEnchantments() {}
}
