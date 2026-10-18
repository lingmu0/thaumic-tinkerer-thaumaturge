package net.lingmu.thaumictinkerer.kami;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.level.block.state.BlockState;
public final class KamiGateBlockEntity extends KamiGateBase {
    public KamiGateBlockEntity(BlockPos pos, BlockState state) { super(pos, state); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.saveAdditional(tag, registries); ContainerHelper.saveAllItems(tag, pearls, registries); tag.putBoolean("locked", locked); tag.putInt("selected", selected); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.loadAdditional(tag, registries); ContainerHelper.loadAllItems(tag, pearls, registries); locked = tag.getBoolean("locked"); selected = Math.floorMod(tag.getInt("selected"), 10); }
}
