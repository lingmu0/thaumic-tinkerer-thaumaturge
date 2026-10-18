package net.lingmu.thaumictinkerer.device;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;

public final class InfusedCropBlock extends CropBlock {
    private final String aspect;
    public InfusedCropBlock(String aspect, Properties properties) { super(properties); this.aspect = aspect; }
    @Override public MapCodec<? extends CropBlock> codec() { return simpleCodec(p -> new InfusedCropBlock(aspect, p)); }
    @Override protected ItemLike getBaseSeedId() { return DeviceContent.SEEDS.get(aspect).get(); }
}
