/* Adapted from ItemCleansingTalisman, original Thaumic Tinkerer; CC BY-NC-SA 3.0. */
package net.lingmu.thaumictinkerer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public final class CleansingTalismanItem extends Item implements ICurioItem {
    public CleansingTalismanItem(Properties properties) { super(properties); }
    @Override
    public void curioTick(SlotContext context, ItemStack stack) {
        var entity = context.entity();
        if (entity.level().isClientSide || entity.tickCount % 20 != 0 || !StackData.enabled(stack)) return;
        boolean removed = entity.isOnFire();
        if (removed) entity.clearFire();
        else {
            for (var effect : java.util.List.copyOf(entity.getActiveEffects())) {
                if (!effect.getEffect().value().isBeneficial()) {
                    entity.removeEffect(effect.getEffect());
                    removed = true;
                    break;
                }
            }
        }
        if (removed) {
            int damage = stack.getDamageValue() + 1;
            if (damage >= stack.getMaxDamage()) {
                stack.shrink(1);
                CuriosApi.broadcastCurioBreakEvent(context);
            } else stack.setDamageValue(damage);
        }
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player.isShiftKeyDown()) {
            StackData.toggle(stack);
            VersionMessages.status(player, Component.translatable(StackData.enabled(stack) ? "ttmisc.active" : "ttmisc.inactive"));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
