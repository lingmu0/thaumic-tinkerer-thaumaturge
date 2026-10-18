package net.lingmu.thaumictinkerer.focus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.lingmu.thaumictinkerer.TTContent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

public final class MobAspects {
    public static final List<String> ASPECTS=List.of("aqua","humanus","aer","volatus","ignis","praecantatio","exanimis","corpus","bestia","venenum","terra","alienis","iter","metallum","limus","messis");
    private static final Map<String, DeferredItem<Item>> ITEMS = new LinkedHashMap<>();
    private static final Map<String,List<String>> MOBS = new LinkedHashMap<>();
    static {
        mob("snow_golem","aqua","aqua","humanus"); mob("bat","aer","aer","volatus"); mob("blaze","ignis","ignis","ignis");
        mob("cave_spider","bestia","venenum","venenum");mob("chicken","messis","volatus","bestia");mob("cow","bestia","terra","bestia");
        mob("creeper","praecantatio","bestia","alienis");mob("enderman","alienis","alienis","humanus");mob("ghast","ignis","volatus","volatus");
        mob("horse","bestia","bestia","iter");mob("iron_golem","metallum","metallum","humanus");mob("magma_cube","ignis","limus","limus");
        mob("mooshroom","bestia","terra","messis");mob("ocelot","bestia","terra","alienis");mob("pig","bestia","terra","iter");
        mob("zombified_piglin","exanimis","corpus","ignis");mob("sheep","terra","terra","bestia");mob("silverfish","metallum","metallum","terra");
        mob("skeleton","exanimis","humanus","exanimis");mob("slime","limus","limus","bestia");mob("spider","bestia","exanimis","exanimis");
        mob("squid","aqua","aqua","aqua");mob("villager","humanus","humanus","humanus");mob("witch","praecantatio","exanimis","alienis");
        mob("wolf","bestia","bestia","bestia");mob("zombie","corpus","corpus","exanimis");
        MOBS.put("thaumaturge:wisp",List.of("aer","praecantatio","praecantatio"));
        MOBS.put("thaumaturge:brainy_zombie",List.of("praecantatio","exanimis","corpus"));
        MOBS.put("thaumaturge:firebat",List.of("volatus","ignis","praecantatio"));
    }
    private static void mob(String id,String a,String b,String c) { MOBS.put("minecraft:"+id,List.of(a,b,c)); }
    public static List<String> drops(Entity entity) { return MOBS.getOrDefault(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString(),List.of()); }
    public static void register() {
        for(String aspect:ASPECTS) for(int tier=0;tier<3;tier++) {
            String id=prefix(tier)+"mob_aspect_"+aspect;
            ITEMS.put(id,TTContent.item(id,Item::new,new Item.Properties().stacksTo(16).component(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE,tier==2)));
        }
    }
    private static String prefix(int tier) { return tier==2?"infused_":tier==1?"condensed_":""; }
    public static ItemStack stack(String aspect,int tier) {
        DeferredItem<Item> item=ITEMS.get(prefix(tier)+"mob_aspect_"+aspect);
        return item==null?ItemStack.EMPTY:new ItemStack(item.get());
    }
    public static String aspect(ItemStack stack) {
        String key=BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if(!key.startsWith("thaumictinkerer:")||!key.contains("mob_aspect_"))return "";
        String aspect=key.substring(key.indexOf("mob_aspect_")+11);
        return ASPECTS.contains(aspect)?aspect:"";
    }
    public static int tier(ItemStack stack) {
        String key=BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return key.startsWith("infused_")?2:key.startsWith("condensed_")?1:0;
    }
    private MobAspects() {}
}
