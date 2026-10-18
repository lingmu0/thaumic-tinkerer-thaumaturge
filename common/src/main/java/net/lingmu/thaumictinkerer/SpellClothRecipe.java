/* Adapted from SpellClothRecipe by Vazkii; CC BY-NC-SA 3.0. */
package net.lingmu.thaumictinkerer;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

public final class SpellClothRecipe extends CustomRecipe {
    private static final TagKey<Item> PROTECTED = TagKey.create(Registries.ITEM, TTContent.id("cannot_disenchant"));
    public SpellClothRecipe(CraftingBookCategory category) { super(category); }

    private boolean enchanted(ItemStack stack) {
        return !stack.is(TTContent.SPELL_CLOTH.get()) && !stack.is(PROTECTED)
            && !stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int cloth = 0, target = 0;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) continue;
            if (stack.is(TTContent.SPELL_CLOTH.get())) cloth++;
            else if (enchanted(stack)) target++;
            else return false;
        }
        return cloth == 1 && target == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (enchanted(stack)) {
                ItemStack result = stack.copyWithCount(1);
                result.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> result = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.is(TTContent.SPELL_CLOTH.get()) && stack.getDamageValue() + 1 < stack.getMaxDamage()) {
                ItemStack remainder = stack.copyWithCount(1);
                remainder.setDamageValue(remainder.getDamageValue() + 1);
                result.set(slot, remainder);
            } else if (!stack.is(TTContent.SPELL_CLOTH.get()) && stack.hasCraftingRemainingItem()) {
                result.set(slot, stack.getCraftingRemainingItem());
            }
        }
        return result;
    }

    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 2; }
    @Override public RecipeSerializer<?> getSerializer() { return TTContent.SPELL_CLOTH_SERIALIZER.get(); }
}
