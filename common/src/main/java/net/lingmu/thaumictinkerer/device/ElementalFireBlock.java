/* Thaumic Tinkerer elemental transmutations, CC BY-NC-SA 3.0. */
package net.lingmu.thaumictinkerer.device;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.world.level.BlockGetter;

public final class ElementalFireBlock extends Block {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
    private final String aspect;
    public ElementalFireBlock(String aspect, Properties properties) {
        super(properties.randomTicks()); this.aspect = aspect;
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(AGE); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return Block.box(0,0,0,16,1,16); }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!VersionDevices.fireEnabled(level, pos)) return;
        int age = state.getValue(AGE);
        if (age >= 15) { level.removeBlock(pos, false); return; }
        level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        for (Direction direction : Direction.values()) {
            BlockPos target = pos.relative(direction);
            BlockState before = level.getBlockState(target);
            Block after = transmute(level, before);
            // Do not erase block inventories as part of a transmutation.
            if (after != null && (!before.hasBlockEntity() || before.is(Blocks.SPAWNER))) {
                level.setBlockAndUpdate(target, after.defaultBlockState());
                if (age < 8 && random.nextInt(3) == 0 && level.isEmptyBlock(target.above()))
                    level.setBlockAndUpdate(target.above(), defaultBlockState().setValue(AGE, age + 2));
            }
        }
    }
    private Block transmute(ServerLevel level, BlockState state) {
        Block b = state.getBlock();
        return switch (aspect) {
            case "aqua" -> b == Blocks.NETHERRACK ? Blocks.SNOW_BLOCK : b == Blocks.LAVA ? Blocks.OBSIDIAN
                : b == Blocks.SAND || b == Blocks.SOUL_SAND || b == Blocks.GLOWSTONE ? Blocks.ICE : null;
            case "aer" -> state.is(BlockTags.LOGS) || b == Blocks.DIRT ? Blocks.SAND : state.is(BlockTags.LEAVES) ? Blocks.SANDSTONE
                : b == Blocks.ICE ? Blocks.GLASS : b == Blocks.WATER ? Blocks.CAKE : null;
            case "terra" -> b == Blocks.SPAWNER ? Blocks.IRON_BLOCK : b == Blocks.GRAVEL ? Blocks.CLAY : b == Blocks.NETHER_BRICKS ? Blocks.OAK_PLANKS
                : b == Blocks.NETHER_BRICK_FENCE ? Blocks.OAK_FENCE : b == Blocks.NETHER_BRICK_STAIRS ? Blocks.OAK_STAIRS
                : b == Blocks.CACTUS ? Blocks.OAK_LOG : b == Blocks.SNOW ? Blocks.SHORT_GRASS
                : b == Blocks.SAND || b == Blocks.STONE || b == Blocks.COBBLESTONE || b == Blocks.GLASS
                    || state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.is(BlockTags.PLANKS) ? Blocks.DIRT : null;
            case "ignis" -> b == Blocks.GRASS_BLOCK || b == Blocks.DIRT ? Blocks.NETHERRACK
                : b == Blocks.SAND || b == Blocks.GRAVEL ? Blocks.SOUL_SAND : b == Blocks.CLAY ? Blocks.GLOWSTONE
                : b == Blocks.LAVA ? Blocks.WATER : state.is(BlockTags.COAL_ORES) || state.is(BlockTags.IRON_ORES)
                    || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.EMERALD_ORES)
                    || state.is(BlockTags.LAPIS_ORES) || state.is(BlockTags.REDSTONE_ORES) || b == Blocks.GOLD_BLOCK ? Blocks.NETHER_QUARTZ_ORE
                : b == Blocks.WHEAT || b == Blocks.CARROTS || b == Blocks.POTATOES ? Blocks.NETHER_WART
                : state.is(BlockTags.SMALL_FLOWERS) && b != Blocks.DANDELION ? Blocks.BROWN_MUSHROOM : null;
            case "ordo" -> state.is(BlockTags.IRON_ORES) ? Blocks.IRON_BLOCK : state.is(BlockTags.GOLD_ORES) ? Blocks.GOLD_BLOCK
                : state.is(BlockTags.COAL_ORES) ? Blocks.COAL_BLOCK : state.is(BlockTags.DIAMOND_ORES) ? Blocks.DIAMOND_BLOCK
                : state.is(BlockTags.EMERALD_ORES) ? Blocks.EMERALD_BLOCK : state.is(BlockTags.LAPIS_ORES) ? Blocks.LAPIS_BLOCK
                : state.is(BlockTags.REDSTONE_ORES) ? Blocks.REDSTONE_BLOCK : b == Blocks.NETHER_QUARTZ_ORE ? Blocks.QUARTZ_BLOCK
                : state.is(BlockTags.COPPER_ORES) ? Blocks.COPPER_BLOCK : commonStorageBlock(level, state);
            case "perditio" -> b instanceof ElementalFireBlock ? Blocks.FIRE : null;
            default -> null;
        };
    }
    /** The old OreDictionary ore-to-storage-block integration, expressed with conventional NeoForge tags. */
    private Block commonStorageBlock(ServerLevel level, BlockState state) {
        for (String material : VersionDevices.oreMaterials(state)) {
            var storage = level.registryAccess().lookupOrThrow(Registries.BLOCK).get(TagKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/" + material))).orElse(null);
            if (storage != null) return storage.stream().findFirst().map(holder -> holder.value()).orElse(null);
        }
        return null;
    }
}
