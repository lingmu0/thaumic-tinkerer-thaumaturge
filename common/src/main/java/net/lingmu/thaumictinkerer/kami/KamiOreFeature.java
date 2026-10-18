package net.lingmu.thaumictinkerer.kami;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** One weighted ore column per chunk, with the original 200 veins and original frequency weights. */
public final class KamiOreFeature extends Feature<NoneFeatureConfiguration> {
    private record Ore(String tag, int weight) {}
    private static final List<Ore> ORES = List.of(
        new Ore("aluminum",617),new Ore("amber",161),new Ore("apatite",269),new Ore("blue_topaz",238),new Ore("certus_quartz",234),new Ore("chimerite",270),new Ore("cinnabar",172),
        new Ore("coal",2648),new Ore("copper",603),new Ore("diamond",67),new Ore("emerald",48),new Ore("dark_iron",61),new Ore("gold",164),new Ore("infused_air",94),new Ore("infused_earth",35),new Ore("infused_entropy",53),new Ore("infused_fire",42),new Ore("infused_order",31),new Ore("infused_water",27),
        new Ore("iron",1503),new Ore("lapis",57),new Ore("lead",335),new Ore("nickel",72),new Ore("peridot",79),new Ore("redstone",364),new Ore("ruby",57),new Ore("saltpeter",768),new Ore("sapphire",70),new Ore("silver",416),new Ore("sulfur",105),new Ore("tin",507),new Ore("uranium",112),new Ore("vinteum",392));
    public KamiOreFeature() { super(NoneFeatureConfiguration.CODEC); }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        List<Block> choices = new ArrayList<>(); List<Integer> weights = new ArrayList<>(); int sum = 0;
        for (Ore ore : ORES) {
            var tag = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "ores/" + ore.tag));
            Block block = BuiltInRegistries.BLOCK.stream().filter(b -> b.defaultBlockState().is(tag)).findFirst().orElse(null);
            if (block != null) { choices.add(block); weights.add(ore.weight); sum += ore.weight; }
        }
        Block ore = Blocks.IRON_ORE;
        if (sum > 0) { int n = context.random().nextInt(sum); for (int i = 0; i < choices.size(); i++) { n -= weights.get(i); if (n < 0) { ore = choices.get(i); break; } } }
        int baseX = context.origin().getX(), baseZ = context.origin().getZ();
        for (int vein = 0; vein < 200; vein++) {
            BlockPos pos = new BlockPos(baseX, 6 + context.random().nextInt(245), baseZ);
            int size = context.random().nextInt(20);
            for (int i = 0; i < size; i++) {
                if (pos.getY() >= 6 && pos.getY() <= 250 && context.level().getBlockState(pos).is(Blocks.BEDROCK)) context.level().setBlock(pos, ore.defaultBlockState(), 2);
                BlockPos next = pos.relative(Direction.values()[context.random().nextInt(6)]);
                if (Math.abs(next.getX() - baseX) <= 3 && Math.abs(next.getZ() - baseZ) <= 3) pos = next;
            }
        }
        return true;
    }
}
