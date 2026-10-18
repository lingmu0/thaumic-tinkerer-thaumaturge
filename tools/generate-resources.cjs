/* Generate only resources for implemented registrations. No upstream dependency assets are bundled. */
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '..');
const reference = path.resolve(root, '../.thaumic-tinkerer-1.7.10-upstream');
const assets = path.join(reference, 'src/main/resources/assets/ttinkerer');
const out = path.join(root, 'common/src/main/resources');
function json(file, value) {
    const target = path.join(out, file);
    fs.mkdirSync(path.dirname(target), {recursive: true});
    fs.writeFileSync(target, JSON.stringify(value, null, 2) + '\n');
}
function copy(from, to) {
    const target = path.join(out, to);
    fs.mkdirSync(path.dirname(target), {recursive: true});
    fs.copyFileSync(path.join(assets, from), target);
}
const mappings = {dark_quartz: 'darkQuartzItem', spell_cloth: 'spellCloth', xp_talisman: 'xpTalisman0', cleansing_talisman: 'cleansingTalisman0', ichor:'ichor',ichor_cloth:'ichorCloth',ichorium:'ichorium',ichor_nugget:'ichorNugget',nether_shard:'netherShard',ender_shard:'enderShard'};
for (const [name, texture] of Object.entries(mappings)) {
    copy(`textures/items/${texture}.png`, `assets/thaumictinkerer/textures/item/${name}.png`);
    json(`assets/thaumictinkerer/models/item/${name}.json`, {parent: 'minecraft:item/generated', textures: {layer0: `thaumictinkerer:item/${name}`}});
}
const quartzTextures={darkQuartz0:'dark_quartz_top',darkQuartz1:'dark_quartz_side',darkQuartz2:'dark_quartz_bottom',chiseledDarkQuartz0:'dark_quartz_chiseled_side',chiseledDarkQuartz1:'dark_quartz_chiseled_end',pillarDarkQuartz0:'dark_quartz_pillar_side',pillarDarkQuartz1:'dark_quartz_pillar_end'};
for (const [file,name] of Object.entries(quartzTextures)) {
    copy(`textures/blocks/${file}.png`, `assets/thaumictinkerer/textures/block/${name}.png`);
    // Remove only these obsolete generated copies, so invalid uppercase paths cannot ship.
    const obsolete=path.join(out,`assets/thaumictinkerer/textures/block/${file}.png`);
    if(fs.existsSync(obsolete))fs.unlinkSync(obsolete);
}
const tex = name => `thaumictinkerer:block/${name}`;
const base = 'assets/thaumictinkerer';
const textures = {side: tex('dark_quartz_side'), top: tex('dark_quartz_top'), bottom: tex('dark_quartz_bottom')};
for (const [name, parent, material] of [
    ['dark_quartz_block', 'minecraft:block/cube_bottom_top', textures],
    ['dark_quartz_chiseled', 'minecraft:block/cube_column', {side: tex('dark_quartz_chiseled_side'), end: tex('dark_quartz_chiseled_end')}],
    ['dark_quartz_pillar', 'minecraft:block/cube_column', {side: tex('dark_quartz_pillar_side'), end: tex('dark_quartz_pillar_end')}],
]) {
    json(`${base}/models/block/${name}.json`, {parent, textures: material});
    json(`${base}/blockstates/${name}.json`, {variants: {'': {model: `thaumictinkerer:block/${name}`}}});
}
for (const [suffix, parent] of [['slab', 'slab'], ['slab_top', 'slab_top'], ['stairs', 'stairs'], ['stairs_inner', 'inner_stairs'], ['stairs_outer', 'outer_stairs']]) {
    json(`${base}/models/block/dark_quartz_${suffix}.json`, {parent: `minecraft:block/${parent}`, textures});
}
json(`${base}/blockstates/dark_quartz_slab.json`, {variants: {
    'type=bottom': {model: 'thaumictinkerer:block/dark_quartz_slab'},
    'type=top': {model: 'thaumictinkerer:block/dark_quartz_slab_top'},
    'type=double': {model: 'thaumictinkerer:block/dark_quartz_block'},
}});
const variants = {};
for (const [facing, rotation] of Object.entries({east: 0, south: 90, west: 180, north: 270})) {
    for (const half of ['bottom', 'top']) for (const shape of ['straight', 'inner_left', 'inner_right', 'outer_left', 'outer_right']) {
        let y = rotation + (shape.endsWith('left') ? 270 : 0);
        if (shape !== 'straight' && half === 'top') y += 90;
        variants[`facing=${facing},half=${half},shape=${shape}`] = {
            model: `thaumictinkerer:block/dark_quartz_${shape.startsWith('inner') ? 'stairs_inner' : shape.startsWith('outer') ? 'stairs_outer' : 'stairs'}`,
            x: half === 'top' ? 180 : 0, y: y % 360, uvlock: true,
        };
    }
}
json(`${base}/blockstates/dark_quartz_stairs.json`, {variants});
const blocks = ['dark_quartz_block', 'dark_quartz_chiseled', 'dark_quartz_pillar', 'dark_quartz_slab', 'dark_quartz_stairs'];
for (const block of blocks) {
    json(`${base}/models/item/${block}.json`, {parent: `thaumictinkerer:block/${block}`});
    const count = block === 'dark_quartz_slab' ? [{add: false, conditions: [{block: `thaumictinkerer:${block}`, condition: 'minecraft:block_state_property', properties: {type: 'double'}}], count: 2, function: 'minecraft:set_count'}] : [];
    json(`data/thaumictinkerer/loot_table/blocks/${block}.json`, {type: 'minecraft:block', pools: [{rolls: 1, entries: [{type: 'minecraft:item', name: `thaumictinkerer:${block}`, functions: count}], conditions: [{condition: 'minecraft:survives_explosion'}]}]});
}
json('data/minecraft/tags/block/mineable/pickaxe.json', {replace: false, values: blocks.map(b => `thaumictinkerer:${b}`)});
json('data/thaumictinkerer/tags/item/cannot_disenchant.json', {replace: false, values: []});
json('data/curios/tags/item/necklace.json', {replace: false, values: ['thaumictinkerer:xp_talisman', 'thaumictinkerer:cleansing_talisman']});
json('data/thaumictinkerer/recipe/spell_cloth_disenchant.json', {type: 'thaumictinkerer:spell_cloth', category: 'misc'});
function ingredient(item) {return item.startsWith('#') ? {tag: item.slice(1)} : {item};}
function shaped(name, count, pattern, key) {
    json(`data/thaumictinkerer/recipe/${name}.json`, {type: 'minecraft:crafting_shaped', category: 'building', pattern,
        key: Object.fromEntries(Object.entries(key).map(([k,v])=>[k,ingredient(v)])), result: {id: `thaumictinkerer:${name}`, count}});
}
shaped('dark_quartz_block', 1, ['QQ','QQ'], {Q:'thaumictinkerer:dark_quartz'});
shaped('dark_quartz_slab', 6, ['QQQ'], {Q:'thaumictinkerer:dark_quartz_block'});
shaped('dark_quartz_stairs', 4, ['Q  ','QQ ','QQQ'], {Q:'thaumictinkerer:dark_quartz_block'});
shaped('dark_quartz_chiseled', 1, ['S','S'], {S:'thaumictinkerer:dark_quartz_slab'});
shaped('dark_quartz_pillar', 2, ['Q','Q'], {Q:'thaumictinkerer:dark_quartz_block'});
const {aspects} = require('./resource-helpers.cjs');
function crucible(name, catalyst, cost) {
    json(`data/thaumictinkerer/recipe/${name}.json`, {type:'thaumaturge:crucible', catalyst:ingredient(catalyst), aspects:aspects(cost), result:{id:`thaumictinkerer:${name}`}, research:{entry:`thaumictinkerer:${name}`}});
}
function infusion(name, catalyst, components, cost, instability, count=1) {
    json(`data/thaumictinkerer/recipe/${name}.json`, {type:'thaumaturge:infusion', catalyst:ingredient(catalyst), components:components.map(ingredient), aspects:aspects(cost), instability, result:{id:`thaumictinkerer:${name}`,count}, research:{entry:`thaumictinkerer:${name}`}});
}
function arcane(name, count, pattern, key, crystals, vis) {
    json(`data/thaumictinkerer/recipe/${name}.json`,{type:'thaumaturge:arcane_workbench_shaped',pattern,key:Object.fromEntries(Object.entries(key).map(([k,v])=>[k,ingredient(v)])),crystals:aspects(crystals),vis,result:{id:`thaumictinkerer:${name}`,count},research:{entry:`thaumictinkerer:${name}`}});
}
// Original values and ingredient intent, with modern Thaumaturge resource identifiers.
shaped('dark_quartz', 8, ['QQQ','QCQ','QQQ'], {Q:'minecraft:quartz',C:'#minecraft:coals'});
crucible('spell_cloth', 'thaumaturge:fabric', {praecantatio:10,perditio:6,permutatio:4});
infusion('xp_talisman','minecraft:gold_ingot',['minecraft:quartz','thaumictinkerer:dark_quartz','thaumaturge:brain','minecraft:diamond'],{lucrum:20,permutatio:10,bestia:10,machina:5},6);
infusion('cleansing_talisman','minecraft:ender_pearl',['thaumictinkerer:dark_quartz','thaumictinkerer:dark_quartz','thaumictinkerer:dark_quartz','thaumictinkerer:dark_quartz','minecraft:ghast_tear','thaumaturge:alumentum'],{sano:10,instrumentum:10,humanus:20,victus:10},5);
infusion('ichor','minecraft:nether_star',['minecraft:diamond',...Array(8).fill('thaumictinkerer:ender_shard'),'minecraft:ender_eye',...Array(8).fill('thaumictinkerer:nether_shard')],{humanus:32,lux:32,spiritus:64},7,8);
arcane('ichor_cloth',3,['CCC','III','DDD'],{C:'thaumaturge:fabric',I:'thaumictinkerer:ichor',D:'minecraft:diamond'},{ignis:1,terra:1,aqua:1,aer:1,ordo:1,perditio:1},125);
arcane('ichorium',1,[' T ','IDI',' I '],{T:'thaumaturge:ingot_thaumium',I:'thaumictinkerer:ichor',D:'minecraft:diamond'},{ignis:1,terra:1,aqua:1,aer:1,ordo:1,perditio:1},100);
json('data/thaumictinkerer/recipe/ichor_nugget.json',{type:'minecraft:crafting_shapeless',category:'misc',ingredients:[ingredient('thaumictinkerer:ichorium')],result:{id:'thaumictinkerer:ichor_nugget',count:9}});
shaped('ichorium_from_nuggets',1,['NNN','NNN','NNN'],{N:'thaumictinkerer:ichor_nugget'});
// The helper's output normally matches its file name; this reverse material recipe shares ichorium.
const reverseRecipe=path.join(out,'data/thaumictinkerer/recipe/ichorium_from_nuggets.json');
const reverseData=JSON.parse(fs.readFileSync(reverseRecipe)); reverseData.result.id='thaumictinkerer:ichorium'; fs.writeFileSync(reverseRecipe,JSON.stringify(reverseData,null,2)+'\n');
json('data/thaumictinkerer/thaumaturge/research_category/tinkering.json', {formula:aspects({instrumentum:10,praecantatio:10,fabrico:10}), icon:'thaumictinkerer:textures/item/dark_quartz.png',background:'thaumaturge:textures/gui/gui_research_back_4.png',required_research:'thaumaturge:unlock_artifice',index:20});
const research = {
    dark_quartz:{x:-2,y:2,parents:[],cost:{}},
    spell_cloth:{x:3,y:2,complexity:2,parents:['thaumaturge:base_artifice'],cost:{praecantatio:2,pannus:1}},
    cleansing_talisman:{x:-3,y:4,complexity:3,parents:['thaumictinkerer:dark_quartz'],cost:{sano:2,ordo:1,venenum:1}},
    xp_talisman:{x:4,y:-1,complexity:2,parents:['thaumaturge:jar_brain','thaumictinkerer:spell_cloth'],cost:{lucrum:1,praecantatio:1,humanus:1}},
    ichor:{x:9,y:8,parents:['thaumaturge:primordial_pearl','thaumictinkerer:xp_talisman','thaumictinkerer:cleansing_talisman'],cost:{humanus:1,lux:2,spiritus:1,vitium:1}},
    ichor_cloth:{x:11,y:7,parents:['thaumictinkerer:ichor'],cost:{pannus:2,lux:1,fabrico:1,sensus:1}},
    ichorium:{x:11,y:9,parents:['thaumictinkerer:ichor_cloth'],cost:{metallum:2,lux:1,fabrico:1,instrumentum:1}},
};
for (const [name, entry] of Object.entries(research)) json(`data/thaumictinkerer/thaumaturge/research_entry/${name}.json`, {
    category:'thaumictinkerer:tinkering', name:`research.thaumictinkerer.${name}.title`, parents:entry.parents,column:entry.x,row:entry.y,
    icons:[`thaumictinkerer:${name}`],note_aspects:aspects(entry.cost),complexity:entry.complexity||1,
    ...(name==='dark_quartz'?{meta:['autounlock','round']}:{}),
    stages:[{text:`research.thaumictinkerer.${name}.text`,recipes:[`thaumictinkerer:${name}`]}],
});
const names = {
    en_us:{dark_quartz:'Smokey Quartz',spell_cloth:'Spellbinding Cloth',xp_talisman:'Talisman of Withhold',cleansing_talisman:'Talisman of Remedium',dark_quartz_block:'Block of Smokey Quartz',dark_quartz_chiseled:'Chiseled Smokey Quartz',dark_quartz_pillar:'Smokey Quartz Pillar',dark_quartz_slab:'Smokey Quartz Slab',dark_quartz_stairs:'Smokey Quartz Stairs',ichor:'Ichor',ichor_cloth:'Ichorcloth',ichorium:'Ichorium',ichor_nugget:'Ichorium Nugget',nether_shard:'Nether Shard',ender_shard:'Ender Shard'},
    zh_cn:{dark_quartz:'烟熏石英',spell_cloth:'祛魔布',xp_talisman:'经验护符',cleansing_talisman:'净化护符',dark_quartz_block:'烟熏石英块',dark_quartz_chiseled:'錾制烟熏石英块',dark_quartz_pillar:'烟熏石英柱',dark_quartz_slab:'烟熏石英台阶',dark_quartz_stairs:'烟熏石英楼梯',ichor:'灵宝',ichor_cloth:'灵宝布',ichorium:'灵宝锭',ichor_nugget:'灵宝粒',nether_shard:'下界碎片',ender_shard:'末影碎片'},
};
for (const [language, entries] of Object.entries(names)) {
    const lang = {'itemGroup.thaumictinkerer':language==='zh_cn'?'神秘工匠':'Thaumic Tinkerer', 'research_category.thaumictinkerer.tinkering':language==='zh_cn'?'神秘工匠':'Thaumic Tinkering'};
    for (const [name, label] of Object.entries(entries)) lang[`${blocks.includes(name)?'block':'item'}.thaumictinkerer.${name}`]=label;
    for (const name of Object.keys(research)) {
        lang[`research.thaumictinkerer.${name}.title`]=entries[name];
        const texts = {
            dark_quartz:['Surround coal or charcoal with eight pieces of nether quartz to make smokey quartz. It can be worked into decorative blocks, pillars, stairs and slabs.','用八块下界石英围住煤或木炭，制成烟熏石英，可用于制作装饰方块、柱、楼梯和台阶。'],
            spell_cloth:['Combine this cloth with one enchanted item in a crafting grid to remove its enchantments. Other item data is preserved. The cloth has 35 uses.','在合成格中将祛魔布与一件附魔物品合成，即可移除附魔并保留其他物品数据。每块布可使用 35 次。'],
            xp_talisman:['Equip in a Curios necklace slot. Sneak-use to toggle collection within 3 blocks, up to 1500 XP. Use with a glass bottle in your inventory to spend 10 XP and fill a Bottle o’ Enchanting.','装备在 Curios 项链栏，潜行使用可切换吸收状态。收集 3 格内的经验，最多储存 1500 点。背包中有玻璃瓶时使用，每瓶消耗 10 点经验。'],
            cleansing_talisman:['Equip in a Curios necklace slot and sneak-use to activate. Every second it extinguishes fire or removes one harmful effect. Lasts for 100 cleanses.','装备在 Curios 项链栏，潜行使用启用。每秒扑灭一次火焰或清除一个负面效果，可净化 100 次。'],
            ichor:['Ichor begins the KAMI material chain. Player kills of endermen in the End have a 1/32 chance to drop an Ender Shard; zombified piglins in the Nether have a 1/16 chance to drop a Nether Shard. Infuse these with a nether star, diamond and eye of ender.','灵宝是 KAMI 材料链的起点。玩家在末地击杀末影人有 1/32 概率掉落末影碎片，在下界击杀僵尸猪灵有 1/16 概率掉落下界碎片。将碎片与下界之星、钻石和末影之眼注魔，制成灵宝。'],
            ichor_cloth:['Weave enchanted fabric, ichor and diamonds into ichorcloth at an arcane workbench.','在奥术工作台中将魔法布、灵宝和钻石编织成灵宝布。'],
            ichorium:['Combine thaumium, ichor and a diamond at an arcane workbench to create ichorium. It can be divided into nine nuggets and combined again.','在奥术工作台将神秘锭、灵宝与钻石制成灵宝锭，可分解为九粒灵宝粒并重新合成。'],
        };
        lang[`research.thaumictinkerer.${name}.text`]=texts[name][language==='zh_cn'?1:0];
    }
    for (const [key, english, chinese] of [['absorbing','Absorbing','正在吸收'],['notAbsorbing','Not Absorbing','停止吸收'],['active','Active','已启用'],['inactive','Inactive','已停用']]) lang[`ttmisc.${key}`]=language==='zh_cn'?chinese:english;
    json(`${base}/lang/${language}.json`,lang);
}
// 26.1.2 moved ingredient encoding to registry strings, and item definitions to assets/*/items.
const modernOut = path.join(root,'neoforge-26.1.2/src/main/resources');
for (const name of [...Object.keys(mappings),...blocks]) {
    const target=path.join(modernOut,`${base}/items/${name}.json`);
    fs.mkdirSync(path.dirname(target),{recursive:true});
    fs.writeFileSync(target,JSON.stringify({model:{type:'minecraft:model',model:`thaumictinkerer:item/${name}`}},null,2)+'\n');
}
console.log('Generated resources for 15 implemented items and blocks.');
