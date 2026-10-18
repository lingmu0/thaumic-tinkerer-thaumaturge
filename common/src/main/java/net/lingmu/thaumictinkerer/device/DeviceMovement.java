package net.lingmu.thaumictinkerer.device;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.BlockEvent;
import com.mojang.authlib.GameProfile;

/** Snapshot both endpoints before committing; detach block entities before removal so inventories cannot drop and duplicate. */
final class DeviceMovement {
    private record Snapshot(BlockState state, CompoundTag data) {}
    private static Snapshot snapshot(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return new Snapshot(level.getBlockState(pos), be == null ? null : be.saveWithFullMetadata(level.registryAccess()));
    }
    private static boolean movable(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos) || level.isOutsideBuildHeight(pos)) return false;
        BlockState state = level.getBlockState(pos);
        return state.getDestroySpeed(level,pos) >= 0 && !state.is(Blocks.MOVING_PISTON) && !state.is(Blocks.NETHER_PORTAL)
            && !state.is(Blocks.END_PORTAL) && !state.is(Blocks.END_GATEWAY);
    }
    private static void clear(ServerLevel level, BlockPos pos) { level.removeBlockEntity(pos); level.setBlock(pos,Blocks.AIR.defaultBlockState(),2); }
    private static void restore(ServerLevel level, BlockPos pos, Snapshot snapshot) {
        level.setBlock(pos,snapshot.state,2);
        if (snapshot.data != null) {
            BlockEntity restored = BlockEntity.loadStatic(pos,snapshot.state,snapshot.data,level.registryAccess());
            if (restored != null) { level.setBlockEntity(restored); restored.setChanged(); }
        }
    }
    private static net.minecraft.server.level.ServerPlayer actor(MachineBlockEntity machine) {
        return FakePlayerFactory.get((ServerLevel)machine.getLevel(),new GameProfile(machine.owner==null
            ?UUID.fromString("86f31227-c7d3-4782-b065-efb9422420b4"):machine.owner,"[TT Device]"));
    }
    private static boolean authorize(ServerLevel level,net.minecraft.server.level.ServerPlayer actor,BlockPos... positions) {
        for(BlockPos pos:positions) {
            if(!level.mayInteract(actor,pos) || NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level,pos,level.getBlockState(pos),actor)).isCanceled()) return false;
        }
        return true;
    }
    static void swap(MachineBlockEntity machine, BlockPos first, BlockPos second) {
        ServerLevel level=(ServerLevel)machine.getLevel();
        if (first.equals(second) || !movable(level,first) || !movable(level,second)) return;
        var actor=actor(machine);
        if(!authorize(level,actor,first,second))return;
        BlockSnapshot beforeA=BlockSnapshot.create(level.dimension(),level,first),beforeB=BlockSnapshot.create(level.dimension(),level,second);
        Snapshot a=snapshot(level,first), b=snapshot(level,second);
        clear(level,first);clear(level,second);restore(level,first,b);restore(level,second,a);
        if(EventHooks.onBlockPlace(actor,beforeA,net.minecraft.core.Direction.UP) || EventHooks.onBlockPlace(actor,beforeB,net.minecraft.core.Direction.UP)) {
            clear(level,first);clear(level,second);restore(level,first,a);restore(level,second,b);return;
        }
        level.updateNeighborsAt(first,b.state.getBlock());level.updateNeighborsAt(second,a.state.getBlock());
    }
    static void mobilize(MachineBlockEntity machine, BlockPos next) {
        ServerLevel level=(ServerLevel)machine.getLevel();
        BlockPos current=machine.getBlockPos();
        if (!movable(level,current.above()) || !level.isEmptyBlock(next) || !level.isEmptyBlock(next.above())) return;
        var actor=actor(machine);
        if(!authorize(level,actor,current,current.above(),next,next.above()))return;
        BlockSnapshot beforeBase=BlockSnapshot.create(level.dimension(),level,next),beforeTop=BlockSnapshot.create(level.dimension(),level,next.above());
        Snapshot body=snapshot(level,current), passenger=snapshot(level,current.above());
        List<Entity> entities=List.copyOf(level.getEntities((Entity)null,new AABB(current.above()).expandTowards(0,2,0),Entity::isAlive));
        clear(level,current.above());clear(level,current);
        restore(level,next,body);restore(level,next.above(),passenger);
        if(EventHooks.onBlockPlace(actor,beforeBase,net.minecraft.core.Direction.UP) || EventHooks.onBlockPlace(actor,beforeTop,net.minecraft.core.Direction.UP)) {
            clear(level,next.above());clear(level,next);restore(level,current,body);restore(level,current.above(),passenger);return;
        }
        Vec3 delta=Vec3.atCenterOf(next).subtract(Vec3.atCenterOf(current));
        for(Entity entity:entities) VersionDevices.teleport(entity,entity.position().add(delta));
        level.updateNeighborsAt(current,Blocks.AIR);level.updateNeighborsAt(next,body.state.getBlock());
    }
    private DeviceMovement() {}
}
