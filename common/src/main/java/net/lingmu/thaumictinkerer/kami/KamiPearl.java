package net.lingmu.thaumictinkerer.kami;

import java.util.List;
import net.lingmu.thaumictinkerer.StackData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class KamiPearl extends Item {
    public KamiPearl(Properties properties) { super(properties); }
    public static boolean bound(ItemStack stack) { return StackData.integer(stack, "tt_gate_bound") == 1; }
    public static BlockPos destination(ItemStack stack) { return new BlockPos(StackData.integer(stack, "tt_gate_x"), StackData.integer(stack, "tt_gate_y"), StackData.integer(stack, "tt_gate_z")); }
    public static void bind(ItemStack stack, Level level, BlockPos pos) {
        StackData.integer(stack, "tt_gate_bound", 1);
        StackData.string(stack, "tt_gate_dimension", level.dimension().location().toString());
        StackData.integer(stack, "tt_gate_x", pos.getX()); StackData.integer(stack, "tt_gate_y", pos.getY()); StackData.integer(stack, "tt_gate_z", pos.getZ());
    }
    public static boolean teleport(ServerPlayer player, ItemStack pearl) {
        if (!bound(pearl) || !StackData.string(pearl, "tt_gate_dimension").equals(player.level().dimension().location().toString())) return false;
        BlockPos pos = destination(pearl);
        if (!player.level().getWorldBorder().isWithinBounds(pos)) return false;
        if (!(player.level().getBlockEntity(pos) instanceof KamiGateBase gate) || gate.locked) return false;
        if (!player.level().getBlockState(pos.above()).getCollisionShape(player.level(), pos.above()).isEmpty() || !player.level().getBlockState(pos.above(2)).getCollisionShape(player.level(), pos.above(2)).isEmpty()) return false;
        KamiVersion.teleport(player, KamiVersion.level(player), pos.above());
        return true;
    }
    public static boolean recall(ServerPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(KamiContent.SKY_PEARL.get()) && bound(stack)) return teleport(player, stack);
        }
        return false;
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().getBlockState(context.getClickedPos()).is(KamiContent.WARP_GATE.get())) {
            if (!bound(context.getItemInHand()) && !context.getLevel().isClientSide) bind(context.getItemInHand(), context.getLevel(), context.getClickedPos());
            else if (bound(context.getItemInHand())) return InteractionResult.PASS;
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) StackData.integer(stack, "tt_gate_bound", 0);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    @Override public boolean isFoil(ItemStack stack) { return bound(stack); }
    public void addTooltip(ItemStack stack, List<Component> tooltip) {
        if (bound(stack)) { tooltip.add(Component.literal(StackData.string(stack, "tt_gate_dimension"))); tooltip.add(Component.literal(destination(stack).toShortString())); }
    }
}
