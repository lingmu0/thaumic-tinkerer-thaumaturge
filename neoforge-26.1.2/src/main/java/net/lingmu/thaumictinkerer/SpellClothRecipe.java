/* Adapted from SpellClothRecipe by Vazkii; CC BY-NC-SA 3.0. */
package net.lingmu.thaumictinkerer;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

public final class SpellClothRecipe extends CustomRecipe {
    private static final TagKey<Item> PROTECTED = TagKey.create(Registries.ITEM, TTContent.id("cannot_disenchant"));
    private boolean enchanted(ItemStack stack) {
        return !stack.is(TTContent.SPELL_CLOTH.get()) && !stack.is(PROTECTED)
            && !stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();
    }
    @Override
    public boolean matches(CraftingInput input, Level level) {
        int cloth = 0, target = 0;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;
            if (stack.is(TTContent.SPELL_CLOTH.get())) cloth++;
            else if (enchanted(stack)) target++;
            else return false;
        }
        return cloth == 1 && target == 1;
    }
    @Override
    public ItemStack assemble(CraftingInput input) {
        for (ItemStack stack : input.items()) {
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
        NonNullList<ItemStack> remaining = CraftingRecipe.defaultCraftingReminder(input);
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.is(TTContent.SPELL_CLOTH.get())) {
                ItemStack cloth = ItemStack.EMPTY;
                if (stack.getDamageValue() + 1 < stack.getMaxDamage()) {
                    cloth = stack.copyWithCount(1);
                    cloth.setDamageValue(cloth.getDamageValue() + 1);
                }
                remaining.set(slot, cloth);
            }
        }
        return remaining;
    }
    @Override public RecipeSerializer<? extends CustomRecipe> getSerializer() { return TTContent.SPELL_CLOTH_SERIALIZER.get(); }
}
