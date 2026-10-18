/* Adapted from Thaumic Tinkerer by Vazkii, pixlepix, nekosune and contributors.
 * CC BY-NC-SA 3.0; see LICENSE. */
package net.lingmu.thaumictinkerer;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ThaumicTinkerer.MODID)
public final class ThaumicTinkerer {
    public static final String MODID = "thaumictinkerer";

    public ThaumicTinkerer(IEventBus bus) {
        net.lingmu.thaumictinkerer.device.DeviceContent.register(bus);
        net.lingmu.thaumictinkerer.focus.FocusContent.register(bus);
        net.lingmu.thaumictinkerer.kami.KamiContent.register(bus);
        TTContent.ITEMS.register(bus);
        TTContent.BLOCKS.register(bus);
        TTContent.TABS.register(bus);
        TTContent.SERIALIZERS.register(bus);
        NeoForge.EVENT_BUS.addListener(DimensionalShards::onDrops);
        net.lingmu.thaumictinkerer.enchantment.TinkererEnchantments.register();
    }
}
