/* Adapted from Thaumic Tinkerer 1.7.10 by Vazkii, pixlepix and nekosune. CC BY-NC-SA 3.0. */
package net.lingmu.thaumictinkerer.device;

import java.util.*;
import net.lingmu.thaumictinkerer.TTContent;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class DeviceContent {
    public enum Kind {
        REPAIRER("repairer"), FUNNEL("funnel"), MAGNET("magnet"), MOB_MAGNET("mob_magnet"),
        ENCHANTER("enchanter"), INTERFACE("transvector_interface"), DISLOCATOR("transvector_dislocator"),
        REMOTE_PLACER("remote_placer"), GOLEM_CONNECTOR("golem_connector"), TABLET("animation_tablet"),
        ANALYZER("aspect_analyzer"), PLATFORM("platform"), CAMO("camo"),
        MOBILIZER("mobilizer"), RELAY("mobilizer_relay"), SUMMON("summon_tablet"), FORCEFIELD("forcefield");
        public final String id;
        Kind(String id) { this.id = id; }
    }
    public static final Map<Kind, DeferredBlock<VersionDeviceBlock>> MACHINES = new EnumMap<>(Kind.class);
    public static final Map<String, DeferredBlock<ElementalFireBlock>> FIRES = new LinkedHashMap<>();
    public static final Map<String, DeferredBlock<InfusedCropBlock>> CROPS = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<Item>> GRAINS = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<SeedItem>> SEEDS = new LinkedHashMap<>();
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "thaumictinkerer");
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VersionMachine>> MACHINE;
    public static final DeferredItem<ConnectorItem> CONNECTOR = TTContent.item("connector", ConnectorItem::new, new Item.Properties().stacksTo(1));
    static {
        for (Kind kind : Kind.values()) {
            BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(2.5F).noOcclusion();
            if (kind == Kind.FORCEFIELD) properties.strength(-1, 3600000).noLootTable();
            MACHINES.put(kind, TTContent.block(kind.id, p -> new VersionDeviceBlock(kind, p), properties));
        }
        TTContent.ENTRIES.remove("forcefield");
        for (String aspect : List.of("aer", "terra", "ignis", "aqua", "ordo", "perditio")) {
            FIRES.put(aspect, TTContent.block("fire_" + aspect, p -> new ElementalFireBlock(aspect, p),
                BlockBehaviour.Properties.ofFullCopy(Blocks.FIRE).lightLevel(s -> 12).noCollission().noLootTable()));
        }
        for (String aspect : List.of("aer", "terra", "ignis", "aqua")) {
            CROPS.put(aspect, TTContent.BLOCKS.register("infused_crop_" + aspect,
                () -> new InfusedCropBlock(aspect, net.lingmu.thaumictinkerer.VersionProperties.block("infused_crop_" + aspect,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)))));
            GRAINS.put(aspect, TTContent.item("infused_grain_" + aspect, Item::new, new Item.Properties()));
            SEEDS.put(aspect, TTContent.item("infused_seeds_" + aspect, p -> new SeedItem(aspect, p), new Item.Properties()));
        }
        MACHINE = BLOCK_ENTITIES.register("device", () -> VersionDevices.type(MACHINES.values().stream().map(DeferredBlock::get).toArray(Block[]::new)));
    }
    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
        bus.addListener(VersionDevices::capabilities);
    }
    private DeviceContent() {}
}
