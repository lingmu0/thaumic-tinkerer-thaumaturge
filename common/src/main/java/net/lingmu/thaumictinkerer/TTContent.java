/* CC BY-NC-SA 3.0; see LICENSE and the original Thaumic Tinkerer credits. */
package net.lingmu.thaumictinkerer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.*;

public final class TTContent {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ThaumicTinkerer.MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ThaumicTinkerer.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ThaumicTinkerer.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ThaumicTinkerer.MODID);
    public static final Map<String, DeferredItem<? extends Item>> ENTRIES = new LinkedHashMap<>();

    public static final DeferredItem<Item> DARK_QUARTZ = item("dark_quartz", Item::new, new Item.Properties());
    public static final DeferredItem<Item> SPELL_CLOTH = item("spell_cloth", Item::new, new Item.Properties().durability(35).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<ExperienceTalismanItem> XP_TALISMAN = item("xp_talisman", ExperienceTalismanItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<CleansingTalismanItem> CLEANSING_TALISMAN = item("cleansing_talisman", CleansingTalismanItem::new, new Item.Properties().durability(100).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> ICHOR = item("ichor", Item::new, new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredItem<Item> ICHOR_CLOTH = item("ichor_cloth", Item::new, new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredItem<Item> ICHORIUM = item("ichorium", Item::new, new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredItem<Item> ICHOR_NUGGET = item("ichor_nugget", Item::new, new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredItem<Item> NETHER_SHARD = item("nether_shard", Item::new, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> ENDER_SHARD = item("ender_shard", Item::new, new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final DeferredBlock<Block> DARK_QUARTZ_BLOCK = block("dark_quartz_block", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK));
    public static final DeferredBlock<Block> DARK_QUARTZ_CHISELED = block("dark_quartz_chiseled", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.CHISELED_QUARTZ_BLOCK));
    public static final DeferredBlock<Block> DARK_QUARTZ_PILLAR = block("dark_quartz_pillar", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_PILLAR));
    public static final DeferredBlock<SlabBlock> DARK_QUARTZ_SLAB = block("dark_quartz_slab", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_SLAB));
    public static final DeferredBlock<StairBlock> DARK_QUARTZ_STAIRS = block("dark_quartz_stairs", p -> new StairBlock(DARK_QUARTZ_BLOCK.get().defaultBlockState(), p), BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_STAIRS));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SpellClothRecipe>> SPELL_CLOTH_SERIALIZER = SERIALIZERS.register("spell_cloth", VersionRecipes::spellCloth);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.thaumictinkerer"))
        .icon(() -> new ItemStack(SPELL_CLOTH.get()))
        .displayItems((parameters, output) -> ENTRIES.values().forEach(entry -> output.accept(entry.get())))
        .build());

    public static <T extends Item> DeferredItem<T> item(String name, Function<Item.Properties, T> factory, Item.Properties properties) {
        DeferredItem<T> result = ITEMS.register(name, () -> factory.apply(VersionProperties.item(name, properties)));
        ENTRIES.put(name, result);
        return result;
    }

    public static <T extends Block> DeferredBlock<T> block(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
        DeferredBlock<T> result = BLOCKS.register(name, () -> factory.apply(VersionProperties.block(name, properties)));
        item(name, p -> new BlockItem(result.get(), p), new Item.Properties());
        return result;
    }

    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(ThaumicTinkerer.MODID, path); }
    private TTContent() {}
}
