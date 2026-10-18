package net.lingmu.thaumictinkerer.device;

import net.lingmu.thaumictinkerer.StackData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.network.chat.Component;

public final class ConnectorItem extends Item {
    public ConnectorItem(Properties properties) { super(properties); }
    @Override public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        var player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        var stack = context.getItemInHand();
        var pos = context.getClickedPos();
        String dimension = context.getLevel().dimension().location().toString();
        if (!player.isShiftKeyDown() && context.getLevel().getBlockEntity(pos) instanceof MachineBlockEntity machine
            && StackData.integer(stack, "tt_bound") != 0) {
            BlockPos target = new BlockPos(StackData.integer(stack, "tt_x"), StackData.integer(stack, "tt_y"), StackData.integer(stack, "tt_z"));
            if (!dimension.equals(StackData.string(stack, "tt_dimension")) || target.equals(pos) || !target.closerThan(pos, 33)) {
                player.displayClientMessage(Component.translatable("message.thaumictinkerer.device.bind_range"), true);
                return InteractionResult.FAIL;
            }
            machine.setTarget(target);
            machine.owner = player.getUUID();
            machine.setChanged();
            player.displayClientMessage(Component.translatable("message.thaumictinkerer.device.linked", target.toShortString()), true);
        } else {
            StackData.integer(stack, "tt_bound", 1);
            StackData.integer(stack, "tt_x", pos.getX()); StackData.integer(stack, "tt_y", pos.getY()); StackData.integer(stack, "tt_z", pos.getZ());
            StackData.string(stack, "tt_dimension", dimension);
            player.displayClientMessage(Component.translatable("message.thaumictinkerer.device.bound", pos.toShortString()), true);
        }
        return InteractionResult.SUCCESS;
    }
}
