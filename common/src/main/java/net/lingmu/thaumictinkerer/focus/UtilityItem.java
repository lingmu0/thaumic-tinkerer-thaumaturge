package net.lingmu.thaumictinkerer.focus;

import net.lingmu.thaumictinkerer.StackData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

public class UtilityItem extends Item {
    private final String kind;
    public UtilityItem(Properties properties, String kind) { super(properties); this.kind = kind; }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (kind.equals("gaseous_light") || kind.equals("gaseous_tenebrae")) {
                BlockPos pos = player.blockPosition().above();
                if (level.getBlockState(pos).canBeReplaced() && level.mayInteract(player, pos)) {
                    GasBlock gas = kind.equals("gaseous_light") ? FocusContent.LIGHT_GAS.get() : FocusContent.SHADOW_GAS.get();
                    level.setBlock(pos, gas.defaultBlockState().setValue(GasBlock.RANGE, 4), 3);
                    level.scheduleTick(pos, gas, 10);
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                }
            } else if (kind.equals("gas_remover") && player.isShiftKeyDown()) {
                for (BlockPos pos : BlockPos.betweenClosed(player.blockPosition().offset(-3,-3,-3), player.blockPosition().offset(2,2,2))) {
                    if (level.getBlockState(pos).getBlock() instanceof GasBlock && level.mayInteract(player,pos)) level.removeBlock(pos,false);
                }
            } else if (kind.equals("share_book")) {
                VersionFocusBridge.shareResearch(player,stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
