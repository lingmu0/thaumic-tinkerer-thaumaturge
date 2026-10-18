package net.lingmu.thaumictinkerer.focus;

import net.lingmu.thaumictinkerer.StackData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

final class FocusEvents {
    static void beforeCast(com.leclowndu93150.thaumaturge.api.spell.event.SpellCastEvent.Pre event) {
        if(event.spell().nodes().stream().anyMatch(node->node.part().location().toString().equals("thaumictinkerer:recall"))) {
            if(event.caster() instanceof Player player&&player.getTicksUsingItem()<60)event.setCanceled(true);
            else event.setVisCost(120);
        }
    }
    static void playerTick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        if(player.tickCount%5!=0)return;
        for(int slot=0;slot<player.getInventory().getContainerSize();slot++) {
            if(!player.getInventory().getItem(slot).is(FocusContent.BRIGHT_NITOR.get()))continue;
            BlockPos pos=player.blockPosition().above();
            if(player.level().getBlockState(pos).canBeReplaced()) {
                player.level().setBlock(pos,FocusContent.NITOR_GAS.get().defaultBlockState(),3);
                player.level().scheduleTick(pos,FocusContent.NITOR_GAS.get(),20);
            }
            break;
        }
    }
    static void bloodDrops(LivingDropsEvent event) {
        if(!(event.getSource().getEntity() instanceof Player player))return;
        ItemStack sword=player.getMainHandItem();
        if(!sword.is(FocusContent.BLOOD_SWORD.get())||!StackData.enabled(sword))return;
        var aspects=MobAspects.drops(event.getEntity());
        if(aspects.isEmpty())return;
        event.getDrops().clear();
        for(String aspect:aspects)event.getDrops().add(new ItemEntity(event.getEntity().level(),event.getEntity().getX(),event.getEntity().getY(),event.getEntity().getZ(),MobAspects.stack(aspect,0)));
    }
    static void bloodAttack(LivingIncomingDamageEvent event) {
        if(!(event.getEntity().level() instanceof ServerLevel))return;
        if(event.getSource().is(net.minecraft.world.damagesource.DamageTypes.MAGIC))return;
        if(event.getEntity() instanceof Player player&&player.isUsingItem()&&player.getUseItem().is(FocusContent.BLOOD_SWORD.get())) {
            event.setCanceled(true);
            VersionFocusBridge.magicDamage(player,3);
        }
        if(event.getSource().getEntity() instanceof Player attacker&&attacker.getMainHandItem().is(FocusContent.BLOOD_SWORD.get()))VersionFocusBridge.magicDamage(attacker,2);
    }
    private FocusEvents() {}
}
