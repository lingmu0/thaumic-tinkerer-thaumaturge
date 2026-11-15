package net.lingmu.thaumictinkerer.focus;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.lingmu.thaumictinkerer.kami.KamiPearl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Fixed-purpose TT effects are ordinary Thaumaturge spell nodes, paid and scheduled by its spell engine. */
public record TinkerEffect(String action) implements SpellBehavior {
    public static final MapCodec<TinkerEffect> CODEC=Codec.STRING.fieldOf("action").xmap(TinkerEffect::new,TinkerEffect::action);
    @Override public SpellBehaviorType<?> type() { return FocusContent.EFFECT.get(); }
    @Override public void execute(CastContext ctx,List<SpellTarget> incoming) {
        if(!(ctx.caster() instanceof ServerPlayer player))return;
        switch(action) {
            case "flight" -> { player.setDeltaMovement(player.getLookAngle().scale(2.0/3.0*Math.max(1,ctx.power())));player.hurtMarked=true;player.fallDistance=0; }
            case "heal" -> player.heal(0.555556F*ctx.power());
            case "telekinesis" -> telekinesis(ctx,player);
            case "deflect" -> deflect(ctx.level(),player);
            case "smelt" -> VersionFocusBridge.smelt(ctx,aim(player,incoming));
            case "ender_chest" -> player.openMenu(new SimpleMenuProvider((id,inventory,p)->ChestMenu.threeRows(id,inventory,p.getEnderChestInventory()),Component.translatable("container.enderchest")));
            case "dislocation" -> VersionFocusBridge.dislocate(ctx,aim(player,incoming));
            case "shadowbeam" -> shadowbeam(ctx,player);
            case "xp_drain" -> xpDrain(player);
            case "recall" -> { if(!KamiPearl.recall(player))player.displayClientMessage(Component.translatable("message.thaumictinkerer.recall_failed"),true); }
        }
        ctx.proceed(incoming);
    }
    private static BlockHitResult aim(ServerPlayer player,List<SpellTarget> incoming) {
        for(SpellTarget target:incoming)if(target.block().isPresent())return target.block().get();
        HitResult hit=player.pick(16,1,false);
        return hit instanceof BlockHitResult block&&hit.getType()==HitResult.Type.BLOCK?block:null;
    }
    private static void telekinesis(CastContext ctx,ServerPlayer player) {
        Vec3 target=player.getEyePosition().add(player.isShiftKeyDown()?Vec3.ZERO:player.getLookAngle().scale(5));
        for(ItemEntity item:ctx.level().getEntitiesOfClass(ItemEntity.class,new AABB(target,target).inflate(6))) {
            Vec3 movement=target.subtract(item.position());
            item.setDeltaMovement(movement.normalize().scale(Math.min(0.3333,movement.length())));
            item.hurtMarked=true;
            ctx.level().sendParticles(ParticleTypes.ENCHANT,item.getX(),item.getY(),item.getZ(),1,0,0,0,0);
        }
    }
    public static void deflect(ServerLevel level,Player player) {
        for(Projectile projectile:level.getEntitiesOfClass(Projectile.class,player.getBoundingBox().inflate(4))) {
            var type=projectile.getType();
            if(type==net.minecraft.world.entity.EntityType.EXPERIENCE_BOTTLE||type==net.minecraft.world.entity.EntityType.FIREBALL||type==net.minecraft.world.entity.EntityType.SMALL_FIREBALL||type==net.minecraft.world.entity.EntityType.WITHER_SKULL||type==net.minecraft.world.entity.EntityType.DRAGON_FIREBALL||projectile.getOwner()==player)continue;
            Vec3 away=projectile.position().subtract(player.getBoundingBox().getCenter()).normalize();
            projectile.setDeltaMovement(away.scale(Math.max(0.5,projectile.getDeltaMovement().length())));
            projectile.setOwner(player);projectile.hurtMarked=true;
        }
    }
    private static void xpDrain(ServerPlayer player) {
        ItemStack wand=VersionFocusBridge.wand(player);
        if(wand.isEmpty())return;
        int max=VersionFocusBridge.wandCapacity(wand);
        for(var aspect:TTAspects.PRIMALS) {
            int room=max-VersionFocusBridge.wandCharge(wand,aspect);
            if(room<=0)continue;
            if(player.totalExperience>=30) {
                player.giveExperiencePoints(-30);
                VersionFocusBridge.addWandCharge(wand,aspect,Math.min(500,room));
            }
            return;
        }
    }
    private static void shadowbeam(CastContext ctx,ServerPlayer player) {
        Vec3 origin=player.getEyePosition(),direction=player.getLookAngle();
        double remaining=45;
        Set<Integer> hitEntities=new HashSet<>();
        for(int bounce=0;bounce<8&&remaining>0.05;bounce++) {
            Vec3 end=origin.add(direction.scale(remaining));
            BlockHitResult wall=ctx.level().clip(new ClipContext(origin,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
            Vec3 stop=wall.getType()==HitResult.Type.MISS?end:wall.getLocation();
            for(LivingEntity target:ctx.level().getEntitiesOfClass(LivingEntity.class,new AABB(origin,stop).inflate(0.5))) {
                if(target==player||ctx.isAlly(target)||!target.isAlive()||hitEntities.contains(target.getId()))continue;
                if(target.getBoundingBox().inflate(0.2).clip(origin,stop).isPresent()) {
                    hitEntities.add(target.getId());VersionFocusBridge.damage(target,player,8*Math.max(0.25F,ctx.power()));
                }
            }
            double distance=origin.distanceTo(stop);
            for(double step=0;step<distance;step+=0.5) {
                Vec3 at=origin.add(direction.scale(step));
                ctx.level().sendParticles(ParticleTypes.WITCH,at.x,at.y,at.z,1,0,0,0,0);
            }
            if(wall.getType()==HitResult.Type.MISS)break;
            remaining-=distance;
            Vec3 normal=Vec3.atLowerCornerOf(wall.getDirection().getNormal());
            direction=direction.subtract(normal.scale(2*direction.dot(normal))).normalize();
            origin=stop.add(direction.scale(0.02));
        }
    }
}
