/* Adapted from ItemXPTalisman, original Thaumic Tinkerer; CC BY-NC-SA 3.0. */
package net.lingmu.thaumictinkerer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ExperienceOrb;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import net.lingmu.thaumictinkerer.mixin.ExperienceOrbAccessor;

public final class ExperienceTalismanItem extends Item implements ICurioItem {
    static final int CAPACITY = 1500;
    static final int BOTTLE_COST = 10;
    public ExperienceTalismanItem(Properties properties) { super(properties); }

    @Override
    public void curioTick(SlotContext context, ItemStack stack) {
        var entity = context.entity();
        if (entity.level().isClientSide || !StackData.enabled(stack)) return;
        int xp = StackData.integer(stack, "tt_xp");
        int previous = xp;
        for (ExperienceOrb orb : entity.level().getEntitiesOfClass(ExperienceOrb.class, entity.getBoundingBox().inflate(3))) {
            if (!orb.isAlive() || orb.getValue() <= 0) continue;
            ExperienceOrbAccessor access = (ExperienceOrbAccessor) orb;
            int consumed = ExperienceTransfer.count(xp, CAPACITY, orb.getValue(), access.tt$getCount());
            if (consumed <= 0) continue;
            xp += consumed * orb.getValue();
            int remaining = access.tt$getCount() - consumed;
            if (remaining == 0) orb.discard(); else access.tt$setCount(remaining);
        }
        if (xp != previous) StackData.integer(stack, "tt_xp", xp);
        if (xp >= CAPACITY) StackData.integer(stack, "tt_enabled", 0);
    }

    @Override public boolean isBarVisible(ItemStack stack) {return StackData.integer(stack,"tt_xp")>0;}
    @Override public int getBarWidth(ItemStack stack) {return Math.max(0,Math.min(13,Math.round(13F*StackData.integer(stack,"tt_xp")/CAPACITY)));}
    @Override public int getBarColor(ItemStack stack) {return 0x80E840;}

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                if (StackData.integer(stack, "tt_xp") < CAPACITY) StackData.toggle(stack);
                VersionMessages.status(player, Component.translatable(StackData.enabled(stack) ? "ttmisc.absorbing" : "ttmisc.notAbsorbing"));
            } else if (StackData.integer(stack, "tt_xp") >= BOTTLE_COST) {
                for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                    ItemStack bottle = player.getInventory().getItem(slot);
                    if (bottle.is(Items.GLASS_BOTTLE)) {
                        bottle.shrink(1);
                        ItemStack output = new ItemStack(Items.EXPERIENCE_BOTTLE);
                        if (!player.getInventory().add(output)) player.drop(output, false);
                        StackData.integer(stack, "tt_xp", StackData.integer(stack, "tt_xp") - BOTTLE_COST);
                        break;
                    }
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
