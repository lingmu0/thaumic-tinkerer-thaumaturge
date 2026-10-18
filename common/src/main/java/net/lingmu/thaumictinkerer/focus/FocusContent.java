/* Thaumic Tinkerer modern adaptation; original work by Vazkii and the TT team, CC BY-NC-SA 3.0. */
package net.lingmu.thaumictinkerer.focus;

import com.leclowndu93150.thaumaturge.api.spell.CastStyle;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPartKind;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItem;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.lingmu.thaumictinkerer.TTContent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FocusContent {
    private static final DeferredRegister<SpellBehaviorType<?>> BEHAVIORS = DeferredRegister.create(SpellBehaviorType.REGISTRY_KEY, "thaumictinkerer");
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<TinkerEffect>> EFFECT = BEHAVIORS.register("tinkering", () -> new SpellBehaviorType<>(SpellPartKind.EFFECT, TinkerEffect.CODEC));
    public static final DeferredItem<FocusItem> FOCUS_SMELT = focus("smelt", CastStyle.CHANNELED, false);
    public static final DeferredItem<FocusItem> FOCUS_FLIGHT = focus("flight", CastStyle.INSTANT, false);
    public static final DeferredItem<FocusItem> FOCUS_DEFLECT = focus("deflect", CastStyle.CHANNELED, false);
    public static final DeferredItem<FocusItem> FOCUS_HEAL = focus("heal", CastStyle.CHANNELED, false);
    public static final DeferredItem<FocusItem> FOCUS_ENDER_CHEST = focus("ender_chest", CastStyle.INSTANT, false);
    public static final DeferredItem<FocusItem> FOCUS_TELEKINESIS = focus("telekinesis", CastStyle.CHANNELED, false);
    public static final DeferredItem<FocusItem> FOCUS_DISLOCATION = focus("dislocation", CastStyle.INSTANT, false);
    public static final DeferredItem<FocusItem> FOCUS_SHADOWBEAM = focus("shadowbeam", CastStyle.CHANNELED, true);
    public static final DeferredItem<FocusItem> FOCUS_XP_DRAIN = focus("xp_drain", CastStyle.CHANNELED, true);
    public static final DeferredItem<FocusItem> FOCUS_RECALL = focus("recall", CastStyle.CHARGED, true);

    public static final DeferredHolder<Block, GasBlock> LIGHT_GAS = gas("light_gas", false, false);
    public static final DeferredHolder<Block, GasBlock> SHADOW_GAS = gas("shadow_gas", true, false);
    public static final DeferredHolder<Block, GasBlock> NITOR_GAS = gas("nitor_gas", false, true);
    public static final DeferredItem<UtilityItem> BRIGHT_NITOR = utility("bright_nitor", 1);
    public static final DeferredItem<UtilityItem> GASEOUS_LIGHT = utility("gaseous_light", 64);
    public static final DeferredItem<UtilityItem> GASEOUS_TENEBRAE = utility("gaseous_tenebrae", 64);
    public static final DeferredItem<UtilityItem> GAS_REMOVER = utility("gas_remover", 1);
    public static final DeferredItem<BloodSwordItem> BLOOD_SWORD = TTContent.item("blood_sword", BloodSwordItem::new, new Item.Properties().durability(950).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<RevealingHelmItem> REVEALING_HELM = TTContent.item("revealing_helm", RevealingHelmItem::new, new Item.Properties().durability(500).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> INFUSED_INKWELL = TTContent.item("infused_inkwell", Item::new, new Item.Properties().durability(800).rarity(Rarity.UNCOMMON).component(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
    public static final DeferredItem<UtilityItem> SHARE_BOOK = utility("share_book", 1);
    public static final DeferredItem<SoulMouldItem> SOUL_MOULD = TTContent.item("soul_mould", SoulMouldItem::new, new Item.Properties().stacksTo(1));

    private static DeferredItem<FocusItem> focus(String name, CastStyle style, boolean kami) {
        ResourceKey<SpellPart> key = ResourceKey.create(SpellPart.REGISTRY_KEY, TTContent.id(name));
        Spell spell = new Spell(style, SpellNode.of(Spell.ORIGIN).then(SpellNode.of(key)));
        return TTContent.item("focus_" + name, p -> new FocusItem(p.component(TTDataComponents.SPELL.get(), spell)), new Item.Properties().stacksTo(1).rarity(kami ? Rarity.EPIC : Rarity.UNCOMMON));
    }

    private static DeferredItem<UtilityItem> utility(String name, int size) {
        return TTContent.item(name, p -> new UtilityItem(p, name), new Item.Properties().stacksTo(size).rarity(Rarity.UNCOMMON));
    }

    private static DeferredHolder<Block, GasBlock> gas(String name, boolean shadow, boolean temporary) {
        // These are airlike effect blocks, not inventory block items.
        return TTContent.BLOCKS.register(name, () -> VersionFocusBridge.gas(name, shadow, temporary));
    }

    public static void register(IEventBus bus) {
        MobAspects.register();
        TinkerPotions.register(bus);
        BEHAVIORS.register(bus);
        NeoForge.EVENT_BUS.addListener(FocusEvents::playerTick);
        NeoForge.EVENT_BUS.addListener(FocusEvents::bloodDrops);
        NeoForge.EVENT_BUS.addListener(FocusEvents::bloodAttack);
        NeoForge.EVENT_BUS.addListener(FocusEvents::beforeCast);
    }
    private FocusContent() {}
}
