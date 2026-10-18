package net.lingmu.thaumictinkerer.device;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;

public final class SeedItem extends Item {
    private final String aspect;
    public SeedItem(String aspect, Properties properties) { super(properties); this.aspect = aspect; }
    @Override public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var ground = context.getClickedPos();
        if (!level.getBlockState(ground).is(Blocks.FARMLAND) || !level.isEmptyBlock(ground.above())) return InteractionResult.PASS;
        if (!level.isClientSide) {
            level.setBlockAndUpdate(ground.above(), DeviceContent.CROPS.get(aspect).get().defaultBlockState());
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
