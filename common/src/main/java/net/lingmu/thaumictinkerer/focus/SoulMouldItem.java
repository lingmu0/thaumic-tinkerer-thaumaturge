package net.lingmu.thaumictinkerer.focus;

import net.lingmu.thaumictinkerer.StackData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class SoulMouldItem extends Item {
    public SoulMouldItem(Properties properties) { super(properties); }
    public static ResourceLocation pattern(ItemStack stack) {
        if (!stack.is(FocusContent.SOUL_MOULD.get())) return null;
        return ResourceLocation.tryParse(StackData.string(stack,"tt_soul_pattern"));
    }
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!player.level().isClientSide && !(target instanceof Player)) {
            StackData.string(stack,"tt_soul_pattern",BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString());
            player.displayClientMessage(target.getDisplayName(),true);
        }
        return InteractionResult.SUCCESS;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack=player.getItemInHand(hand);
        if (!level.isClientSide && player.isShiftKeyDown()) StackData.string(stack,"tt_soul_pattern","");
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
