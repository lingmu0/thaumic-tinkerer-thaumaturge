package net.lingmu.thaumictinkerer.device;

import java.util.*;
import com.mojang.authlib.GameProfile;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
import net.neoforged.neoforge.transfer.*;
import net.neoforged.neoforge.transfer.item.*;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class VersionDevices {
    static List<String> oreMaterials(net.minecraft.world.level.block.state.BlockState state) {
        return state.typeHolder().tags().map(net.minecraft.tags.TagKey::location)
            .filter(id -> id.getNamespace().equals("c") && id.getPath().startsWith("ores/"))
            .map(id -> id.getPath().substring(5)).toList();
    }
    static boolean fireEnabled(ServerLevel level,BlockPos pos){return level.canSpreadFireAround(pos);}
    static boolean payWand(ItemStack wand,Player player,Map<net.minecraft.resources.ResourceKey<com.leclowndu93150.thaumaturge.api.aspect.IAspect>,Integer> cost){return com.leclowndu93150.thaumaturge.content.wands.WandVisHelper.payCosts(wand,player,cost,true,true);}
    static BlockEntityType<VersionMachine> type(Block[] blocks){return new BlockEntityType<>(VersionMachine::new,blocks);}
    static void capabilities(RegisterCapabilitiesEvent event){
        var type=DeviceContent.MACHINE.get();
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT,type,(machine,side)->machine);
        event.registerBlockEntity(com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities.CONTAINER,type,(machine,side)->machine.kind()==DeviceContent.Kind.INTERFACE && machine.validTarget()?machine.getLevel().getCapability(com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities.CONTAINER,machine.target,side):null);
        event.registerBlockEntity(Capabilities.Item.BLOCK,type,(machine,side)->{
            if(machine.kind()==DeviceContent.Kind.ENCHANTER)return null;
            if(machine.kind()==DeviceContent.Kind.INTERFACE)return machine.validTarget()?machine.getLevel().getCapability(Capabilities.Item.BLOCK,machine.target,side):null;
            if(machine.getContainerSize()==0)return null;
            return new ItemStacksResourceHandler(machine.inventory){
                @Override public int size(){return machine.kind()==DeviceContent.Kind.FUNNEL && side==Direction.DOWN?0:machine.getContainerSize();}
                @Override public boolean isValid(int slot,ItemResource resource){return slot>=0 && slot<size() && machine.canPlaceItem(slot,resource.toStack());}
                @Override protected int getCapacity(int slot,ItemResource resource){return Math.min(super.getCapacity(slot,resource),machine.getMaxStackSize());}
                @Override protected void onContentsChanged(int slot,ItemStack previous){machine.setChanged();}
            };
        });
        event.registerBlockEntity(Capabilities.Fluid.BLOCK,type,(machine,side)->machine.kind()==DeviceContent.Kind.INTERFACE && machine.validTarget()?machine.getLevel().getCapability(Capabilities.Fluid.BLOCK,machine.target,side):null);
        event.registerBlockEntity(Capabilities.Energy.BLOCK,type,(machine,side)->machine.kind()==DeviceContent.Kind.INTERFACE && machine.validTarget()?machine.getLevel().getCapability(Capabilities.Energy.BLOCK,machine.target,side):null);
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
    static boolean spawn(ServerLevel level,Identifier id,BlockPos pos){
        EntityType<?> type=BuiltInRegistries.ENTITY_TYPE.getValue(id);
        if(type==null)return false;
        Entity entity=type.create(level,EntitySpawnReason.MOB_SUMMONED);if(entity==null)return false;
        entity.snapTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
        if(!level.noCollision(entity))return false;
        if(entity instanceof Mob mob){mob.finalizeSpawn(level,level.getCurrentDifficultyAt(pos),EntitySpawnReason.MOB_SUMMONED,null);mob.setPersistenceRequired();}
        return level.addFreshEntity(entity);
    }
    static void commandGolems(MachineBlockEntity machine){
        if(machine.owner==null)return;
        for(var golem:machine.getLevel().getEntitiesOfClass(EntityThaumaturgeGolem.class,new AABB(machine.getBlockPos()).inflate(32),g->g.ownerIdentity().filter(machine.owner::equals).isPresent())){
            golem.setHomeTo(machine.target,32);golem.getNavigation().moveTo(machine.target.getX()+.5,machine.target.getY(),machine.target.getZ()+.5,1);
        }
    }
    static void describeGolems(MachineBlockEntity machine,Player player){
        for(var golem:machine.getLevel().getEntitiesOfClass(EntityThaumaturgeGolem.class,new AABB(machine.getBlockPos()).inflate(32),g->g.ownerIdentity().filter(player.getUUID()::equals).isPresent()))
            net.lingmu.thaumictinkerer.VersionMessages.send(player,Component.translatable("message.thaumictinkerer.device.golem",golem.getName(),golem.getHealth(),golem.blockPosition().toShortString()),false);
    }
    static Container inventoryAt(ServerLevel level,BlockPos pos){
        ResourceHandler<ItemResource> handler=level.getCapability(Capabilities.Item.BLOCK,pos,null);
        if(handler==null)return null;
        return new Container(){
            public int getContainerSize(){return handler.size();}
            public boolean isEmpty(){for(int i=0;i<handler.size();i++)if(handler.getAmountAsLong(i)>0)return false;return true;}
            public ItemStack getItem(int i){return handler.getResource(i).toStack(handler.getAmountAsInt(i));}
            public ItemStack removeItem(int i,int amount){
                ItemResource resource=handler.getResource(i);if(resource.isEmpty())return ItemStack.EMPTY;
                try(Transaction transaction=Transaction.openRoot()){int removed=handler.extract(i,resource,amount,transaction);transaction.commit();return resource.toStack(removed);}
            }
            public ItemStack removeItemNoUpdate(int i){return removeItem(i,Integer.MAX_VALUE);}
            public void setItem(int i,ItemStack stack){
                try(Transaction transaction=Transaction.openRoot()){
                    ItemResource old=handler.getResource(i);int amount=handler.getAmountAsInt(i);
                    if(!old.isEmpty() && handler.extract(i,old,amount,transaction)!=amount)return;
                    if(!stack.isEmpty() && handler.insert(i,ItemResource.of(stack),stack.getCount(),transaction)!=stack.getCount())return;
                    transaction.commit();
                }
            }
            public void setChanged(){BlockEntity be=level.getBlockEntity(pos);if(be!=null)be.setChanged();}
            public boolean stillValid(Player player){return true;}
            public void clearContent(){for(int i=0;i<handler.size();i++)removeItemNoUpdate(i);}
        };
    }
    private VersionDevices(){}
}
