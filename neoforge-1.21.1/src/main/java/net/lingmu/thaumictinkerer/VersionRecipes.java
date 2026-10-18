package net.lingmu.thaumictinkerer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
final class VersionRecipes {
    static RecipeSerializer<SpellClothRecipe> spellCloth() { return new SimpleCraftingRecipeSerializer<>(SpellClothRecipe::new); }
}
