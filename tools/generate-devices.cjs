/* Device data and credited original assets; CC BY-NC-SA 3.0. Never writes another module's files. */
const fs=require('fs'),path=require('path');
const {ROOT,aspects,ingredient,write}=require('./resource-helpers.cjs');
const source=path.resolve(ROOT,'../.thaumic-tinkerer-1.7.10-upstream/src/main/resources/assets/ttinkerer');
const ns='thaumictinkerer',base=`assets/${ns}`;
const languages={en_us:{},zh_cn:{}};
const coverage={},adaptations=[
  'Original GUIs are replaced by documented right-click and sneak-click controls; machines retain saved inventories and server logic.',
  'Arcane recipes use one crystal for each required primal and the original total primal cost as modern workbench Vis.',
  'Old disappeared aspects are mapped through resource-helpers.cjs; soul aspect identities retain their original distinct names.',
  'Golem connector controls owned Thaumaturge golems home/navigation and reports health/location. Legacy ComputerCraft/OpenComputers peripherals and old Thaumcraft marker/decorations APIs have no modern integration.',
  'Tool dynamism acts every second while powered using an owner-identified FakePlayer. Original animated tool rendering is replaced by a textured block model.',
  'Osmotic enchanter uses all supported catalogue enchantments, validates six pillars, pays the selected Vis cost at start and completes after five seconds.',
  'Levitational locomotive links to a relay within 32 blocks, carries the upper block and its block-entity data, and reverses when obstructed. The original two-relay track topology is adapted to a single bound relay.',
  'Elemental fires retain original transformations, add modern deepslate/copper ore equivalents and conventional ore/storage block tag integration, age and spread locally over transformable blocks. Existing block entities except the original earth-fire spawner-to-iron transformation are excluded to preserve inventories.',
];
function lang(key,en,zh){languages.en_us[key]=en;languages.zh_cn[key]=zh;}
function copy(relative,target){const dst=path.join(ROOT,'common/src/main/resources',target);fs.mkdirSync(path.dirname(dst),{recursive:true});fs.copyFileSync(path.join(source,relative),dst);if(fs.existsSync(path.join(source,relative+'.mcmeta')))fs.copyFileSync(path.join(source,relative+'.mcmeta'),dst+'.mcmeta');}
function itemModel(id,texture){copy(`textures/items/${texture}.png`,`${base}/textures/item/${id}.png`);write(`${base}/models/item/${id}.json`,{parent:'minecraft:item/generated',textures:{layer0:`${ns}:item/${id}`}});modern(id);}
function modern(id){write(`${base}/items/${id}.json`,{model:{type:'minecraft:model',model:`${ns}:item/${id}`}},'neoforge-26.1.2/src/main/resources');}
function block(id,texture,display){write(`data/${ns}/loot_table/blocks/${id}.json`,{type:'minecraft:block',pools:[{rolls:1,entries:[{type:'minecraft:item',name:`${ns}:${id}`}],conditions:[{condition:'minecraft:survives_explosion'}]}]});lang(`block.${ns}.${id}`,display[0],display[1]);}
const machines={
  repairer:['model/repairer','Thaumic Restorer','神秘修复台','TileRepairer'],
  funnel:['blocks/funnel0','Essentia Funnel','精华漏斗','TileFunnel'],
  magnet:['model/magnetN','Kinetic Attractor','动能磁铁','TileMagnet'],
  mob_magnet:['model/mobMagnetN','Corporeal Attractor','生灵磁铁','TileMobMagnet'],
  enchanter:['blocks/enchanter1','Osmotic Enchanter','渗透附魔台','TileEnchanter'],
  transvector_interface:['blocks/interface','Transvector Interface','向量接口','transvector/TileTransvectorInterface'],
  transvector_dislocator:['blocks/dislocator0','Transvector Dislocator','向量移位器','transvector/TileTransvectorDislocator'],
  remote_placer:['blocks/remotePlacer0','Remote Placer','遥控放置器','TileRPlacer'],
  golem_connector:['blocks/golemConnector','Golem Connector','傀儡连接器','TileGolemConnector'],
  animation_tablet:['blocks/animationTablet0','Tool Dynamism Tablet','工具活化石板','tablet/TileAnimationTablet'],
  aspect_analyzer:['blocks/aspectAnalyzer0','Aspect Analyzer','要素分析器','TileAspectAnalyzer'],
  platform:['blocks/platform','Translucent Platform','半透平台','BlockPlatform'],
  camo:['blocks/platform','Camouflaged Block','迷彩方块','TileCamo'],
  mobilizer:['blocks/Levitational Locomotive0','Levitational Locomotive','浮空机车','TileEntityMobilizer'],
  mobilizer_relay:['blocks/Levitational Locomotive Relay0','Levitational Locomotive Relay','浮空机车中继','TileEntityRelay'],
  summon_tablet:['blocks/spawner1','Summoning Tablet','灵魂召唤石板','TileSummon'],
  forcefield:['blocks/emptyTexture','Temporary Forcefield','临时力场','TileForcefield'],
};
for(const [id,[texture,en,zh,original]] of Object.entries(machines)){block(id,texture,[en,zh]);coverage[original]=`device/MachineBlockEntity.java (${id})`;}
require('./generate-device-models.cjs').generateDeviceModels();
write(`data/${ns}/tags/block/devices.json`,{replace:false,values:Object.keys(machines).filter(id=>id!=='forcefield').map(id=>`${ns}:${id}`)});
// A forcefield is a temporary auxiliary block and drops nothing.
write(`data/${ns}/loot_table/blocks/forcefield.json`,{type:'minecraft:block',pools:[]});
itemModel('connector','connector');lang(`item.${ns}.connector`,'Transvector Binder','向量绑定器');coverage.ItemConnector='device/ConnectorItem.java';
function arcane(id,pattern,key,cost,research=id,count=1){const primals={};for(const k of Object.keys(cost))primals[k]=1;write(`data/${ns}/recipe/${id}.json`,{type:'thaumaturge:arcane_workbench_shaped',pattern,key:Object.fromEntries(Object.entries(key).map(([k,v])=>[k,ingredient(v)])),crystals:aspects(primals),vis:Object.values(cost).reduce((a,b)=>a+b,0),result:{id:`${ns}:${id}`,count},research:{entry:`${ns}:${research}`}});}
function infusion(id,catalyst,components,cost,instability,research=id){write(`data/${ns}/recipe/${id}.json`,{type:'thaumaturge:infusion',catalyst:ingredient(catalyst),components:components.map(ingredient),aspects:aspects(cost),instability,result:{id:`${ns}:${id}`},research:{entry:`${ns}:${research}`}});}
function crucible(id,catalyst,cost,research=id){write(`data/${ns}/recipe/${id}.json`,{type:'thaumaturge:crucible',catalyst:ingredient(catalyst),aspects:aspects(cost),result:{id:`${ns}:${id}`},research:{entry:`${ns}:${research}`}});}
arcane('connector',[' I ',' WI','S  '],{I:'minecraft:iron_ingot',W:'minecraft:stick',S:'thaumaturge:crystal_ordo'},{ordo:2},'transvector_interface');
arcane('transvector_interface',['BRB','LEL','BRB'],{B:'thaumaturge:stone_arcane',R:'minecraft:redstone',L:'minecraft:lapis_lazuli',E:'minecraft:ender_pearl'},{ordo:12,perditio:16});
arcane('transvector_dislocator',[' M ',' I ',' C '],{M:'thaumaturge:mirrored_glass',I:`${ns}:transvector_interface`,C:'minecraft:comparator'},{terra:5,perditio:5});
arcane('animation_tablet',['GIG','ICI'],{G:'minecraft:gold_ingot',I:'minecraft:iron_ingot',C:'thaumaturge:golem'},{aer:25,ordo:15,ignis:10});
for(const [id,metal] of [['magnet','minecraft:iron_ingot'],['mob_magnet','minecraft:copper_ingot']])arcane(id,[' I ','SIs','WFW'],{I:metal,S:'thaumaturge:crystal_aer',s:'thaumaturge:crystal_terra',W:'thaumaturge:log_greatwood',F:`${ns}:focus_telekinesis`},{aer:20,ordo:5,terra:15,perditio:5},'mob_magnet');
arcane('funnel',['STS'],{S:'minecraft:stone',T:'thaumaturge:ingot_thaumium'},{ordo:1,perditio:1});
infusion('repairer','thaumaturge:metal_thaumium',['minecraft:iron_ingot','minecraft:gold_ingot','minecraft:diamond','minecraft:cobblestone','minecraft:oak_planks','minecraft:leather','thaumaturge:fabric','thaumaturge:ingot_thaumium'],{instrumentum:15,fabrico:20,ordo:10,praecantatio:15},8);
infusion('enchanter','minecraft:enchanting_table',[...Array(5).fill('thaumaturge:stone_ancient_tile'),'thaumaturge:ingot_thaumium','thaumaturge:ingot_thaumium',`${ns}:spell_cloth`],{praecantatio:50,potentia:20,alienis:20,vacuos:20,cognitio:10},15,'osmotic_enchanter');
arcane('remote_placer',['ses','sds','sss'],{s:'thaumaturge:stone_arcane',e:'minecraft:ender_pearl',d:'minecraft:dispenser'},{aer:20,ordo:5,terra:15,perditio:5});
arcane('golem_connector',['WFW','sIs','WFW'],{W:'thaumaturge:log_greatwood',F:'minecraft:redstone_block',s:'minecraft:ender_pearl',I:'thaumaturge:golem_bell'},{aer:20,ordo:5,perditio:15});
arcane('aspect_analyzer',['TWT','WMW','TWT'],{T:'thaumaturge:ingot_thaumium',W:'thaumaturge:plank_greatwood',M:'thaumaturge:thaumometer'},{ordo:1,perditio:1});
arcane('platform',[' S ','G G'],{S:'thaumaturge:arcane_pressure_plate',G:'thaumaturge:plank_greatwood'},{aer:2,perditio:4},'platform',2);
arcane('camo',['SSS','SPS','SSS'],{S:'minecraft:oak_planks',P:`${ns}:platform`},{ordo:2,perditio:2},'platform');
infusion('mobilizer','thaumaturge:levitator',['minecraft:iron_ingot','minecraft:feather','minecraft:iron_ingot','thaumaturge:stone_ancient_tile'],{motus:15,ordo:20,praecantatio:15},4);
arcane('mobilizer_relay',['WFW','SIs','WFW'],{W:'thaumaturge:log_greatwood',F:'minecraft:glass',S:'thaumaturge:crystal_aer',s:'thaumaturge:crystal_terra',I:'minecraft:iron_ingot'},{aer:20,ordo:5,terra:15},'mobilizer');
arcane('summon_tablet',['WWW','SSS'],{W:'thaumaturge:stone_ancient_tile',S:'minecraft:stone'},{ordo:50,perditio:50});
const elementNames={aer:['Air','风'],ignis:['Fire','火'],terra:['Earth','地'],aqua:['Water','水'],ordo:['Order','秩序'],perditio:['Entropy','混沌']};
const fireTextures={aer:'Air',ignis:'Fire',terra:'Earth',aqua:'Water',ordo:'Order',perditio:'Chaos'};
for(const [aspect,[en,zh]] of Object.entries(elementNames)){
  const id=`fire_${aspect}`;
  for(const layer of [0,1])copy(`textures/blocks/fire${fireTextures[aspect]}_layer_${layer}.png`,`${base}/textures/block/${id}_${layer}.png`);
  write(`${base}/models/block/${id}.json`,{parent:'minecraft:block/fire_floor',render_type:'minecraft:cutout',textures:{fire:`${ns}:block/${id}_0`}});
  write(`${base}/blockstates/${id}.json`,{variants:{'':{model:`${ns}:block/${id}`}}});
  copy(`textures/blocks/${aspect}Fire.png`,`${base}/textures/item/${id}.png`);write(`${base}/models/item/${id}.json`,{parent:'minecraft:item/generated',textures:{layer0:`${ns}:item/${id}`}});modern(id);
  const fireCost={ignis:5,praecantatio:5};fireCost[aspect]=(fireCost[aspect]||0)+5;
  lang(`block.${ns}.${id}`,`${en} Infused Fire`,`${zh}之焰`);crucible(id,`${ns}:bright_nitor`,fireCost);
  coverage[`BlockFire${fireTextures[aspect]}`]=`device/ElementalFireBlock.java (${aspect})`;
}
for(const aspect of ['aer','ignis','terra','aqua']){
  const [en,zh]=elementNames[aspect],crop=`infused_crop_${aspect}`,seed=`infused_seeds_${aspect}`,grain=`infused_grain_${aspect}`;
  const variants={};for(let age=0;age<8;age++){
    const stage=Math.min(3,Math.floor(age/2));const model=`${crop}_${stage}`;
    copy(`textures/blocks/crop_${aspect}_${stage}.png`,`${base}/textures/block/${model}.png`);
    write(`${base}/models/block/${model}.json`,{parent:'minecraft:block/crop',render_type:'minecraft:cutout',textures:{crop:`${ns}:block/${model}`}});variants[`age=${age}`]={model:`${ns}:block/${model}`};
  }
  write(`${base}/blockstates/${crop}.json`,{variants});
  itemModel(seed,`seed_${aspect}`);itemModel(grain,`fruit_${aspect}`);
  lang(`item.${ns}.${seed}`,`${en} Infused Seeds`,`${zh}之注魔种子`);lang(`item.${ns}.${grain}`,`${en} Infused Grain`,`${zh}之注魔谷物`);
  lang(`block.${ns}.${crop}`,`${en} Infused Crop`,`${zh}之注魔作物`);
  infusion(seed,'minecraft:wheat_seeds',Array(4).fill(`thaumaturge:crystal_${aspect}`),{messis:32,meto:32},5,'infused_crops');
  write(`data/${ns}/loot_table/blocks/${crop}.json`,{type:'minecraft:block',pools:[
    {rolls:1,entries:[{type:'minecraft:item',name:`${ns}:${seed}`} ]},
    {rolls:1,conditions:[{condition:'minecraft:block_state_property',block:`${ns}:${crop}`,properties:{age:'7'}}],entries:[{type:'minecraft:item',name:`${ns}:${grain}`} ]},
    {rolls:1,conditions:[{condition:'minecraft:block_state_property',block:`${ns}:${crop}`,properties:{age:'7'}}],entries:[{type:'minecraft:item',name:`${ns}:${seed}`,functions:[{function:'minecraft:set_count',count:{type:'minecraft:uniform',min:0,max:3}}]}]},
  ]});
}
coverage.BlockInfusedGrain='device/InfusedCropBlock.java (four elements)';coverage.ItemInfusedSeeds='device/SeedItem.java';coverage.ItemInfusedGrain='device/DeviceContent.java (four grain items)';
const instructions={
  repairer:['Insert a damaged tool with a right-click; an empty hand retrieves it. Attach an essentia tube or jar to the facing side. Every half-second one Instrumentum repairs 8 durability, Fabrico 5, or Ordo 3. Hoppers can insert and remove tools.','右键放入受损工具，空手右键取回。朝向的一侧接精华管或罐。每半秒消耗1点工具修复8耐久、工艺修复5耐久，或秩序修复3耐久。支持漏斗输入输出。'],
  funnel:['Right-click with an essentia jar to insert it; empty-hand right-click retrieves it. Place a hopper underneath, pointing into an essentia jar. The funnel drains one essentia every quarter-second into that jar. A jar directly underneath also works.','手持精华罐右键放入，空手右键取出。下方放置朝向目标精华罐的漏斗，每四分之一秒输送1点精华。也可直接放在目标罐上方。'],
  mob_magnet:['Supply redstone power: range is half the signal strength. The kinetic attractor moves item entities; the corporeal attractor moves living mobs. Sneak-right-click empty-handed to switch attraction/repulsion. Insert a filled Soul Mould to filter a species; sneak-click with wheat toggles adult/young animals.','输入红石信号后，半径为信号强度的一半。动能磁铁吸引掉落物，生灵磁铁吸引非玩家生物。潜行空手右键切换吸引与排斥。放入灵魂模具可筛选物种；潜行手持小麦右键切换成年/幼年筛选。'],
  osmotic_enchanter:['Place six pillars within four blocks horizontally, each 2–12 blocks of obsidian or arcane stone topped with Nitor. Insert an item with right-click. Sneak-right-click empty-handed cycles every compatible catalogue enchantment; sneak-click with a stick cycles its level. An enchanted book also selects an enchantment. Right-click with a charged Thaumaturge wand to pay Vis and begin a five-second enchantment. Empty-hand right-click retrieves the item. Removing pillars interrupts the paid operation. TT enchantments require their own research.','在水平4格内建造6根柱子，每根由2–12格黑曜石或奥术石组成，顶部放置炽焱。右键放入装备。潜行空手右键轮换兼容附魔，潜行手持木棍右键选择等级，也可用附魔书选择。手持充能法杖右键一次支付显示的Vis并开始5秒附魔。空手右键取回。期间拆柱会中断且不退还Vis。工匠附魔需完成对应研究。'],
  transvector_interface:['Sneak-right-click a target with the Transvector Binder, then right-click the interface with that binder. The target must be in this dimension and within 32 blocks. Item, fluid, energy and essentia automation attached to the interface reaches the target. Right-click the interface to open the target menu where one exists. Interfaces cannot link into other interfaces.','潜行手持向量绑定器右键目标，再手持绑定器右键向量接口。目标需在同一维度32格内。接在接口的物品、流体、能量与精华管线可访问目标。右键接口可打开目标原有界面。不可连接另一向量接口。'],
  transvector_dislocator:['Bind a target with the Transvector Binder. On a redstone rising edge, exchange entities between the target block and the facing destination. Sneak-right-click empty-handed to switch to exchanging blocks and their block-entity contents. Sneak-click with a stick cycles destination distance 1–16. Bedrock, portals and unloaded chunks are excluded.','用向量绑定器绑定目标。红石上升沿时交换目标格与朝向目标格的实体。潜行空手右键切换为交换方块及方块实体数据；潜行手持木棍右键循环设置朝向距离1–16格。不能移动基岩、传送门或未加载区块。'],
  remote_placer:['Right-click to insert a stack of blocks. A redstone rising edge uses it at the facing position. Sneak-click with a stick cycles distance 1–16. Empty-hand right-click retrieves the stack. Sneak-click with a block copies its appearance without changing machine behavior.','右键放入一组方块，在红石上升沿时向朝向位置使用。潜行手持木棍右键循环设置1–16格距离，空手右键取回。潜行手持方块右键复制其外观。'],
  golem_connector:['Bind a home position using the Transvector Binder. Right-click reports the health and position of your golems within 32 blocks. A redstone pulse sets those golems home to the bound position and directs them there. Modern Thaumaturge seals replace the old marker system.','使用向量绑定器绑定家园位置。右键报告32格内自己傀儡的生命与坐标。红石脉冲将这些傀儡的家园设为绑定位置并命令其前往。现代傀儡使用神秘学印记系统。'],
  animation_tablet:['Insert a tool or item by right-clicking. While redstone powered, the tablet uses it every second on the facing block. Sneak-right-click empty-handed switches to left-click, mining blocks or attacking mobs. Empty-hand right-click retrieves the held item. Actions run as an owner-identified fake player and consume tool durability and item counts.','右键放入工具或物品。红石供能时，每秒向朝向的一格使用一次。潜行空手右键切换左键模式，执行挖掘或攻击。空手右键取回物品。操作由带所有者身份的模拟玩家执行，消耗耐久及物品数量。'],
  aspect_analyzer:['Right-click with an item to report its live Thaumaturge aspect composition in chat. Empty-hand right-click reads the item inserted by automation.','手持物品右键，在聊天栏显示神秘学实时要素组成。空手右键读取自动化放入的样品。'],
  platform:['You can pass upward through this platform and stand on its top. Sneaking allows downward passage. Sneak-right-click with a block to copy that blocks appearance. The camouflaged utility block uses the same appearance controls.','可从下方穿过平台并站在顶部，潜行时可向下穿过。潜行手持方块右键可复制其外观。迷彩方块使用相同外观操作。'],
  mobilizer:['Bind the locomotive to a relay within 32 blocks using the binder. Place a block on top: every five seconds the locomotive carries it one horizontal block, including saved machine inventories. Redstone stops movement. An obstacle reverses the direction. Sneak-right-click empty-handed also reverses it. Place the locomotive while looking horizontally.','用绑定器将机车连接到32格内的中继。顶部可放一个方块，每5秒沿水平方向移动一格，同时携带机器的存档与库存。输入红石可停车，遇到阻挡自动反向，潜行空手右键也可反向。放置时请水平观察。'],
  summon_tablet:['Insert three mob aspects with right-click, or place them on nearby Thaumaturge pedestals within five blocks. Every 15 seconds an unpowered tablet consumes a matching trio and summons its mob. All three infused aspects instead remain bound to their first tablet and summon once per minute without consumption. Duplicate aspects require separate slots. Empty-hand right-click retrieves inserted aspects. See the individual mob-aspect entries for species and combinations.','右键依次放入3个生物要素，或放在5格内的神秘学注魔基座上。无红石时每15秒检查一次，消耗匹配的3个要素召唤生物。若3个均为注魔要素，则绑定第一次使用的石板，每分钟召唤一次且不消耗。重复要素需放在不同栏位。空手右键取回。各要素条目列有物种与组合。'],
  infused_crops:['Infuse wheat seeds with four matching primal crystals. Plant the seeds on farmland and harvest fully mature crops for seeds and infused grain. Grain is the crucible catalyst for its corresponding infused potion. Four cultivars exist: air, fire, earth and water.','将小麦种子与4个同种元素水晶注魔。种植在耕地上，成熟后收获种子及注魔谷物。谷物可作为坩埚催化剂制作对应注魔药水，共风、火、地、水四种。'],
};
const entries={repairer:['Thaumic Restorer','神秘修复台'],funnel:['Essentia Funnel','精华漏斗'],mob_magnet:['Magnetic Forces','磁力操控'],osmotic_enchanter:['Osmotic Enchanter','渗透附魔台'],transvector_interface:['Transvector Interface','向量接口'],transvector_dislocator:['Transvector Dislocator','向量移位器'],remote_placer:['Remote Placer','遥控放置器'],golem_connector:['Golem Connector','傀儡连接器'],animation_tablet:['Tool Dynamism','工具活化'],aspect_analyzer:['Aspect Analyzer','要素分析器'],platform:['Translucent Platforms','半透平台'],mobilizer:['Levitational Locomotive','浮空机车'],summon_tablet:['Summoning Tablet','灵魂召唤石板'],infused_crops:['Infused Crops','注魔作物']};
let row=0;
for(const [id,[en,zh]] of Object.entries(entries)){
  const recipes=id==='osmotic_enchanter'?['enchanter']:id==='mob_magnet'?['magnet','mob_magnet']:id==='transvector_interface'?['transvector_interface','connector']:id==='mobilizer'?['mobilizer','mobilizer_relay']:id==='platform'?['platform','camo']:id==='infused_crops'?['aer','ignis','terra','aqua'].map(a=>`infused_seeds_${a}`):[id];
  const parents=id==='transvector_dislocator'?['thaumictinkerer:transvector_interface']:id==='summon_tablet'?['thaumictinkerer:mob_magnet']:['thaumaturge:base_artifice'];
  write(`data/${ns}/thaumaturge/research_entry/${id}.json`,{category:`${ns}:tinkering`,name:`research.${ns}.${id}.title`,parents,column:-9+(row%4)*2,row:-6+Math.floor(row/4)*2,icons:[`${ns}:${recipes[0]}`],note_aspects:aspects({instrumentum:2,praecantatio:2}),complexity:2,stages:[{text:`research.${ns}.${id}.text`,recipes:recipes.map(r=>`${ns}:${r}`)}]});row++;
  lang(`research.${ns}.${id}.title`,en,zh);const text=[...instructions[id]];if(id!=='infused_crops'){text[0]+=' Sneak-click with a compass to cycle the six facing directions.';text[1]+='潜行手持指南针右键可循环设置六个朝向。';}lang(`research.${ns}.${id}.text`,...text);coverage[`research:${id}`]=recipes;
}
const fireRules={aer:['Wood becomes sand, leaves sandstone, ice glass, water cake and dirt sand.','原木变为沙子，树叶变砂岩，冰变玻璃，水变蛋糕，泥土变沙子。'],aqua:['Sand, soul sand and glowstone become ice; netherrack becomes snow and lava obsidian.','沙子、灵魂沙、萤石变为冰，下界岩变雪块，熔岩变黑曜石。'],terra:['Sand, stone, cobblestone, logs, leaves, planks and glass become dirt; gravel becomes clay and nether bricks wood.','沙子、石头、圆石、原木、树叶、木板与玻璃变为泥土，沙砾变黏土，下界砖变木材。'],ignis:['Dirt becomes netherrack; sand and gravel soul sand; clay glowstone; ores nether quartz ore; lava water; crops nether wart.','泥土变为下界岩，沙子和沙砾变灵魂沙，黏土变萤石，矿石变下界石英矿，熔岩变水，作物变下界疣。'],ordo:['Ores become the corresponding storage blocks, including deepslate ores and modern copper.','矿石变为对应储存块，包括深层矿石与现代铜矿。'],perditio:['Other elemental fires become ordinary fire.','将其他元素火焰转化为普通火焰。']};
for(const [aspect,[en,zh]]of Object.entries(elementNames)){
  const id=`fire_${aspect}`;write(`data/${ns}/thaumaturge/research_entry/${id}.json`,{category:`${ns}:tinkering`,name:`research.${ns}.${id}.title`,parents:[`${ns}:bright_nitor`],column:2+Object.keys(elementNames).indexOf(aspect)*2,row:-7,icons:[`${ns}:${id}`],note_aspects:aspects({ignis:2,[aspect]:2}),complexity:1,stages:[{text:`research.${ns}.${id}.text`,recipes:[`${ns}:${id}`]}]});
  lang(`research.${ns}.${id}.title`,`${en} Infused Fire`,`${zh}之焰`);lang(`research.${ns}.${id}.text`,`Place the fire to transmute adjacent blocks. It ages and spreads locally over transformable materials, so build a suitable containment. ${fireRules[aspect][0]}`,`放置后转化相邻方块，火焰会衰减并在可转化材料上局部传播，请合理隔离。${fireRules[aspect][1]}`);
}
const messages={
  bind_range:['Target must be in this dimension, within 32 blocks and different from this device.','目标需在同维度32格内且不能是设备本身。'],
  linked:['Linked to %s','已连接到 %s'],bound:['Target selected: %s','已选中目标：%s'],facing:['Facing: %s','朝向：%s'],status:['Facing %s, distance %s, linked target %s','朝向 %s，距离 %s，绑定目标 %s'],alternate:['Alternate mode enabled','已切换到反向/左键/方块模式'],normal:['Normal mode enabled','已切换到正向/右键/实体模式'],distance:['Facing distance: %s blocks','朝向距离：%s 格'],adults:['Filter: adults','筛选：成年'],children:['Filter: young animals','筛选：幼年'],camouflage:['Appearance copied: %s','已复制外观：%s'],enchanting:['Enchanting: %s ticks remaining','附魔中：剩余 %s 刻'],selected_enchant:['Selected: %s. Vis: %s','已选择：%s。Vis消耗：%s'],research:['Complete this enchantments research first.','请先完成该附魔的研究。'],pillars:['Six pillars are required: 2–12 obsidian or arcane-stone blocks topped with Nitor, within four blocks.','需要在4格内建造6根2–12格黑曜石或奥术石柱，顶部放置炽焱。'],vis:['The wand does not hold enough Vis.','法杖Vis不足。'],golem:['%s — health %s, position %s','%s — 生命 %s，坐标 %s'],
};for(const [id,values]of Object.entries(messages))lang(`message.${ns}.device.${id}`,...values);
fs.mkdirSync(path.join(ROOT,'catalog'),{recursive:true});fs.writeFileSync(path.join(ROOT,'catalog/devices.json'),JSON.stringify({languages,coverage,adaptations,implemented:Object.keys(machines).concat(['connector'],Object.keys(elementNames).map(a=>`fire_${a}`),['aer','ignis','terra','aqua'].flatMap(a=>[`infused_crop_${a}`,`infused_seeds_${a}`,`infused_grain_${a}`]))},null,2)+'\n');
console.log(`Generated device resources: ${Object.keys(machines).length} machines, 6 fires, 4 crops; language map and coverage in catalog/devices.json.`);
