package net.lingmu.thaumictinkerer.enchantment;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
public final class EnchantmentCatalog {
    private static final Map<String,Map<String,Integer>> COSTS=Map.ofEntries(
        Map.entry("minecraft:protection",Map.ofEntries(Map.entry("terra",10),Map.entry("perditio",7))),
        Map.entry("minecraft:fire_protection",Map.ofEntries(Map.entry("ignis",10),Map.entry("perditio",3),Map.entry("aqua",4))),
        Map.entry("minecraft:feather_falling",Map.ofEntries(Map.entry("aer",16),Map.entry("ordo",5))),
        Map.entry("minecraft:blast_protection",Map.ofEntries(Map.entry("terra",5),Map.entry("ignis",5),Map.entry("perditio",8))),
        Map.entry("minecraft:projectile_protection",Map.ofEntries(Map.entry("aer",10),Map.entry("perditio",7))),
        Map.entry("minecraft:respiration",Map.ofEntries(Map.entry("aqua",10),Map.entry("aer",8),Map.entry("ordo",5))),
        Map.entry("minecraft:aqua_affinity",Map.ofEntries(Map.entry("aqua",25),Map.entry("ordo",20),Map.entry("terra",5))),
        Map.entry("minecraft:thorns",Map.ofEntries(Map.entry("terra",10),Map.entry("perditio",12))),
        Map.entry("minecraft:sharpness",Map.ofEntries(Map.entry("ordo",10))),
        Map.entry("minecraft:smite",Map.ofEntries(Map.entry("ordo",5),Map.entry("aer",5))),
        Map.entry("minecraft:bane_of_arthropods",Map.ofEntries(Map.entry("ordo",5),Map.entry("ignis",5))),
        Map.entry("minecraft:knockback",Map.ofEntries(Map.entry("perditio",5),Map.entry("aer",10))),
        Map.entry("minecraft:fire_aspect",Map.ofEntries(Map.entry("ignis",15),Map.entry("terra",4))),
        Map.entry("minecraft:looting",Map.ofEntries(Map.entry("aer",10),Map.entry("ignis",10),Map.entry("aqua",10),Map.entry("terra",10),Map.entry("ordo",15),Map.entry("perditio",15))),
        Map.entry("minecraft:efficiency",Map.ofEntries(Map.entry("perditio",12),Map.entry("terra",4))),
        Map.entry("minecraft:silk_touch",Map.ofEntries(Map.entry("ordo",50),Map.entry("terra",10),Map.entry("perditio",10))),
        Map.entry("minecraft:unbreaking",Map.ofEntries(Map.entry("ordo",15),Map.entry("aqua",8),Map.entry("terra",8))),
        Map.entry("minecraft:fortune",Map.ofEntries(Map.entry("aer",10),Map.entry("ignis",10),Map.entry("aqua",10),Map.entry("terra",10),Map.entry("ordo",15),Map.entry("perditio",15))),
        Map.entry("minecraft:power",Map.ofEntries(Map.entry("terra",5),Map.entry("ordo",10))),
        Map.entry("minecraft:punch",Map.ofEntries(Map.entry("aer",4),Map.entry("terra",10),Map.entry("perditio",5))),
        Map.entry("minecraft:flame",Map.ofEntries(Map.entry("perditio",5),Map.entry("ignis",20),Map.entry("terra",5))),
        Map.entry("minecraft:infinity",Map.ofEntries(Map.entry("perditio",40),Map.entry("ordo",40),Map.entry("terra",10))),
        Map.entry("minecraft:lure",Map.ofEntries(Map.entry("aqua",20),Map.entry("bestia",20))),
        Map.entry("minecraft:luck_of_the_sea",Map.ofEntries(Map.entry("perditio",20),Map.entry("aqua",20))),
        Map.entry("thaumictinkerer:ascent_boost",Map.ofEntries(Map.entry("perditio",8),Map.entry("aer",10))),
        Map.entry("thaumictinkerer:slow_fall",Map.ofEntries(Map.entry("ordo",8),Map.entry("aer",10))),
        Map.entry("thaumictinkerer:auto_smelt",Map.ofEntries(Map.entry("perditio",20),Map.entry("ignis",30))),
        Map.entry("thaumictinkerer:disintegrate",Map.ofEntries(Map.entry("perditio",25),Map.entry("aer",10),Map.entry("terra",10))),
        Map.entry("thaumictinkerer:quick_draw",Map.ofEntries(Map.entry("ordo",10),Map.entry("aer",10),Map.entry("aqua",5))),
        Map.entry("thaumictinkerer:vampirism",Map.ofEntries(Map.entry("perditio",8),Map.entry("ignis",10),Map.entry("aqua",10))),
        Map.entry("thaumictinkerer:focused_strikes",Map.ofEntries(Map.entry("ordo",12),Map.entry("aer",10),Map.entry("aqua",10))),
        Map.entry("thaumictinkerer:dispersed_strikes",Map.ofEntries(Map.entry("perditio",12),Map.entry("ignis",10),Map.entry("terra",10))),
        Map.entry("thaumictinkerer:valiance",Map.ofEntries(Map.entry("ordo",12),Map.entry("ignis",10),Map.entry("terra",10))),
        Map.entry("thaumictinkerer:final_strike",Map.ofEntries(Map.entry("perditio",16),Map.entry("ignis",16))),
        Map.entry("thaumictinkerer:tunnel",Map.ofEntries(Map.entry("terra",16),Map.entry("ordo",16))),
        Map.entry("thaumictinkerer:shatter",Map.ofEntries(Map.entry("terra",16),Map.entry("perditio",16))),
        Map.entry("thaumictinkerer:shockwave",Map.ofEntries(Map.entry("terra",16),Map.entry("aer",16))),
        Map.entry("thaumictinkerer:pounce",Map.ofEntries(Map.entry("terra",16),Map.entry("aer",16))));
    public static boolean supports(ResourceLocation id) {return COSTS.containsKey(id.toString());}
    public static Map<String,Integer> cost(ResourceLocation id,int level) {
        Map<String,Integer> base=COSTS.get(id.toString());
        if(base==null||level<1||level>255)return Map.of();
        Map<String,Integer> result=new LinkedHashMap<>();
        double multiplier=level*(1D+level*0.2D);
        base.forEach((aspect,amount)->result.put(aspect,(int)(amount*multiplier)));
        return Collections.unmodifiableMap(result);
    }
    public static ResourceLocation research(ResourceLocation id) {
        return id.getNamespace().equals("thaumictinkerer") ? ResourceLocation.fromNamespaceAndPath("thaumictinkerer","enchant/"+id.getPath()) : null;
    }
    private EnchantmentCatalog() {}
}
