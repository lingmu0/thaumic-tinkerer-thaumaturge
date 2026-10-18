package net.lingmu.thaumictinkerer.kami;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
public final class KamiGateBlockEntity extends KamiGateBase {
    public KamiGateBlockEntity(BlockPos pos, BlockState state) { super(pos, state); }
    @Override protected void saveAdditional(ValueOutput output) { super.saveAdditional(output); ContainerHelper.saveAllItems(output, pearls); output.putBoolean("locked", locked); output.putInt("selected", selected); }
    @Override protected void loadAdditional(ValueInput input) { super.loadAdditional(input); ContainerHelper.loadAllItems(input, pearls); locked = input.getBooleanOr("locked", false); selected = Math.floorMod(input.getIntOr("selected", 0), 10); }
}
