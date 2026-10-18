package net.lingmu.thaumictinkerer.kami;

import java.util.List;
import net.lingmu.thaumictinkerer.StackData;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class KamiToolItem extends Item {
    public enum Kind { PICK, SHOVEL, AXE, SWORD }
    public final Kind kind;
    public final boolean awakened;
    public KamiToolItem(Properties properties, Kind kind, boolean awakened) { super(properties); this.kind = kind; this.awakened = awakened; }
    /** Legacy item hook; modern versions receive the same value through their material component. */
    public int getEnchantmentValue() { return 25; }
    public boolean effective(BlockState state) {
        return switch (kind) {
            case PICK -> state.is(BlockTags.MINEABLE_WITH_PICKAXE);
            case SHOVEL -> state.is(BlockTags.MINEABLE_WITH_SHOVEL);
            case AXE -> state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.LEAVES) || state.is(Blocks.COBWEB);
            case SWORD -> state.is(Blocks.COBWEB);
        };
    }
    @Override public float getDestroySpeed(ItemStack stack, BlockState state) { return awakened && kind == Kind.PICK && state.is(Blocks.BEDROCK) ? Float.MAX_VALUE : effective(state) ? 10F : 1F; }
    @Override public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) { return effective(state); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (awakened && player.isShiftKeyDown()) {
            int mode = (StackData.integer(stack, "tt_mode") + 1) % 3;
            StackData.integer(stack, "tt_mode", mode);
            KamiVersion.toolMode(stack, mode);
            KamiVersion.status(player, Component.translatable("message.thaumictinkerer.tool_mode." + kind.name().toLowerCase() + "." + mode));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    public void addTooltip(ItemStack stack, List<Component> tooltip) {
        if (awakened) tooltip.add(Component.translatable("message.thaumictinkerer.tool_mode." + kind.name().toLowerCase() + "." + StackData.integer(stack, "tt_mode")));
    }
}
