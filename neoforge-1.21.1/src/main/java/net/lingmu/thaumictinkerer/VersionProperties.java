package net.lingmu.thaumictinkerer;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class VersionProperties {
    public static Item.Properties item(String name, Item.Properties properties) { return properties; }
    public static BlockBehaviour.Properties block(String name, BlockBehaviour.Properties properties) { return properties; }
}
