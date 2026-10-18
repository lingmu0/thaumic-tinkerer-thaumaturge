package net.lingmu.thaumictinkerer.device;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class VersionDeviceBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING=BlockStateProperties.FACING;
    public static final BooleanProperty DISGUISED=BooleanProperty.create("disguised");
    public final DeviceContent.Kind kind;
    public VersionDeviceBlock(DeviceContent.Kind kind,Properties properties){super(properties);this.kind=kind;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(DISGUISED,false));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(p->new VersionDeviceBlock(kind,p));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING,DISGUISED);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getNearestLookingDirection().getOpposite());}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new VersionMachine(pos,state);}
    @Override protected RenderShape getRenderShape(BlockState state){return kind==DeviceContent.Kind.CAMO || kind==DeviceContent.Kind.PLATFORM || state.getValue(DISGUISED)?RenderShape.INVISIBLE:RenderShape.MODEL;}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return createTickerHelper(type,DeviceContent.MACHINE.get(),MachineBlockEntity::tick);}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(!level.isClientSide() && level.getBlockEntity(pos) instanceof MachineBlockEntity machine)machine.interact(player,InteractionHand.MAIN_HAND);
        return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useItemOn(ItemStack held,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(held.getItem() instanceof ConnectorItem)return InteractionResult.PASS;
        if(!level.isClientSide() && level.getBlockEntity(pos) instanceof MachineBlockEntity machine)machine.interact(player,hand);
        return InteractionResult.SUCCESS;
    }
    @Override protected VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){
        if(kind==DeviceContent.Kind.PLATFORM){VoxelShape top=Block.box(0,14,0,16,16,16);return !context.isDescending() && context.isAbove(top,pos,true)?top:Shapes.empty();}
        return super.getCollisionShape(state,level,pos,context);
    }
}
