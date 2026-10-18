/* Original KAMI recipes, research and art: Thaumic Tinkerer 1.7.10, CC BY-NC-SA 3.0. */
const fs=require('node:fs'),path=require('node:path');
const {ROOT,aspects,ingredient,write}=require('./resource-helpers.cjs');
const UP=path.resolve(ROOT,'../.thaumic-tinkerer-1.7.10-upstream/src/main');
const T='thaumictinkerer:',G='thaumaturge:',M='minecraft:';
const idx=JSON.parse(fs.readFileSync(path.join(ROOT,'dev-libs/thaumaturge-index-26.1.2.json')));
const catalog={items:[],recipes:[],research:[],coverage:{},cannot_disenchant:[],adaptations:[],languages:{en_us:{},zh_cn:{}}};
const textures={ichor_cap:'ichorCap',ichorcloth_rod:'ichorclothRod',cat_amulet:'catAmulet',block_talisman:'blockTalisman0',placement_mirror:'placementMirror',protoclay:'protoclay',sky_pearl:'skyPearl',ichor_pouch:'ichorPouch'};
for(const piece of ['helm','chest','legs','boots'])for(const gem of [false,true])textures[`ichor_${piece}${gem?'_gem':''}`]=`ichorcloth${piece[0].toUpperCase()+piece.slice(1)}${gem?'Gem':''}`;
for(const tool of ['pick','shovel','axe','sword'])for(const gem of [false,true])textures[`ichor_${tool}${gem?'_gem':''}`]=`ichor${tool[0].toUpperCase()+tool.slice(1)}${gem?'Gem0':''}`;
const language={};for(const [lang,file]of [['en_us','en_US'],['zh_cn','zh_CN']])language[lang]=Object.fromEntries(fs.readFileSync(path.join(UP,`resources/assets/ttinkerer/lang/${file}.lang`),'utf8').split(/\r?\n/).filter(l=>l.includes('=')&&!l.startsWith('#')).map(l=>[l.slice(0,l.indexOf('=')),l.slice(l.indexOf('=')+1)]));
function copy(source,dest){const target=path.join(ROOT,'common/src/main/resources',dest);fs.mkdirSync(path.dirname(target),{recursive:true});fs.copyFileSync(path.join(UP,'resources/assets/ttinkerer',source),target);}
for(const [id,texture]of Object.entries(textures)){
    copy(`textures/items/${texture}.png`,`assets/thaumictinkerer/textures/item/${id}.png`);
    write(`assets/thaumictinkerer/models/item/${id}.json`,{parent:id.includes('pick')||id.includes('axe')||id.includes('sword')||id.includes('shovel')?'minecraft:item/handheld':'minecraft:item/generated',textures:{layer0:`${T}item/${id}`}});
    write(`assets/thaumictinkerer/items/${id}.json`,{model:{type:'minecraft:model',model:`${T}item/${id}`}},'neoforge-26.1.2/src/main/resources');
    catalog.items.push(id);
    let original=texture.replace(/Gem0$/,'Gem');
    if(id==='block_talisman')original='blockTalisman';
    for(const lang of ['en_us','zh_cn'])catalog.languages[lang][`item.thaumictinkerer.${id}`]=language[lang][`item.${original}.name`]||language.en_us[`item.${original}.name`]||id;
}
for(const tool of ['pick','shovel','axe','sword']){
    const id=`ichor_${tool}_gem`,original=`ichor${tool[0].toUpperCase()+tool.slice(1)}Gem`;
    const overrides=[];
    for(let mode=1;mode<=2;mode++){
        const variant=`${id}_mode_${mode}`;
        copy(`textures/items/${original}${mode}.png`,`assets/thaumictinkerer/textures/item/${variant}.png`);
        write(`assets/thaumictinkerer/models/item/${variant}.json`,{parent:'minecraft:item/handheld',textures:{layer0:T+'item/'+variant}});
        overrides.push({predicate:{'minecraft:custom_model_data':mode},model:T+'item/'+variant});
    }
    write(`assets/thaumictinkerer/models/item/${id}.json`,{parent:'minecraft:item/handheld',textures:{layer0:T+'item/'+id},overrides});
    write(`assets/thaumictinkerer/items/${id}.json`,{model:{type:'minecraft:range_dispatch',property:'minecraft:custom_model_data',entries:[1,2].map(mode=>({threshold:mode,model:{type:'minecraft:model',model:T+'item/'+id+'_mode_'+mode}})),fallback:{type:'minecraft:model',model:T+'item/'+id}}},'neoforge-26.1.2/src/main/resources');
}
for(const[old,name]of [['capIchor','wand_cap_ichor'],['rodIchorcloth','wand_rod_ichorcloth'],['ichor1','ichor_layer_1'],['ichor2','ichor_layer_2'],['ichorGem1','ichor_gem_layer_1'],['ichorGem2','ichor_gem_layer_2']])copy(`textures/model/${old}.png`,`assets/thaumictinkerer/textures/models/${name}.png`);
for(const [id,old]of [['ichor','ichor'],['ichor_gem','ichorGem']]){
    copy(`textures/model/${old}1.png`,`assets/thaumictinkerer/textures/models/armor/${id}_layer_1.png`);
    copy(`textures/model/${old}2.png`,`assets/thaumictinkerer/textures/models/armor/${id}_layer_2.png`);
    copy(`textures/model/${old}1.png`,`assets/thaumictinkerer/textures/entity/equipment/humanoid/${id}.png`);
    copy(`textures/model/${old}2.png`,`assets/thaumictinkerer/textures/entity/equipment/humanoid_leggings/${id}.png`);
    write(`assets/thaumictinkerer/equipment/${id}.json`,{layers:{humanoid:[{texture:T+id}],humanoid_leggings:[{texture:T+id}]}},'neoforge-26.1.2/src/main/resources');
}
for(let i=0;i<3;i++)copy(`textures/blocks/warpGate${i}.png`,`assets/thaumictinkerer/textures/block/kami_warp_gate_${i}.png`);
write('assets/thaumictinkerer/models/block/warp_gate.json',{parent:'minecraft:block/cube_bottom_top',textures:{top:T+'block/kami_warp_gate_0',bottom:T+'block/kami_warp_gate_1',side:T+'block/kami_warp_gate_1'}});
write('assets/thaumictinkerer/models/block/bedrock_portal.json',{parent:'minecraft:block/cube_all',textures:{all:'minecraft:block/crying_obsidian'}});
for(const id of ['warp_gate','bedrock_portal']){
    write(`assets/thaumictinkerer/blockstates/${id}.json`,{variants:{'':{model:T+'block/'+id}}});
    write(`assets/thaumictinkerer/models/item/${id}.json`,{parent:T+'block/'+id});
    write(`assets/thaumictinkerer/items/${id}.json`,{model:{type:'minecraft:model',model:T+'item/'+id}},'neoforge-26.1.2/src/main/resources');
    catalog.items.push(id);
}
write('assets/thaumictinkerer/models/block/kami_light.json',{elements:[]});
write('assets/thaumictinkerer/blockstates/kami_light.json',{variants:{'':{model:T+'block/kami_light'}}});
write('data/thaumictinkerer/loot_table/blocks/warp_gate.json',{type:'minecraft:block',pools:[{rolls:1,entries:[{type:'minecraft:item',name:T+'warp_gate'}],conditions:[{condition:'minecraft:survives_explosion'}]}]});
function shaped(id,pattern,key,cost,research=id,count=1){write(`data/thaumictinkerer/recipe/${id}.json`,{type:G+'arcane_workbench_shaped',pattern,key:Object.fromEntries(Object.entries(key).map(([k,v])=>[k,ingredient(v)])),crystals:aspects(cost).map(a=>({...a,amount:1})),vis:Math.max(...Object.values(cost)),result:{id:T+id,count},research:{entry:T+research}});catalog.recipes.push(id);}
function infusion(id,instability,cost,catalyst,components,research=id,count=1){write(`data/thaumictinkerer/recipe/${id}.json`,{type:G+'infusion',aspects:aspects(cost),catalyst:ingredient(catalyst),components:components.map(ingredient),instability,research:{entry:T+research},result:{id:T+id,count}});catalog.recipes.push(id);}
function research(id,old,icon,parents,x,y,cost,recipes=[id],text){
    const entry={category:T+'tinkering',name:`research.thaumictinkerer.${id}.title`,parents:parents.map(p=>p.includes(':')?p:T+p),column:x,row:y,icons:[T+icon],note_aspects:aspects(cost),complexity:3,stages:[{text:`research.thaumictinkerer.${id}.text`,recipes:recipes.map(r=>T+r)}]};
    write(`data/thaumictinkerer/thaumaturge/research_entry/${id}.json`,entry);catalog.research.push(id);catalog.coverage[old]=T+id;
    for(const lang of ['en_us','zh_cn']){
        catalog.languages[lang][`research.thaumictinkerer.${id}.title`]=language[lang][`ttresearch.name.${old}`]||catalog.languages[lang][`item.thaumictinkerer.${icon}`]||language.en_us[`ttresearch.name.${old}`]||id;
        const original=language[lang][`ttresearch.page.${old}.0`]||language.en_us[`ttresearch.page.${old}.0`]||'';
        catalog.languages[lang][`research.thaumictinkerer.${id}.text`]=text?.[lang]||original.replace(/<BR>/gi,'\n').replace(/<[^>]+>/g,'').replace(/Pressing U will toggle this armors effects\./g,'Sneak-use the item to toggle its effects.');
    }
}
const allPrimals={FIRE:100,EARTH:100,WATER:100,AIR:100,ORDER:100,ENTROPY:100};
shaped('ichor_cap',['ICI',' M ','ICI'],{M:T+'ichorium',I:T+'ichor',C:G+'wand_cap_thaumium'},allPrimals,'ichor_cap',2);
research('ichor_cap','CAP_ICHOR','ichor_cap',['ichorium'],11,11,{TOOL:2,METAL:1,LIGHT:1,MAGIC:1});
infusion('ichorcloth_rod',9,{MAGIC:100,LIGHT:32,TOOL:32},G+'wand_rod_silverwood',[T+'ichor',T+'ichor_cloth',G+'salis_mundus',M+'ghast_tear',G+'salis_mundus',T+'ichor_cloth']);
research('ichorcloth_rod','ROD_ICHORCLOTH','ichorcloth_rod',['ichor_cloth','ichor_cap'],14,2,{TOOL:2,CLOTH:1,LIGHT:1,MAGIC:1});
const armorRecipes=[];
for(const [piece,pattern,element]of [['helm',['CCC','C C'],'WATER'],['chest',['C C','CCC','CCC'],'AIR'],['legs',['CCC','C C','C C'],'FIRE'],['boots',['C C','C C'],'EARTH']]){shaped('ichor_'+piece,pattern,{C:T+'ichor_cloth'},{[element]:75},'ichorcloth_armor');armorRecipes.push('ichor_'+piece);}
research('ichorcloth_armor','ICHORCLOTH_ARMOR','ichor_helm',['ichor_cloth'],17,5,{ARMOR:2,CLOTH:1,LIGHT:1,CRAFT:1},armorRecipes);
const armour={
helm:{xy:[18,3],notes:{WATER:2,HEAL:1,HUNGER:1,AURA:1},cost:{WATER:50,ARMOR:32,HUNGER:32,AURA:32,LIGHT:64,FLESH:16,MIND:16},extra:[G+'thaumonomicon',G+'focus_3',M+'golden_helmet',M+'potion',G+'goggles_revealing',T+'cleansing_talisman',M+'cod',M+'cake',M+'ender_eye']},
chest:{xy:[17,7],notes:{AIR:2,MOTION:1,FLIGHT:1,ELDRITCH:1},cost:{AIR:50,ARMOR:32,FLIGHT:32,ORDER:32,LIGHT:64,ELDRITCH:16,SENSES:16},extra:[G+'focus_3',G+'thaumonomicon',M+'golden_chestplate',T+'focus_flight',G+'thaumostatic_harness',T+'focus_deflect',M+'feather',M+'firework_rocket',M+'arrow']},
legs:{xy:[17,9],notes:{FIRE:2,HEAL:1,GREED:1,ENERGY:1},cost:{FIRE:50,ARMOR:32,HEAL:32,ENERGY:32,LIGHT:64,GREED:16,ELDRITCH:16},extra:[G+'focus_3',G+'thaumonomicon',M+'golden_chestplate',M+'potion',T+'focus_smelt',T+'bright_nitor',M+'lava_bucket',M+'fire_charge',M+'blaze_rod']},
boots:{xy:[15,10],notes:{EARTH:2,TRAVEL:1,MINE:1,PLANT:1},cost:{EARTH:50,ARMOR:32,MINE:32,MOTION:32,LIGHT:64,PLANT:16,TRAVEL:16},extra:[G+'thaumonomicon',G+'focus_3',M+'golden_boots',M+'grass_block',G+'arcane_bore',G+'lamp_growth',M+'wheat_seeds',M+'white_wool',M+'lead']},
};
for(const[piece,v]of Object.entries(armour)){const id='ichor_'+piece+'_gem';infusion(id,13,v.cost,T+'ichor_'+piece,[M+'diamond',T+'ichor',T+'ichor',...v.extra]);research(id,`ICHORCLOTH_${piece.toUpperCase()}_GEM`,id,['ichorcloth_armor'],...v.xy,v.notes);}
const tools=[];
for(const[kind,pattern,element]of [['pick',['III',' R ',' R '],'FIRE'],['shovel',[' I ',' R ',' R '],'EARTH'],['axe',['II ','IR ',' R '],'WATER'],['sword',[' I ',' I ',' R '],'AIR']]){shaped('ichor_'+kind,pattern,{I:T+'ichorium',R:G+'wand_rod_silverwood'},{[element]:75},'ichor_tools');tools.push('ichor_'+kind);}
research('ichor_tools','ICHOR_TOOLS','ichor_pick',['ichorium','ichorcloth_rod'],13,12,{TOOL:2,WEAPON:1,METAL:1,CRAFT:1},tools);
const gemTools={
pick:{xy:[13,15],notes:{FIRE:2,TOOL:1,MINE:1,EARTH:1},cost:{FIRE:50,MINE:64,METAL:32,EARTH:32,HARVEST:32,GREED:16,SENSES:16},elemental:'elemental_pickaxe'},
shovel:{xy:[15,15],notes:{EARTH:3,TOOL:1,MINE:1},cost:{EARTH:82,MINE:64,TOOL:32,HARVEST:32,TRAP:16,SENSES:16},elemental:'elemental_shovel'},
axe:{xy:[16,14],notes:{WATER:2,TOOL:1,TREE:1,CROP:1},cost:{WATER:50,MINE:64,TOOL:32,TREE:32,HARVEST:32,CROP:16,SENSES:16},elemental:'elemental_axe'},
sword:{xy:[16,12],notes:{AIR:2,WEAPON:1,SOUL:1,HUNGER:1},cost:{AIR:50,HUNGER:64,SOUL:32,WEAPON:32,ENERGY:32,ORDER:16,CRYSTAL:16},elemental:'elemental_sword'},
};
for(const[kind,v]of Object.entries(gemTools)){const id='ichor_'+kind+'_gem';const focus=G+'focus_2';infusion(id,15,v.cost,T+'ichor_'+kind,[T+'ichorium',T+'ichor',G+v.elemental,focus,kind==='sword'?M+'cactus':M+'tnt',G+'cluster_cinnabar',G+'cluster_iron',G+'cluster_gold',M+'diamond',focus,G+v.elemental,T+'ichor_cloth']);research(id,'ICHOR_'+kind.toUpperCase()+'_GEM',id,['ichor_tools'],...v.xy,v.notes);}
infusion('cat_amulet',8,{DARKNESS:16,ORDER:32,MIND:16},M+'quartz_block',[T+'ichor',M+'gold_ingot',M+'gold_ingot',M+'cocoa_beans',M+'jungle_leaves',M+'cod']);
research('cat_amulet','CAT_AMULET','cat_amulet',['ichorium'],13,10,{MIND:2,ORDER:1,DARKNESS:1,DEATH:1});
infusion('ichor_pouch',9,{VOID:64,MAN:32,CLOTH:32,ELDRITCH:32,AIR:64},G+'focus_pouch',[T+'ichor_cloth',G+'focus_3',M+'diamond',T+'ichor_cloth',G+'hungry_chest',G+'jar_void']);
research('ichor_pouch','ICHOR_POUCH','ichor_pouch',['ichor_cloth'],13,6,{VOID:2,CLOTH:1,ELDRITCH:1,MAN:1},['ichor_pouch'],{en_us:'A 117-slot pouch that accepts ordinary items and prevents nested pouches. Use to open the current page; sneak-use to cycle between pages 1, 2 and 3 (54, 54 and 9 slots). Equip in a Curios belt slot when not in use.',zh_cn:'可容纳普通物品的 117 格无底袋，禁止嵌套存放。使用打开当前页，潜行使用依次切换三页（54、54、9 格）。平时可装备在 Curios 腰带栏。'});
infusion('block_talisman',9,{VOID:65,DARKNESS:32,MAGIC:50,ELDRITCH:32},G+'focus_3',[T+'ichor',M+'ender_chest',M+'diamond',T+'ichor',G+'tainted_goo',G+'jar_void']);
research('block_talisman','BLOCK_TALISMAN','block_talisman',['ichor_pick_gem','ichor_shovel_gem'],14,17,{VOID:2,DARKNESS:1,ELDRITCH:1,MAGIC:1},['block_talisman'],{en_us:'Sneak-use a block to attune this ring while empty, then equip it in a Curios ring slot. It absorbs matching inventory blocks up to 2,147,483,647 while retaining and refilling one ordinary stack for building. Use a surface to place a stored block; sneak-use in the air to withdraw a stack. Blocks with custom components are not absorbed.',zh_cn:'空戒指潜行使用方块即可绑定，装备在 Curios 戒指栏后自动吸收背包中同类方块，最多存放 2,147,483,647 个，并保留、补满一组供普通建造使用。对表面使用放置一块；潜行对空使用取出一组。不会吸收带自定义组件的方块。'});
infusion('placement_mirror',12,{CRAFT:65,CRYSTAL:32,MAGIC:50,MIND:32},T+'block_talisman',[T+'ichor',M+'dropper',M+'diamond',M+'glass',M+'blaze_powder',T+'ichor']);
research('placement_mirror','PLACEMENT_MIRROR','placement_mirror',['block_talisman'],17,16,{CRAFT:2,CRYSTAL:1,ELDRITCH:1,MIND:1},['placement_mirror'],{en_us:'Sneak-use a block to select it. Sneak-use in the air cycles the square size from 3 to 11. Use a surface to build a plane along its face, consuming blocks from your inventory or Black Hole Rings. Block placement respects world borders and placement cancellation.',zh_cn:'潜行使用方块以选择材料；潜行对空使用可将边长在 3 至 11 之间切换。对表面使用可沿该面建造平面，消耗背包或黑洞戒指中的方块，遵守世界边界和放置事件。'});
infusion('protoclay',4,{MINE:16,TOOL:16},M+'clay_ball',[M+'dirt',M+'stone',M+'oak_log',T+'ender_shard']);
research('protoclay','PROTOCLAY','protoclay',['ichor_pick_gem','ichor_shovel_gem'],12,17,{TOOL:2,MINE:1,MAN:1,MECHANISM:1});
infusion('warp_gate',8,{TRAVEL:64,ELDRITCH:50,FLIGHT:50},G+'paving_stone_travel',[T+'ichor',T+'ender_shard',T+'transvector_dislocator',T+'nether_shard',M+'diamond',M+'feather']);
infusion('sky_pearl',6,{TRAVEL:32,ELDRITCH:32,FLIGHT:32,AIR:16},M+'ender_pearl',[T+'ichor',T+'ender_shard',M+'lapis_block',M+'diamond'],'warp_gate',2);
research('warp_gate','WARP_GATE','warp_gate',['ichor_chest_gem','ichor_boots_gem'],19,6,{TRAVEL:2,ELDRITCH:1,FLIGHT:1,MECHANISM:1},['warp_gate','sky_pearl'],{en_us:'Use an unattuned Celestial Pearl on a gateway to bind it. Sneak-use a bound pearl on another gateway to insert a destination (10 slots). Sneak-use an empty hand to cycle destination, then use to teleport. Sneak-use redstone to lock arrivals; sneak-use a pearl in the air to clear its attunement. Destinations must be in the same dimension and have clear space. Breaking a gateway drops its pearls.',zh_cn:'对天体网关使用未绑定的天体珍珠以绑定位置。潜行使用已绑定珍珠插入另一网关（10 个目的地栏位）。空手潜行使用切换目的地，普通使用传送；持红石潜行使用切换到达锁。潜行对空使用珍珠清除绑定。目的地必须处于同一维度且上方空间畅通；破坏网关会掉落珍珠。'});
catalog.coverage.ItemIchorclothArmor='kami.KamiArmorItem';catalog.coverage.ItemIchorclothArmorAdv='kami.KamiArmorItem';
for(const kind of ['Pick','Shovel','Axe','Sword']){catalog.coverage['ItemIchor'+kind]='kami.KamiToolItem';catalog.coverage['ItemIchor'+kind+'Adv']='kami.KamiToolItem + KamiEvents';}
for(const piece of ['Helm','Chest','Legs','Boots'])catalog.coverage['ItemGem'+piece]='kami.KamiArmorItem + KamiEvents';
for(const cls of ['CatAmulet','BlockTalisman','PlacementMirror','Protoclay'])catalog.coverage['Item'+cls]='kami.KamiUtilityItem + KamiEvents';
Object.assign(catalog.coverage,{ItemIchorPouch:'kami.KamiPouchItem',ItemSkyPearl:'kami.KamiPearl',RodIchorcloth:'Thaumaturge WandRod registry: thaumictinkerer:ichorcloth',CapIchor:'Thaumaturge WandCap registry: thaumictinkerer:ichor',BlockWarpGate:'kami.KamiGateBlock/KamiGateBlockEntity',BlockBedrockPortal:'kami.KamiEvents + bedrock_portal',ChunkProviderBedrock:'dimension/bedrock.json',OreClusterGenerator:'kami.KamiOreFeature',EnumOreFrequency:'kami.KamiOreFeature',SoulHeartHandler:'native absorption and kami_soul_hearts attribute',KamiArmorHandler:'per-stack tt_disabled toggle'});
Object.assign(catalog.coverage,{IAdvancedTool:'kami.KamiToolItem kind/awakened fields',ToolHandler:'kami.KamiEvents.breakBlock',KamiDimensionHandler:'kami.KamiEvents.leftClick',TileBedrockPortal:'kami.KamiEvents.tick',TileWarpGate:'kami.KamiGateBase/KamiGateBlockEntity',ItemBlockWarpGate:'vanilla BlockItem registration',WorldProviderBedrock:'version-specific dimension_type/bedrock.json',TeleporterBedrock:'kami.KamiVersion.teleport'});
write('data/thaumictinkerer/dimension/bedrock.json',{type:T+'bedrock',generator:{type:'minecraft:flat',settings:{biome:T+'bedrock',features:true,lakes:false,layers:[{height:256,block:'minecraft:bedrock'}],structure_overrides:[]}}});
write('data/thaumictinkerer/worldgen/configured_feature/bedrock_ores.json',{type:T+'bedrock_ores',config:{}});
write('data/thaumictinkerer/worldgen/placed_feature/bedrock_ores.json',{feature:T+'bedrock_ores',placement:[{type:'minecraft:in_square'}]});
const biome={has_precipitation:false,temperature:.8,downfall:0,effects:{fog_color:0,sky_color:0,water_color:4159204,water_fog_color:329011},spawners:{},spawn_costs:{},carvers:{},features:[[],[],[],[],[],[],[T+'bedrock_ores']]};
write('data/thaumictinkerer/worldgen/biome/bedrock.json',biome,'neoforge-1.21.1/src/main/resources');
write('data/thaumictinkerer/worldgen/biome/bedrock.json',{...biome,attributes:{'minecraft:visual/sky_color':'#000000'},carvers:[],effects:{water_color:'#3f76e4'}},'neoforge-26.1.2/src/main/resources');
const dim={ultrawarm:false,natural:false,piglin_safe:false,respawn_anchor_works:false,bed_works:false,has_raids:false,has_skylight:false,has_ceiling:true,coordinate_scale:1,ambient_light:0,logical_height:256,min_y:0,height:256,infiniburn:'#minecraft:infiniburn_overworld',effects:'minecraft:the_end',fixed_time:18000,monster_spawn_light_level:0,monster_spawn_block_light_limit:0};
write('data/thaumictinkerer/dimension_type/bedrock.json',dim,'neoforge-1.21.1/src/main/resources');
write('data/thaumictinkerer/dimension_type/bedrock.json',{ambient_light:0,coordinate_scale:1,has_ceiling:true,has_ender_dragon_fight:false,has_fixed_time:true,has_skylight:false,height:256,logical_height:256,min_y:0,infiniburn:'#minecraft:infiniburn_overworld',monster_spawn_light_level:0,monster_spawn_block_light_limit:0,skybox:'none',attributes:{'minecraft:gameplay/bed_rule':{can_set_spawn:'never',can_sleep:'never',explodes:true},'minecraft:gameplay/can_start_raid':false,'minecraft:gameplay/respawn_anchor_works':false,'minecraft:visual/sky_color':'#000000','minecraft:visual/fog_color':'#000000','minecraft:visual/sky_light_factor':0}},'neoforge-26.1.2/src/main/resources');
write('data/curios/tags/item/kami_necklace.json',{values:[T+'cat_amulet']});
write('data/curios/tags/item/kami_belt.json',{values:[T+'ichor_pouch']});
write('data/curios/tags/item/kami_ring.json',{values:[T+'block_talisman']});
// Parent merges these entries into the actual Curios slot tags, keeping generators independent.
catalog.curios={necklace:[T+'cat_amulet'],belt:[T+'ichor_pouch'],ring:[T+'block_talisman']};
for(const [lang,d]of Object.entries(catalog.languages)){
    Object.assign(d,{
        'block.thaumictinkerer.warp_gate':lang==='zh_cn'?'天体网关':'Celestial Gateway','block.thaumictinkerer.bedrock_portal':lang==='zh_cn'?'基岩传送门':'Bedrock Portal',
        'container.thaumictinkerer.ichor_pouch':lang==='zh_cn'?'无底袋 · 第 %s 页':'Bottomless Pouch · Page %s',
        'message.thaumictinkerer.pouch_page':lang==='zh_cn'?'无底袋：第 %s 页':'Pouch: page %s',
        'message.thaumictinkerer.gate_locked':lang==='zh_cn'?'到达锁定：%s':'Arrival lock: %s',
        'message.thaumictinkerer.gate_destination':lang==='zh_cn'?'目的地：%s / 10':'Destination: %s / 10',
        'message.thaumictinkerer.gate_unavailable':lang==='zh_cn'?'无法到达：珍珠为空、网关锁定或目的地空间阻塞。':'Destination unavailable: missing pearl or gateway, locked arrivals, or obstructed space.',
    });
    for(const kind of ['pick','shovel','axe','sword'])for(let i=0;i<3;i++)d[`message.thaumictinkerer.tool_mode.${kind}.${i}`]=language[lang][`ttmisc.mode.${kind}.${i}`]||language.en_us[`ttmisc.mode.${kind}.${i}`];
}
catalog.adaptations=[
'The Thaumaturge modular focus tiers replace removed Thaumcraft excavation/frost/primal/portable-hole component items in the original infusion recipes; original instability and aspect costs are retained and retired aspects are merged through the parent mapping.',
'The 117-slot bottomless pouch uses three vanilla chest pages (54 + 54 + 9); sneak-use cycles pages. The custom 1.7 scrolling screen is replaced.',
'Gate destinations retain 10 stored pearls, same-dimension validation, arrival locking and pearl drops. Direct server-authoritative hand interactions replace the two old gateway GUI screens.',
'Old Baubles slots map to Curios necklace, belt and ring tags. Armor effects toggle per stack by sneak-use; no U-key dependency.',
'Soul hearts use Minecraft absorption with a 20-point maximum so damage consumption and HUD sync are handled by the game.',
'The bedrock dimension retains the 256-high solid bedrock mass, a weighted ore type per column, and 200 vein attempts. Ore Dictionary categories map to c:ores/<material> tags; only installed categories participate. Vein geometry uses bounded random walks instead of the removed WorldGenMinable.',
'Armor uses the native armor rendering pipeline with original robe and awakened textures and original armor points (3/8/6/3), enchantability 20, and no toughness. Custom animated wings and the mirror placement ghost are not yet rendered.',
'Legacy metadata dependencies resolve to arcane bore, lamp of growth, travel paving stone, tainted goo and native cinnabar/iron/gold clusters. Original potion components accept a potion item without a required effect.',
];
write('catalog/kami.json',catalog,'.');
console.log(`KAMI: ${catalog.items.length} items/blocks, ${catalog.recipes.length} recipes, ${catalog.research.length} research entries; catalog/kami.json contains language and coverage records.`);
