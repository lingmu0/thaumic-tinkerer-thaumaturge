package net.lingmu.thaumictinkerer.kami;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class KamiGateBase extends BlockEntity {
    public final NonNullList<ItemStack> pearls = NonNullList.withSize(10, ItemStack.EMPTY);
    public int selected;
    public boolean locked;
    protected KamiGateBase(BlockPos pos, BlockState state) { super(KamiContent.GATE_ENTITY.get(), pos, state); }
}
