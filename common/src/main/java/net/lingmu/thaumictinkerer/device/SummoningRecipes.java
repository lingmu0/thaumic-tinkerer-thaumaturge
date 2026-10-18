/* Original Thaumic Tinkerer mob-aspect combinations, ported under CC BY-NC-SA 3.0. */
package net.lingmu.thaumictinkerer.device;

import java.util.*;
import net.lingmu.thaumictinkerer.StackData;
import net.lingmu.thaumictinkerer.focus.MobAspects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public final class SummoningRecipes {
    public record Entry(String entity, List<String> aspects) {
        Entry(String entity, String a, String b, String c) { this(entity, sorted(List.of(a,b,c))); }
    }
    public static final List<Entry> RECIPES = List.of(
        new Entry("minecraft:snow_golem","aqua","aqua","humanus"), new Entry("minecraft:bat","aer","aer","volatus"),
        new Entry("minecraft:blaze","ignis","ignis","ignis"), new Entry("thaumaturge:brainy_zombie","praecantatio","exanimis","corpus"),
        new Entry("thaumaturge:firebat","volatus","ignis","praecantatio"), new Entry("minecraft:cave_spider","bestia","venenum","venenum"),
        new Entry("minecraft:chicken","messis","volatus","bestia"), new Entry("minecraft:cow","bestia","terra","bestia"),
        new Entry("minecraft:creeper","praecantatio","bestia","alienis"), new Entry("minecraft:enderman","alienis","alienis","humanus"),
        new Entry("minecraft:ghast","ignis","volatus","volatus"), new Entry("minecraft:horse","bestia","bestia","iter"),
        new Entry("minecraft:iron_golem","metallum","metallum","humanus"), new Entry("minecraft:magma_cube","ignis","limus","limus"),
        new Entry("minecraft:mooshroom","bestia","terra","messis"), new Entry("minecraft:ocelot","bestia","terra","alienis"),
        new Entry("minecraft:pig","bestia","terra","iter"), new Entry("minecraft:zombified_piglin","exanimis","corpus","ignis"),
        new Entry("minecraft:sheep","terra","terra","bestia"), new Entry("minecraft:silverfish","metallum","metallum","terra"),
        new Entry("minecraft:skeleton","exanimis","humanus","exanimis"), new Entry("minecraft:slime","limus","limus","bestia"),
        new Entry("minecraft:spider","bestia","exanimis","exanimis"), new Entry("minecraft:squid","aqua","aqua","aqua"),
        new Entry("minecraft:villager","humanus","humanus","humanus"), new Entry("thaumaturge:wisp","aer","praecantatio","praecantatio"),
        new Entry("minecraft:witch","praecantatio","exanimis","alienis"), new Entry("minecraft:wolf","bestia","bestia","bestia"),
        new Entry("minecraft:zombie","corpus","corpus","exanimis")
    );
    private record Input(Container container, int slot) { ItemStack stack() { return container.getItem(slot); } }
    private static List<String> sorted(List<String> values) { return values.stream().sorted().toList(); }
    public static void summon(MachineBlockEntity machine, ServerLevel level) {
        List<Input> inputs = new ArrayList<>();
        add(inputs, machine);
        for (BlockPos pos : BlockPos.betweenClosed(machine.getBlockPos().offset(-5,-1,-5), machine.getBlockPos().offset(5,1,5))) {
            if (pos.equals(machine.getBlockPos()) || !level.hasChunkAt(pos)) continue;
            if (level.getBlockEntity(pos) instanceof Container container) add(inputs, container);
            else {
                Container pedestal = VersionDevices.inventoryAt(level, pos);
                if (pedestal != null) add(inputs, pedestal);
            }
        }
        // Keep the search bounded even next to storage warehouses.
        if (inputs.size() > 32) inputs = inputs.subList(0,32);
        for (int i=0;i<inputs.size();i++) for(int j=i+1;j<inputs.size();j++) for(int k=j+1;k<inputs.size();k++) {
            List<Input> picked = List.of(inputs.get(i),inputs.get(j),inputs.get(k));
            List<String> aspects = sorted(picked.stream().map(input -> MobAspects.aspect(input.stack())).toList());
            Entry entry = RECIPES.stream().filter(recipe -> recipe.aspects.equals(aspects)).findFirst().orElse(null);
            if (entry == null) continue;
            boolean permanent = picked.stream().allMatch(input -> MobAspects.tier(input.stack()) == 2);
            if (permanent && level.getGameTime() % 1200 != 0) continue;
            String identity = level.dimension().location() + "/" + machine.getBlockPos().asLong();
            // Infused souls become tied to their first summoning tablet, as in the original.
            if (permanent && picked.stream().anyMatch(input -> !StackData.string(input.stack(),"tt_summoner").isEmpty()
                && !StackData.string(input.stack(),"tt_summoner").equals(identity))) continue;
            if (!VersionDevices.spawn(level, ResourceLocation.parse(entry.entity), machine.getBlockPos().above())) continue;
            for (Input input : picked) {
                if (permanent) { ItemStack bound = input.stack().copy(); StackData.string(bound,"tt_summoner",identity); input.container.setItem(input.slot,bound); }
                else input.container.removeItem(input.slot,1);
                input.container.setChanged();
            }
            machine.setChanged(); return;
        }
    }
    private static void add(List<Input> into, Container container) {
        for(int slot=0;slot<container.getContainerSize();slot++)
            if (!MobAspects.aspect(container.getItem(slot)).isEmpty()) into.add(new Input(container,slot));
    }
    private SummoningRecipes() {}
}
