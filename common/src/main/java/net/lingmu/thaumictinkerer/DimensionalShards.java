/* Adapted from DimensionalShardDropHandler by Vazkii; CC BY-NC-SA 3.0. */
package net.lingmu.thaumictinkerer;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

final class DimensionalShards {
    static void onDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof Player)) return;
        var entity = event.getEntity();
        Level level = entity.level();
        if (level.isClientSide) return;
        Item drop = null;
        if (entity.getType() == EntityType.ENDERMAN && level.dimension() == Level.END && entity.getRandom().nextInt(32) == 0)
            drop = TTContent.ENDER_SHARD.get();
        if (entity.getType() == EntityType.ZOMBIFIED_PIGLIN && level.dimension() == Level.NETHER && entity.getRandom().nextInt(16) == 0)
            drop = TTContent.NETHER_SHARD.get();
        if (drop != null) event.getDrops().add(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), new ItemStack(drop)));
    }
    private DimensionalShards() {}
}
