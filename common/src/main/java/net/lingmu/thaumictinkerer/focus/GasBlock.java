package net.lingmu.thaumictinkerer.focus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class GasBlock extends Block {
    public static final IntegerProperty RANGE=IntegerProperty.create("range",0,4);
    final boolean shadow, temporary;
    public GasBlock(Properties properties,boolean shadow,boolean temporary) {
        super(properties);this.shadow=shadow;this.temporary=temporary;
        registerDefaultState(stateDefinition.any().setValue(RANGE,0));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(RANGE); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context) { return net.minecraft.world.phys.shapes.Shapes.empty(); }
    @Override protected void tick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random) {
        if(temporary) {
            boolean held=false;
            for(net.minecraft.world.entity.player.Player player:level.getEntitiesOfClass(net.minecraft.world.entity.player.Player.class,new net.minecraft.world.phys.AABB(pos).inflate(1.5))) {
                for(int slot=0;slot<player.getInventory().getContainerSize();slot++)if(player.getInventory().getItem(slot).is(FocusContent.BRIGHT_NITOR.get())){held=true;break;}
                if(held)break;
            }
            if(held)level.scheduleTick(pos,this,20);else level.removeBlock(pos,false);
            return;
        }
        int range=state.getValue(RANGE);
        if(range<=0)return;
        for(Direction direction:Direction.values()) {
            BlockPos next=pos.relative(direction);
            if(level.hasChunkAt(next)&&level.getBlockState(next).isAir()&&level.getBlockState(next).getBlock()!=this) {
                level.setBlock(next,state.setValue(RANGE,range-1),3);
                level.scheduleTick(next,this,10);
            }
        }
        level.setBlock(pos,state.setValue(RANGE,0),2);
    }
}
