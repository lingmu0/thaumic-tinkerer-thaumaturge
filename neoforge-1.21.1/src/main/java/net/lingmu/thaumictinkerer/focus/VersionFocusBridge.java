package net.lingmu.thaumictinkerer.focus;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import java.util.Map;
import net.lingmu.thaumictinkerer.StackData;
import net.lingmu.thaumictinkerer.TTContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

final class VersionFocusBridge {
    private record SmeltProgress(BlockPos pos,long start,long last) {}
    private static final Map<Player,SmeltProgress> SMELTING=new java.util.WeakHashMap<>();
    static ItemStack wand(Player player) {
        if(player.getUseItem().getItem() instanceof ItemWand)return player.getUseItem();
        if(player.getMainHandItem().getItem() instanceof ItemWand)return player.getMainHandItem();
        if(player.getOffhandItem().getItem() instanceof ItemWand)return player.getOffhandItem();
        return ItemStack.EMPTY;
    }
    static void damage(LivingEntity target,Player player,float amount) { target.hurt(target.damageSources().playerAttack(player),amount); }
    static void magicDamage(Player player,float amount) { player.hurt(player.damageSources().magic(),amount); }
    static GasBlock gas(String name,boolean shadow,boolean temporary) {
        BlockBehaviour.Properties props=BlockBehaviour.Properties.of().air().noCollission().noOcclusion().replaceable().noLootTable().strength(-1).lightLevel(s->shadow?0:temporary?12:15);
        return new GasBlock(props,shadow,temporary) {
            @Override protected int getLightBlock(BlockState state,BlockGetter level,BlockPos pos) { return shadow?15:0; }
        };
    }
    static void shareResearch(Player user,ItemStack stack) {
        if(!(user instanceof ServerPlayer player))return;
        String stored=StackData.string(stack,"tt_shared_research");
        if(StackData.string(stack,"tt_research_author").isEmpty()||player.isShiftKeyDown()&&StackData.string(stack,"tt_research_author").equals(player.getUUID().toString())) {
            var knowledge=PlayerResearchAccess.of(player);
            String complete=knowledge.knownEntries().stream().filter(knowledge::hasCompleted).map(ResourceLocation::toString).sorted().collect(java.util.stream.Collectors.joining("\n"));
            StackData.string(stack,"tt_shared_research",complete);StackData.string(stack,"tt_research_author",player.getUUID().toString());
            StackData.string(stack,"tt_research_author_name",player.getGameProfile().getName());
            player.displayClientMessage(Component.translatable("message.thaumictinkerer.share_written"),true);
        } else {
            for(String id:stored.split("\n")) {
                ResourceLocation key=ResourceLocation.tryParse(id);
                if(key!=null)ResearchManager.complete(player,key);
            }
            player.displayClientMessage(Component.translatable("message.thaumictinkerer.share_read"),true);
        }
    }
    static void smelt(CastContext ctx,BlockHitResult hit) {
        if(hit==null||!(ctx.caster() instanceof ServerPlayer player))return;
        BlockPos pos=hit.getBlockPos();BlockState state=ctx.level().getBlockState(pos);
        if(state.isAir()||state.getDestroySpeed(ctx.level(),pos)<0||ctx.level().getBlockEntity(pos)!=null||!ctx.level().mayInteract(player,pos))return;
        if(NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(ctx.level(),pos,state,player)).isCanceled())return;
        SingleRecipeInput input=new SingleRecipeInput(new ItemStack(state.getBlock()));
        var recipe=ctx.level().getRecipeManager().getRecipeFor(RecipeType.SMELTING,input,ctx.level());
        if(recipe.isEmpty())return;
        ItemStack output=recipe.get().value().assemble(input,ctx.registries());
        if(output.isEmpty()||!(output.getItem() instanceof net.minecraft.world.item.BlockItem blockItem))return;
        long now=ctx.level().getGameTime();SmeltProgress previous=SMELTING.get(player);
        if(previous==null||!previous.pos().equals(pos)||now-previous.last()>15){SMELTING.put(player,new SmeltProgress(pos,now,now));return;}
        if(now-previous.start()<20){SMELTING.put(player,new SmeltProgress(pos,previous.start(),now));return;}
        SMELTING.remove(player);
        ctx.level().setBlock(pos,blockItem.getBlock().defaultBlockState(),3);
        ctx.level().levelEvent(2001,pos,Block.getId(state));
    }
    static void dislocate(CastContext ctx,BlockHitResult hit) {
        if(hit==null||!(ctx.caster() instanceof ServerPlayer player))return;
        ItemStack caster=wand(player);if(caster.isEmpty())return;
        ItemWand wand=(ItemWand)caster.getItem();ItemStack focus=wand.socketedFocus(caster);
        CompoundTag data=StackData.read(focus);
        if(data.contains("tt_displaced_state")) {
            BlockState stored=BlockState.CODEC.parse(NbtOps.INSTANCE,data.getCompound("tt_displaced_state")).result().orElse(null);
            if(stored==null)return;
            BlockPos pos=hit.getBlockPos().relative(hit.getDirection());
            if(!ctx.level().getBlockState(pos).canBeReplaced()||!ctx.level().mayInteract(player,pos)||!stored.canSurvive(ctx.level(),pos))return;
            if(NeoForge.EVENT_BUS.post(new BlockEvent.EntityPlaceEvent(net.neoforged.neoforge.common.util.BlockSnapshot.create(ctx.level().dimension(),ctx.level(),pos),ctx.level().getBlockState(hit.getBlockPos()),player)).isCanceled())return;
            ctx.level().setBlock(pos,stored,3);
            if(data.contains("tt_displaced_tile")) {
                CompoundTag tile=data.getCompound("tt_displaced_tile");
                tile.putInt("x",pos.getX());tile.putInt("y",pos.getY());tile.putInt("z",pos.getZ());
                BlockEntity restored=BlockEntity.loadStatic(pos,stored,tile,ctx.registries());
                if(restored!=null)ctx.level().setBlockEntity(restored);
            }
            data.remove("tt_displaced_state");data.remove("tt_displaced_tile");
        } else {
            BlockPos pos=hit.getBlockPos();BlockState state=ctx.level().getBlockState(pos);BlockEntity tile=ctx.level().getBlockEntity(pos);
            if(state.isAir()||state.getDestroySpeed(ctx.level(),pos)<0||!state.getFluidState().isEmpty()||!ctx.level().mayInteract(player,pos))return;
            if(NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(ctx.level(),pos,state,player)).isCanceled())return;
            int entropy=tile instanceof SpawnerBlockEntity?10000:tile!=null?2500:500;
            int earth=tile instanceof SpawnerBlockEntity?5000:tile!=null?500:100;
            if(!player.getAbilities().instabuild&&!WandVisHelper.payCosts(caster,player,Map.of(TTAspects.PERDITIO,entropy,TTAspects.ORDO,entropy,TTAspects.TERRA,earth),true,false))return;
            var encoded=BlockState.CODEC.encodeStart(NbtOps.INSTANCE,state).result();if(encoded.isEmpty())return;
            data.put("tt_displaced_state",encoded.get());
            if(tile!=null)data.put("tt_displaced_tile",tile.saveWithFullMetadata(ctx.registries()));
            ctx.level().removeBlockEntity(pos);ctx.level().setBlock(pos,Blocks.AIR.defaultBlockState(),3);
        }
        focus.set(DataComponents.CUSTOM_DATA,CustomData.of(data));
        FocusStorageIdentity.update(focus,data.contains("tt_displaced_state"),data.hashCode());
        wand.socketFocus(caster,focus);
    }
    private VersionFocusBridge() {}
}
