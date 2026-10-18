package net.lingmu.thaumictinkerer.kami;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
/** Temporary light left by awakened leggings, removed by scheduled server ticks. */
public final class KamiLightBlock extends Block {
    public KamiLightBlock(Properties properties) { super(properties); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) { level.removeBlock(pos, false); }
}
