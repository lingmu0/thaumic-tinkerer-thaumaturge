/* Derived from Thaumic Tinkerer 1.7.10, CC BY-NC-SA 3.0. */
package net.lingmu.thaumictinkerer.kami;

import java.util.List;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.content.wands.ItemWandCap;
import com.leclowndu93150.thaumaturge.content.wands.ItemWandRod;
import net.lingmu.thaumictinkerer.TTContent;
import net.lingmu.thaumictinkerer.ThaumicTinkerer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.*;

public final class KamiContent {
    public static final DeferredRegister<WandCap> CAPS = DeferredRegister.create(WandCap.REGISTRY_KEY, ThaumicTinkerer.MODID);
    public static final DeferredRegister<WandRod> RODS = DeferredRegister.create(WandRod.REGISTRY_KEY, ThaumicTinkerer.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ThaumicTinkerer.MODID);
    public static final DeferredRegister<net.minecraft.world.level.levelgen.feature.Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, ThaumicTinkerer.MODID);
    public static final DeferredHolder<net.minecraft.world.level.levelgen.feature.Feature<?>, KamiOreFeature> BEDROCK_ORES = FEATURES.register("bedrock_ores", KamiOreFeature::new);
    public static final DeferredHolder<WandCap, WandCap> ICHOR_WAND_CAP = CAPS.register("ichor", () -> new WandCap(0.8F, List.of(), 0F, 10, TTContent.id("textures/models/wand_cap_ichor.png")));
    public static final DeferredHolder<WandRod, WandRod> ICHORCLOTH_WAND_ROD = RODS.register("ichorcloth", () -> new WandRod(1000, 10, TTContent.id("textures/models/wand_rod_ichorcloth.png"), null, true, false, false, TTContent.id("ichorcloth_rod")));
    public static final DeferredItem<ItemWandCap> ICHOR_CAP = TTContent.item("ichor_cap", p -> new ItemWandCap(p, ICHOR_WAND_CAP), epic());
    public static final DeferredItem<ItemWandRod> ICHORCLOTH_ROD = TTContent.item("ichorcloth_rod", p -> new ItemWandRod(p, ICHORCLOTH_WAND_ROD), epic());
    public static final DeferredItem<KamiArmorItem> ICHOR_HELM = armor("ichor_helm", 0, false);
    public static final DeferredItem<KamiArmorItem> ICHOR_CHEST = armor("ichor_chest", 1, false);
    public static final DeferredItem<KamiArmorItem> ICHOR_LEGS = armor("ichor_legs", 2, false);
    public static final DeferredItem<KamiArmorItem> ICHOR_BOOTS = armor("ichor_boots", 3, false);
    public static final DeferredItem<KamiArmorItem> ICHOR_HELM_GEM = armor("ichor_helm_gem", 0, true);
    public static final DeferredItem<KamiArmorItem> ICHOR_CHEST_GEM = armor("ichor_chest_gem", 1, true);
    public static final DeferredItem<KamiArmorItem> ICHOR_LEGS_GEM = armor("ichor_legs_gem", 2, true);
    public static final DeferredItem<KamiArmorItem> ICHOR_BOOTS_GEM = armor("ichor_boots_gem", 3, true);
    public static final DeferredItem<KamiToolItem> ICHOR_PICK = tool("ichor_pick", KamiToolItem.Kind.PICK, false);
    public static final DeferredItem<KamiToolItem> ICHOR_SHOVEL = tool("ichor_shovel", KamiToolItem.Kind.SHOVEL, false);
    public static final DeferredItem<KamiToolItem> ICHOR_AXE = tool("ichor_axe", KamiToolItem.Kind.AXE, false);
    public static final DeferredItem<KamiToolItem> ICHOR_SWORD = tool("ichor_sword", KamiToolItem.Kind.SWORD, false);
    public static final DeferredItem<KamiToolItem> ICHOR_PICK_GEM = tool("ichor_pick_gem", KamiToolItem.Kind.PICK, true);
    public static final DeferredItem<KamiToolItem> ICHOR_SHOVEL_GEM = tool("ichor_shovel_gem", KamiToolItem.Kind.SHOVEL, true);
    public static final DeferredItem<KamiToolItem> ICHOR_AXE_GEM = tool("ichor_axe_gem", KamiToolItem.Kind.AXE, true);
    public static final DeferredItem<KamiToolItem> ICHOR_SWORD_GEM = tool("ichor_sword_gem", KamiToolItem.Kind.SWORD, true);
    public static final DeferredItem<KamiUtilityItem> CAT_AMULET = utility("cat_amulet", KamiUtilityItem.Kind.CAT_AMULET);
    public static final DeferredItem<KamiUtilityItem> BLOCK_TALISMAN = utility("block_talisman", KamiUtilityItem.Kind.BLOCK_TALISMAN);
    public static final DeferredItem<KamiUtilityItem> PLACEMENT_MIRROR = utility("placement_mirror", KamiUtilityItem.Kind.PLACEMENT_MIRROR);
    public static final DeferredItem<KamiUtilityItem> PROTOCLAY = utility("protoclay", KamiUtilityItem.Kind.PROTOCLAY);
    public static final DeferredItem<KamiPearl> SKY_PEARL = TTContent.item("sky_pearl", KamiPearl::new, epic().stacksTo(1));
    public static final DeferredItem<KamiPouchItem> ICHOR_POUCH = TTContent.item("ichor_pouch", KamiPouchItem::new, epic().stacksTo(1));
    public static final DeferredBlock<KamiGateBlock> WARP_GATE = TTContent.block("warp_gate", KamiGateBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN).strength(5F, 2000F));
    public static final DeferredBlock<Block> BEDROCK_PORTAL = TTContent.block("bedrock_portal", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK).lightLevel(s -> 12).noLootTable());
    public static final DeferredBlock<KamiLightBlock> KAMI_LIGHT = TTContent.BLOCKS.register("kami_light", () -> new KamiLightBlock(net.lingmu.thaumictinkerer.VersionProperties.block("kami_light", BlockBehaviour.Properties.of().noCollission().noOcclusion().replaceable().air().lightLevel(s -> 15).noLootTable())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<KamiGateBlockEntity>> GATE_ENTITY = BLOCK_ENTITIES.register("warp_gate", KamiVersion::gateEntityType);

    private static Item.Properties epic() { return new Item.Properties().rarity(Rarity.EPIC).fireResistant(); }
    private static DeferredItem<KamiArmorItem> armor(String id, int slot, boolean awakened) { return TTContent.item(id, p -> new KamiArmorItem(p, slot, awakened), epic()); }
    private static DeferredItem<KamiToolItem> tool(String id, KamiToolItem.Kind kind, boolean awakened) { return TTContent.item(id, p -> new KamiToolItem(KamiVersion.toolProperties(p, kind), kind, awakened), epic().stacksTo(1)); }
    private static DeferredItem<KamiUtilityItem> utility(String id, KamiUtilityItem.Kind kind) { return TTContent.item(id, p -> new KamiUtilityItem(p, kind), epic().stacksTo(1)); }
    public static void register(IEventBus bus) {
        CAPS.register(bus); RODS.register(bus); BLOCK_ENTITIES.register(bus); FEATURES.register(bus);
        NeoForge.EVENT_BUS.register(KamiEvents.class);
    }
    private KamiContent() {}
}
