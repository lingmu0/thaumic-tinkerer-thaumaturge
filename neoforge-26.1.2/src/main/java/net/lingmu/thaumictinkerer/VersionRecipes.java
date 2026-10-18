package net.lingmu.thaumictinkerer;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
final class VersionRecipes {
    static RecipeSerializer<SpellClothRecipe> spellCloth() {
        var recipe = new SpellClothRecipe();
        return new RecipeSerializer<>(MapCodec.unit(recipe), StreamCodec.unit(recipe));
    }
}
