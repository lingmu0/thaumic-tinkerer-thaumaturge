package net.lingmu.thaumictinkerer.focus;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

public final class InfusedPotionItem extends Item {
    private final String aspect;
    public InfusedPotionItem(Properties properties,String aspect){super(properties);this.aspect=aspect;}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand){player.startUsingItem(hand);return InteractionResult.CONSUME;}
    @Override public int getUseDuration(ItemStack stack,LivingEntity entity){return 32;}
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack){return ItemUseAnimation.DRINK;}
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity entity){
        if(!level.isClientSide())TinkerPotions.apply(entity,aspect);
        if(entity instanceof Player player&&player.getAbilities().instabuild)return stack;
        stack.shrink(1);return stack.isEmpty()?new ItemStack(Items.GLASS_BOTTLE):stack;
    }
}
