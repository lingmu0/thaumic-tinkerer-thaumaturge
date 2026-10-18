package net.lingmu.thaumictinkerer.device;

import java.util.*;
import com.mojang.authlib.GameProfile;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.items.*;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public final class VersionDevices {
    static List<String> oreMaterials(net.minecraft.world.level.block.state.BlockState state) {
        return state.getTags().map(net.minecraft.tags.TagKey::location)
            .filter(id -> id.getNamespace().equals("c") && id.getPath().startsWith("ores/"))
            .map(id -> id.getPath().substring(5)).toList();
    }
    static boolean fireEnabled(ServerLevel level,BlockPos pos){return level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOFIRETICK);}
    static boolean payWand(ItemStack wand,Player player,Map<net.minecraft.resources.ResourceKey<com.leclowndu93150.thaumaturge.api.aspect.IAspect>,Integer> cost){return com.leclowndu93150.thaumaturge.content.wands.WandVisHelper.payCosts(wand,player,cost,true,true);}
    static BlockEntityType<VersionMachine> type(Block[] blocks){return BlockEntityType.Builder.of(VersionMachine::new,blocks).build(null);}
    static void capabilities(RegisterCapabilitiesEvent event){
        var type=DeviceContent.MACHINE.get();
        EssentiaCapabilities.registerConduit(event,type,(machine,side)->machine);
        event.registerBlockEntity(com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities.CONTAINER,type,(machine,side)->machine.kind()==DeviceContent.Kind.INTERFACE && machine.validTarget()?machine.getLevel().getCapability(com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities.CONTAINER,machine.target,side):null);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,type,(machine,side)->{
            if(machine.kind()==DeviceContent.Kind.ENCHANTER)return null;
            if(machine.kind()==DeviceContent.Kind.INTERFACE)return machine.validTarget()?machine.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,machine.target,side):null;
            return machine.getContainerSize()>0?(side==null?new InvWrapper(machine):new net.neoforged.neoforge.items.wrapper.SidedInvWrapper(machine,side)):null;
        });
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,type,(machine,side)->machine.kind()==DeviceContent.Kind.INTERFACE && machine.validTarget()?machine.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,machine.target,side):null);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,type,(machine,side)->machine.kind()==DeviceContent.Kind.INTERFACE && machine.validTarget()?machine.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,machine.target,side):null);
    }
    static void act(MachineBlockEntity machine,boolean rightClick){
        ServerLevel level=(ServerLevel)machine.getLevel();
        ItemStack held=machine.inventory.getFirst();if(held.isEmpty())return;
        var profile=new GameProfile(machine.owner==null?UUID.fromString("86f31227-c7d3-4782-b065-efb9422420b4"):machine.owner,"[TT Device]");
        var actor=FakePlayerFactory.get(level,profile);
        BlockPos target=machine.getBlockPos().relative(machine.facing(),machine.kind()==DeviceContent.Kind.REMOTE_PLACER?machine.distance:1);
        if(!level.hasChunkAt(target) || level.isOutsideBuildHeight(target))return;
        actor.teleportTo(machine.getBlockPos().getX()+.5,machine.getBlockPos().getY(),machine.getBlockPos().getZ()+.5);
        actor.setItemInHand(InteractionHand.MAIN_HAND,held);
        if(rightClick){
            BlockHitResult hit=new BlockHitResult(Vec3.atCenterOf(target),machine.facing().getOpposite(),target,false);
            InteractionResult result=held.useOn(new UseOnContext(actor,InteractionHand.MAIN_HAND,hit));
            if(!result.consumesAction() && !(held.getItem() instanceof BlockItem))held.getItem().use(level,actor,InteractionHand.MAIN_HAND);
        }else{
            var entities=level.getEntitiesOfClass(LivingEntity.class,new AABB(target),e->e!=actor && !(e instanceof Player));
            if(!entities.isEmpty())actor.attack(entities.getFirst());
            else {
                var state=level.getBlockState(target);
                if(machine.miningState!=state){machine.miningState=state;machine.miningProgress=0;}
                if(!state.isAir() && state.getDestroySpeed(level,target)>=0){
                    machine.miningProgress+=state.getDestroyProgress(actor,level,target)*20;
                    level.destroyBlockProgress(actor.getId(),target,Math.min(9,(int)(machine.miningProgress*10)));
                    if(machine.miningProgress>=1){actor.gameMode.destroyBlock(target);machine.miningProgress=0;level.destroyBlockProgress(actor.getId(),target,-1);}
                }
            }
        }
        machine.inventory.set(0,actor.getMainHandItem());actor.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);machine.setChanged();
    }
    static void openTarget(MachineBlockEntity machine,Player player){
        if(machine.getLevel().getBlockEntity(machine.target) instanceof MenuProvider provider && player instanceof ServerPlayer serverPlayer)serverPlayer.openMenu(provider);
    }
    static void swapBlocks(MachineBlockEntity machine,BlockPos a,BlockPos b){DeviceMovement.swap(machine,a,b);}
    static void moveMobilizer(MachineBlockEntity machine,BlockPos target){DeviceMovement.mobilize(machine,target);}
    static void teleport(Entity entity,Vec3 target){entity.teleportTo(target.x,target.y,target.z);}
    static boolean spawn(ServerLevel level,ResourceLocation id,BlockPos pos){
        EntityType<?> type=BuiltInRegistries.ENTITY_TYPE.get(id);
        if(type==null || type==EntityType.PIG && !id.equals(ResourceLocation.withDefaultNamespace("pig")))return false;
        Entity entity=type.create(level);if(entity==null)return false;
        entity.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
        if(!level.noCollision(entity))return false;
        if(entity instanceof Mob mob){mob.finalizeSpawn(level,level.getCurrentDifficultyAt(pos),MobSpawnType.MOB_SUMMONED,null);mob.setPersistenceRequired();}
        return level.addFreshEntity(entity);
    }
    static void commandGolems(MachineBlockEntity machine){
        if(machine.owner==null)return;
        for(var golem:machine.getLevel().getEntitiesOfClass(EntityThaumaturgeGolem.class,new AABB(machine.getBlockPos()).inflate(32),g->machine.owner.equals(g.getOwnerUUID()))){
            golem.restrictTo(machine.target,32);golem.getNavigation().moveTo(machine.target.getX()+.5,machine.target.getY(),machine.target.getZ()+.5,1);
        }
    }
    static void describeGolems(MachineBlockEntity machine,Player player){
        for(var golem:machine.getLevel().getEntitiesOfClass(EntityThaumaturgeGolem.class,new AABB(machine.getBlockPos()).inflate(32),g->player.getUUID().equals(g.getOwnerUUID())))
            player.displayClientMessage(Component.translatable("message.thaumictinkerer.device.golem",golem.getName(),golem.getHealth(),golem.blockPosition().toShortString()),false);
    }
    static Container inventoryAt(ServerLevel level,BlockPos pos){
        IItemHandler handler=level.getCapability(Capabilities.ItemHandler.BLOCK,pos,null);
        if(handler==null)return null;
        return new Container(){
            public int getContainerSize(){return handler.getSlots();}
            public boolean isEmpty(){for(int i=0;i<handler.getSlots();i++)if(!handler.getStackInSlot(i).isEmpty())return false;return true;}
            public ItemStack getItem(int i){return handler.getStackInSlot(i);}
            public ItemStack removeItem(int i,int amount){return handler.extractItem(i,amount,false);}
            public ItemStack removeItemNoUpdate(int i){return removeItem(i,Integer.MAX_VALUE);}
            public void setItem(int i,ItemStack stack){if(handler instanceof IItemHandlerModifiable modifiable)modifiable.setStackInSlot(i,stack);}
            public void setChanged(){BlockEntity be=level.getBlockEntity(pos);if(be!=null)be.setChanged();}
            public boolean stillValid(Player player){return true;}
            public void clearContent(){for(int i=0;i<handler.getSlots();i++)removeItemNoUpdate(i);}
        };
    }
    private VersionDevices(){}
}
