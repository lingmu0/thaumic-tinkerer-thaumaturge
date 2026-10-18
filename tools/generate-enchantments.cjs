const fs=require('node:fs'),path=require('node:path');
const {ROOT,aspects,modernAspect,write}=require('./resource-helpers.cjs');
const reference=path.resolve(ROOT,'../.thaumic-tinkerer-1.7.10-upstream');
const source=fs.readFileSync(path.join(reference,'src/main/java/thaumic/tinkerer/common/enchantment/core/EnchantmentManager.java'),'utf8');
const renames={ascentBoost:'ascent_boost',slowFall:'slow_fall',autoSmelt:'auto_smelt',desintegrate:'disintegrate',quickDraw:'quick_draw',focusedStrike:'focused_strikes',dispersedStrikes:'dispersed_strikes',finalStrike:'final_strike'};
const vanillaNames={fireProtection:'fire_protection',featherFalling:'feather_falling',blastProtection:'blast_protection',projectileProtection:'projectile_protection',aquaAffinity:'aqua_affinity',baneOfArthropods:'bane_of_arthropods',fireAspect:'fire_aspect',silkTouch:'silk_touch',field_151369_A:'lure',field_151370_z:'luck_of_the_sea'};
const costs={};
for(const line of source.split('\n')) {
    const match=line.match(/registerExponentialCostData\((ModEnchantments|Enchantment)\.([\w]+),/);
    if(!match)continue;
    const isTinkerer=match[1]==='ModEnchantments';
    const id=(isTinkerer?'thaumictinkerer:':'minecraft:')+(renames[match[2]]||vanillaNames[match[2]]||match[2]);
    const list={};
    for(const aspect of line.matchAll(/\.add\(Aspect\.([A-Z_]+),\s*(\d+)\)/g)) {
        const key=modernAspect(aspect[1]);list[key]=(list[key]||0)+Number(aspect[2]);
    }
    costs[id]=list;
}
const specs={
    ascent_boost:[4,'legs','leg_armor','Ascent Boost','腾跃',6,2,{AIR:1,MOTION:1,MAGIC:2},[],'ascentBoost'],
    slow_fall:[3,'feet','foot_armor','Slow Fall','缓落',7,3,{AIR:1,MOTION:1,MAGIC:2},[],'slowFall'],
    auto_smelt:[1,'mainhand','mining','Fiery Core','炽热核心',7,5,{FIRE:1,ENTROPY:1,MAGIC:2},[],'autoSmelt'],
    disintegrate:[1,'mainhand','mining','Disintegrate','分解',6,6,{ENTROPY:1,VOID:1,MAGIC:2},[],'desintegrate'],
    quick_draw:[2,'mainhand','bow','Quick Draw','速射',4,6,{SENSES:1,WEAPON:1,MAGIC:2},[],'quickDraw'],
    vampirism:[2,'mainhand','weapon','Vampirism','吸血',3,5,{HUNGER:1,WEAPON:1,MAGIC:2},[],'vamprisim'],
    focused_strikes:[5,'mainhand','weapon','Focused Strikes','专注打击',2,7,{ORDER:1,WEAPON:1,MAGIC:2},['vampirism'],'focusedStrikes'],
    dispersed_strikes:[5,'mainhand','weapon','Dispersed Strikes','分散打击',1,6,{ENTROPY:1,WEAPON:1,MAGIC:2},['vampirism'],'dispersedStrikes'],
    final_strike:[5,'mainhand','weapon','Final Strike','终结打击',0,8,{ENTROPY:3,ORDER:3,WEAPON:3,MAGIC:2},['focused_strikes','dispersed_strikes'],'finalStrike'],
    pounce:[5,'legs','leg_armor','Pounce','猛扑',7,0,{AIR:3,ORDER:3,ARMOR:3,MAGIC:2},['ascent_boost'],'pounce'],
    shatter:[5,'mainhand','mining','Shatter','粉碎',5,8,{EARTH:3,ENTROPY:3,TOOL:1,MAGIC:2},['disintegrate'],'shatter'],
    shockwave:[5,'feet','foot_armor','Shockwave','冲击波',9,2,{AIR:3,ENTROPY:3,ARMOR:1,MAGIC:2},['slow_fall'],'shockwave'],
    tunnel:[5,'mainhand','mining','Tunneling','隧道',9,6,{EARTH:3,ORDER:3,TOOL:1,MAGIC:2},['auto_smelt'],'tunnel'],
    valiance:[5,'mainhand','weapon','Valiance','英勇',1,4,{WEAPON:3,HEAL:3,MAGIC:2},['vampirism'],'valiance'],
};
const exclusion={slow_fall:['minecraft:feather_falling','thaumictinkerer:shockwave'],shockwave:['minecraft:feather_falling','thaumictinkerer:slow_fall'],quick_draw:['minecraft:punch'],vampirism:['minecraft:fire_aspect','minecraft:knockback'],shatter:['minecraft:efficiency','thaumictinkerer:disintegrate','thaumictinkerer:tunnel'],tunnel:['minecraft:efficiency','thaumictinkerer:shatter']};
const restrictive={
    auto_smelt:['minecraft:unbreaking','minecraft:mending'],disintegrate:['minecraft:unbreaking','minecraft:mending'],
    focused_strikes:['minecraft:unbreaking','minecraft:mending','minecraft:knockback','minecraft:looting'],
    dispersed_strikes:['minecraft:unbreaking','minecraft:mending','minecraft:knockback','minecraft:looting'],
    final_strike:['minecraft:unbreaking','minecraft:mending','minecraft:sharpness','minecraft:smite'],
    valiance:['minecraft:unbreaking','minecraft:mending','minecraft:sharpness','minecraft:smite'],
};
const allIds=[...Object.keys(costs),'minecraft:mending'];
const catalog={languages:{en_us:{},zh_cn:{}},enchantments:{}};
const descriptions={
    ascent_boost:['Increases jump velocity when not sneaking.','非潜行时提高起跳速度。'],slow_fall:['Slows falls after 2.9 blocks. Sneak to fall normally.','下落超过 2.9 格后减速，潜行可正常下落。'],
    auto_smelt:['Instantly breaks wood and smelts its drops when a furnace recipe exists. Other block materials cannot be mined with this enchantment.','立即破坏木质方块；若掉落物可烧炼，自动转换为产物。此附魔无法开采其他材质。'],
    disintegrate:['Instantly destroys blocks with hardness at most 1.5, producing no drops or experience.','立即摧毁硬度不超过 1.5 的方块，不产生掉落物或经验。'],
    quick_draw:['Draws bows more quickly.','加快弓的拉弦速度。'],vampirism:['Heals the attacker by one heart per two enchantment levels of health points per hit.','每次命中恢复等于附魔等级的生命值。'],
    focused_strikes:['Damage starts at half and grows as the same target is struck repeatedly.','首次伤害减半，连续攻击同一目标时逐渐增加。'],dispersed_strikes:['Repeated attacks against the same target deal reduced damage. Change targets to reset the penalty.','连续攻击同一目标时伤害降低，切换目标可重置。'],
    final_strike:['A small chance to triple attack damage.','攻击有一定概率造成三倍伤害。'],pounce:['Increases attack damage while airborne.','腾空时增加攻击伤害。'],shatter:['Increases speed on blocks harder than 20; other blocks are slower to mine.','大幅加速开采硬度超过 20 的方块，开采其他方块时略微减速。'],
    shockwave:['A fall of more than three blocks damages other living entities within ten blocks.','从超过三格的高度坠落时，对十格内其他生物造成冲击伤害。'],tunnel:['Mining in the same direction becomes faster. Turning more than 50 degrees incurs a speed penalty until the next block is broken.','沿相同方向挖掘更快，转向超过 50 度后减速，直至破坏下一个方块。'],valiance:['Increases damage while below half health.','生命值低于一半时提高伤害。'],
};
for(const [id,spec] of Object.entries(specs)) {
    const [max,slot,tag,en,zh,column,row,note,parents,texture]=spec;
    const incompatible=new Set(exclusion[id]||[]);
    if(restrictive[id])for(const candidate of allIds)if(candidate!==`thaumictinkerer:${id}`&&!restrictive[id].includes(candidate))incompatible.add(candidate);
    // Compatibility is symmetric in vanilla. Mirror custom restrictions so neither direction bypasses it.
    for(const [other,allowed] of Object.entries(restrictive))if(other!==id&&!allowed.includes(`thaumictinkerer:${id}`))incompatible.add(`thaumictinkerer:${other}`);
    write(`data/thaumictinkerer/enchantment/${id}.json`,{description:{translate:`enchantment.thaumictinkerer.${id}`},supported_items:`#minecraft:enchantable/${tag}`,weight:1,max_level:max,min_cost:{base:1,per_level_above_first:0},max_cost:{base:100,per_level_above_first:0},anvil_cost:4,slots:[slot],exclusive_set:[...incompatible],effects:{}});
    let icon='minecraft:enchanted_book';
    const original=path.join(reference,`src/main/resources/assets/ttinkerer/textures/enchants/${texture}.png`);
    if(fs.existsSync(original)) {
        const target=path.join(ROOT,`common/src/main/resources/assets/thaumictinkerer/textures/research/enchant/${id}.png`);fs.mkdirSync(path.dirname(target),{recursive:true});fs.copyFileSync(original,target);
        icon=`thaumictinkerer:textures/research/enchant/${id}.png`;
    }
    write(`data/thaumictinkerer/thaumaturge/research_entry/enchant/${id}.json`,{category:'thaumictinkerer:tinkering',name:`research.thaumictinkerer.enchant.${id}.title`,parents:parents.length?parents.map(p=>`thaumictinkerer:enchant/${p}`):['thaumictinkerer:osmotic_enchanter'],column,row,icons:[icon],note_aspects:aspects(note),complexity:2,stages:[{text:`research.thaumictinkerer.enchant.${id}.text`}]});
    for(const [language,name,index] of [['en_us',en,0],['zh_cn',zh,1]]) {
        catalog.languages[language][`enchantment.thaumictinkerer.${id}`]=name;
        catalog.languages[language][`research.thaumictinkerer.enchant.${id}.title`]=name;
        catalog.languages[language][`research.thaumictinkerer.enchant.${id}.text`]=descriptions[id][index];
    }
    catalog.enchantments[id]={maxLevel:max,slot,baseCost:costs[`thaumictinkerer:${id}`],research:`thaumictinkerer:enchant/${id}`};
}
const java=`package net.lingmu.thaumictinkerer.enchantment;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
public final class EnchantmentCatalog {
    private static final Map<String,Map<String,Integer>> COSTS=Map.ofEntries(
${Object.entries(costs).map(([id,cost])=>`        Map.entry(${JSON.stringify(id)},Map.ofEntries(${Object.entries(cost).map(([a,n])=>`Map.entry(${JSON.stringify(a)},${n})`).join(',')}))`).join(',\n')});
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
`;
const target=path.join(ROOT,'common/src/main/java/net/lingmu/thaumictinkerer/enchantment/EnchantmentCatalog.java');fs.mkdirSync(path.dirname(target),{recursive:true});fs.writeFileSync(target,java);
fs.mkdirSync(path.join(ROOT,'catalog'),{recursive:true});fs.writeFileSync(path.join(ROOT,'catalog/enchantments.json'),JSON.stringify(catalog,null,2)+'\n');
console.log(`Generated ${Object.keys(specs).length} enchantments and ${Object.keys(costs).length} osmotic cost profiles from the licensed original source.`);
