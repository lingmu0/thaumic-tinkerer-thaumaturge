/* Original elemental-imbuement effects: pixlepix and the Thaumic Tinkerer team, CC BY-NC-SA 3.0. */
package net.lingmu.thaumictinkerer.focus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.lingmu.thaumictinkerer.TTContent;
import net.lingmu.thaumictinkerer.device.DeviceContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TinkerPotions {
    private static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(Registries.MOB_EFFECT,"thaumictinkerer");
    public static final Map<String,DeferredHolder<MobEffect,MobEffect>> IMBUEMENTS=new LinkedHashMap<>();
    private static final Map<Entity,Long> AIR_HITS=new WeakHashMap<>(),FIRE_HITS=new WeakHashMap<>();
    public static void register(IEventBus bus) {
        for(String aspect:java.util.List.of("aer","ignis","terra","aqua")) {
            int color=switch(aspect){case "aer"->0xffff7e;case "ignis"->0xff5a01;case "terra"->0x56c000;default->0x3cd4fc;};
            IMBUEMENTS.put(aspect,EFFECTS.register("imbued_"+aspect,()->new ElementalEffect(color)));
            TTContent.item("infused_potion_"+aspect,p->new InfusedPotionItem(p,aspect),new Item.Properties().stacksTo(1));
        }
        EFFECTS.register(bus);
        NeoForge.EVENT_BUS.addListener(TinkerPotions::onAttack);
        NeoForge.EVENT_BUS.addListener(TinkerPotions::entityTick);
        NeoForge.EVENT_BUS.addListener(TinkerPotions::playerTick);
    }
    public static void apply(LivingEntity entity,String aspect) { entity.addEffect(new MobEffectInstance(IMBUEMENTS.get(aspect),3600)); }
    private static void onAttack(LivingIncomingDamageEvent event) {
        if(event.isCanceled()||!(event.getEntity().level() instanceof ServerLevel level)||!(event.getSource().getEntity() instanceof Player player))return;
        if(player.hasEffect(IMBUEMENTS.get("aer")))AIR_HITS.put(event.getEntity(),level.getGameTime()+20);
        if(player.hasEffect(IMBUEMENTS.get("ignis")))FIRE_HITS.put(event.getEntity(),level.getGameTime()+6000);
        if(player.hasEffect(IMBUEMENTS.get("terra"))) {
            Entity target=event.getEntity();
            boolean xAxis=Math.abs(target.getZ()-player.getZ())<Math.abs(target.getX()-player.getX());
            BlockPos center=BlockPos.containing((target.getX()+player.getX())/2,player.getY()+2,(target.getZ()+player.getZ())/2);
            for(int y=-2;y<=2;y++)for(int side=-2;side<=2;side++) {
                BlockPos pos=center.offset(xAxis?0:side,y,xAxis?side:0);
                if(!level.hasChunkAt(pos)||!level.getBlockState(pos).isAir()||!level.mayInteract(player,pos))continue;
                if(NeoForge.EVENT_BUS.post(new BlockEvent.EntityPlaceEvent(net.neoforged.neoforge.common.util.BlockSnapshot.create(level.dimension(),level,pos),level.getBlockState(pos.below()),player)).isCanceled())continue;
                level.setBlock(pos,DeviceContent.MACHINES.get(DeviceContent.Kind.FORCEFIELD).get().defaultBlockState(),3);
            }
        }
    }
    private static void entityTick(ServerTickEvent.Post event) {
        java.util.Set<Entity> marked=new java.util.HashSet<>(AIR_HITS.keySet());marked.addAll(FIRE_HITS.keySet());
        for(Entity entity:marked) {
            if(!(entity.level() instanceof ServerLevel level)||entity.tickCount%5!=0)continue;
            Long air=AIR_HITS.get(entity),fire=FIRE_HITS.get(entity);
            if(air!=null) {
                if(!entity.isAlive()||air<level.getGameTime())AIR_HITS.remove(entity);
                else {entity.setDeltaMovement(level.getRandom().nextDouble()-0.5,level.getRandom().nextDouble(),level.getRandom().nextDouble()-0.5);entity.hurtMarked=true;}
            }
            if(fire!=null) {
                if(!entity.isAlive()||fire<level.getGameTime())FIRE_HITS.remove(entity);
                else entity.setRemainingFireTicks(120);
            }
        }
    }
    private static void playerTick(PlayerTickEvent.Post event) {
        Player player=event.getEntity();
        if(!(player.level() instanceof ServerLevel level)||player.tickCount%5!=0||!player.hasEffect(IMBUEMENTS.get("aqua")))return;
        for(BlockPos pos:BlockPos.betweenClosed(player.blockPosition().offset(-2,-2,-2),player.blockPosition().offset(1,1,1))) {
            BlockState state=level.getBlockState(pos);
            if(!state.getFluidState().is(FluidTags.LAVA)||!level.mayInteract(player,pos))continue;
            if(NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level,pos,state,player)).isCanceled())continue;
            level.setBlock(pos,Blocks.OBSIDIAN.defaultBlockState(),3);
        }
    }
    private static final class ElementalEffect extends MobEffect { ElementalEffect(int color){super(MobEffectCategory.BENEFICIAL,color);} }
    private TinkerPotions() {}
}
