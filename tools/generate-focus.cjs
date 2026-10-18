/* Original TT textures/recipe values: Vazkii and contributors, CC BY-NC-SA 3.0. */
const fs=require('node:fs'),path=require('node:path');
const {ROOT,aspects,ingredient,write}=require('./resource-helpers.cjs');
const source=path.resolve(ROOT,'../.thaumic-tinkerer-1.7.10-upstream/src/main/resources/assets/ttinkerer');
const ns='thaumictinkerer',base=`assets/${ns}`;
const index=JSON.parse(fs.readFileSync(path.join(ROOT,'dev-libs/thaumaturge-index-26.1.2.json'),'utf8'));
const languages={en_us:{},zh_cn:{}};
const names={
 focus_smelt:["Wand Focus: Efreet's Flame",'法杖核心：伊夫利特之辉','focusSmelt'],
 focus_flight:['Wand Focus: Uprising','法杖核心：腾飞','focusFlight'],
 focus_deflect:['Wand Focus: Distortion','法杖核心：偏转','focusDeflect'],
 focus_heal:['Wand Focus: Mending','法杖核心：治愈','focusHeal'],
 focus_ender_chest:['Wand Focus: Ender Rift','法杖核心：末影裂隙','focusEnderChest'],
 focus_telekinesis:['Wand Focus: Telekinesis','法杖核心：念力','focusTelekinesis'],
 focus_dislocation:['Wand Focus: Dislocation','法杖核心：移位','focusDislocation'],
 focus_shadowbeam:['Wand Focus: Shadowbeam','法杖核心：暗影光束','focusShadowbeam'],
 focus_xp_drain:['Wand Focus: Experience Drain','法杖核心：经验汲取','focusXPDrain'],
 focus_recall:['Wand Focus: Celestial Recall','法杖核心：仙域召唤','focusRecall'],
 bright_nitor:['Hyperenergetic Nitor','超能闪耀之光','brightNitor'],
 gaseous_light:['Gaseous Illuminae','光明气体','gaseousLightItem'],
 gaseous_tenebrae:['Gaseous Tenebrae','黑暗气体','gaseousShadowItem'],
 gas_remover:['Gas Remover','气体清除器','gasRemover'],
 blood_sword:['Blood Sword','血腥之刃','bloodSword0'],
 revealing_helm:['Revealing Helmet','揭示头盔','revealingHelm'],
 infused_inkwell:['Infused Inkwell','注魔墨水瓶','infusedInkwell'],
 share_book:['Tome of Knowledge Sharing','知识分享之书','shareBook'],
 soul_mould:['Soul Mould','灵魂模具','soulMould'],
 infused_potion_aer:['Air-Imbued Potion','风之注魔药水','potion_aer'],
 infused_potion_ignis:['Fire-Imbued Potion','火之注魔药水','potion_ignis'],
 infused_potion_terra:['Earth-Imbued Potion','地之注魔药水','potion_terra'],
 infused_potion_aqua:['Water-Imbued Potion','水之注魔药水','potion_aqua'],
};
function copyTexture(name,texture){
 const input=path.join(source,`textures/items/${texture}.png`),output=path.join(ROOT,'common/src/main/resources',base,`textures/item/${name}.png`);
 fs.mkdirSync(path.dirname(output),{recursive:true});fs.copyFileSync(input,output);
 if(fs.existsSync(input+'.mcmeta'))fs.copyFileSync(input+'.mcmeta',output+'.mcmeta');
}
function model(name,texture,handheld=false){
 copyTexture(name,texture);
 const textures={layer0:`${ns}:item/${name}`};
 for(const suffix of ['Orn','Depth'])if(fs.existsSync(path.join(source,`textures/items/${texture}${suffix}.png`))){
  const layer=Object.keys(textures).length;copyTexture(`${name}_layer${layer}`,texture+suffix);textures[`layer${layer}`]=`${ns}:item/${name}_layer${layer}`;
 }
 write(`${base}/models/item/${name}.json`,{parent:handheld?'minecraft:item/handheld':'minecraft:item/generated',textures});
}
for(const [id,[en,zh,texture]]of Object.entries(names)){
 model(id,texture,id==='blood_sword');languages.en_us[`item.${ns}.${id}`]=en;languages.zh_cn[`item.${ns}.${id}`]=zh;
}
for(const id of ['light_gas','shadow_gas','nitor_gas']){
 write(`${base}/models/block/${id}.json`,{textures:{particle:`${ns}:item/gaseous_light`},elements:[]});
 write(`${base}/blockstates/${id}.json`,{variants:{'':{model:`${ns}:block/${id}`}}});
}
for(const [id,en,zh]of [
 ['light_gas','Illuminae Gas','光明气体'],
 ['shadow_gas','Tenebrae Gas','暗影气体'],
 ['nitor_gas','Temporary Nitor Light','临时闪耀光源'],
]){
 languages.en_us[`block.${ns}.${id}`]=en;
 languages.zh_cn[`block.${ns}.${id}`]=zh;
}
function crucible(id,catalyst,cost){write(`data/${ns}/recipe/${id}.json`,{type:'thaumaturge:crucible',catalyst:ingredient(catalyst),aspects:aspects(cost),result:{id:`${ns}:${id}`},research:{entry:`${ns}:${id}`}});}
function infusion(id,catalyst,components,cost,instability,gate=id){write(`data/${ns}/recipe/${id}.json`,{type:'thaumaturge:infusion',catalyst:ingredient(catalyst),components:components.map(ingredient),aspects:aspects(cost),instability,result:{id:`${ns}:${id}`},research:{entry:`${ns}:${gate}`}});}
function arcane(id,pattern,key,cost){
 const primal=aspects(cost),vis=primal.reduce((s,a)=>s+a.amount,0);
 write(`data/${ns}/recipe/${id}.json`,{type:'thaumaturge:arcane_workbench_shaped',pattern,key:Object.fromEntries(Object.entries(key).map(([k,v])=>[k,ingredient(v)])),crystals:primal.map(a=>({...a,amount:1})),vis,result:{id:`${ns}:${id}`},research:{entry:`${ns}:${id}`}});
}
function shaped(id,pattern,key,output=id){write(`data/${ns}/recipe/${id}.json`,{type:'minecraft:crafting_shaped',category:'misc',pattern,key:Object.fromEntries(Object.entries(key).map(([k,v])=>[k,ingredient(v)])),result:{id:`${ns}:${output}`}});}
const T=id=>`thaumaturge:${id}`,N=id=>`${ns}:${id}`,M=id=>`minecraft:${id}`;
const q=M('quartz'),pearl=M('ender_pearl'),diamond=M('diamond'),blank=T('focus_1'),advanced=T('focus_2');
crucible('gaseous_light',T('phial'),{LIGHT:16,AIR:10,MOTION:8});
crucible('gaseous_tenebrae',T('phial'),{DARKNESS:16,AIR:10,MOTION:8});
crucible('bright_nitor',T('nitor_yellow'),{ENERGY:25,LIGHT:25,AIR:10,FIRE:10});
crucible('soul_mould',pearl,{BEAST:4,MIND:8,SENSES:8});
for(const aspect of ['aer','ignis','terra','aqua']){
 crucible('infused_potion_'+aspect,N('infused_grain_'+aspect),{auram:5,[aspect]:5});
 const output=path.join(ROOT,'common/src/main/resources',base,`textures/mob_effect/imbued_${aspect}.png`);
 fs.mkdirSync(path.dirname(output),{recursive:true});fs.copyFileSync(path.join(source,`textures/items/potion_${aspect}.png`),output);
 languages.en_us[`effect.${ns}.imbued_${aspect}`]=names['infused_potion_'+aspect][0].replace(' Potion','');
 languages.zh_cn[`effect.${ns}.imbued_${aspect}`]=names['infused_potion_'+aspect][1].replace('药水','');
}
arcane('gas_remover',['DDD','T G','QQQ'],{D:N('dark_quartz'),T:N('gaseous_tenebrae'),G:N('gaseous_light'),Q:q},{AIR:2,ORDER:2});
arcane('revealing_helm',['GH'],{G:T('goggles_revealing'),H:T('thaumium_helm')},{EARTH:5,FIRE:5,WATER:5,AIR:5,ORDER:5,ENTROPY:5});
arcane('focus_smelt',['FNE'],{F:blank,N:T('nitor_yellow'),E:blank},{FIRE:10,ORDER:5,ENTROPY:6});
arcane('focus_ender_chest',['M','E','P'],{M:T('mirror'),E:M('ender_eye'),P:advanced},{ORDER:10,ENTROPY:10});
infusion('focus_flight',pearl,[q,q,q,q,M('feather'),M('feather'),T('crystal_aer')],{AIR:15,MOTION:20,TRAVEL:10},3);
infusion('focus_deflect',N('focus_flight'),[T('mirrored_glass'),T('mirrored_glass'),T('paving_stone_barrier'),T('crystal_ordo')],{AIR:15,ARMOR:5,ORDER:20},5);
infusion('focus_heal',blank,[M('golden_carrot'),M('gold_nugget'),M('gold_nugget'),M('gold_nugget')],{HEAL:10,SOUL:10,LIFE:15},4);
infusion('focus_telekinesis',pearl,[q,q,q,q,M('iron_ingot'),M('gold_ingot'),T('crystal_aer')],{MOTION:10,AIR:20,ENTROPY:20,MIND:10},5);
infusion('focus_dislocation',pearl,[q,q,q,q,T('amber'),T('amber'),T('amber'),diamond],{ELDRITCH:20,DARKNESS:10,VOID:25,MAGIC:20,TAINT:5},8);
infusion('focus_shadowbeam',T('focus_3'),[N('ichor'),M('arrow'),diamond,advanced,N('focus_deflect'),N('ichor')],{DARKNESS:65,ELDRITCH:32,MAGIC:50,WEAPON:32},12);
infusion('focus_xp_drain',pearl,[N('ichor'),M('experience_bottle'),diamond,N('xp_talisman'),M('enchanting_table'),N('ichor')],{MIND:65,TAINT:16,MAGIC:50,AURA:32},12);
infusion('focus_recall',N('sky_pearl'),[N('ichor'),N('ichor'),pearl,diamond,T('mirror'),advanced],{TRAVEL:100,ELDRITCH:64,MAGIC:50},10);
infusion('blood_sword',T('thaumium_sword'),[M('rotten_flesh'),M('porkchop'),M('beef'),M('bone'),diamond,M('ghast_tear')],{HUNGER:20,DARKNESS:5,SOUL:10,MAN:6},6);
infusion('infused_inkwell',T('scribing_tools'),[T('crystal_aer'),T('jar_normal'),T('quicksilver')],{VOID:8,DARKNESS:8},2);
shaped('infused_inkwell_refill',['QQQ','QCQ','QQQ'],{Q:M('ink_sac'),C:N('infused_inkwell')},'infused_inkwell');
shaped('share_book',[' S ','PTP',' P '],{S:T('scribing_tools'),T:T('thaumonomicon'),P:M('paper')});
write('data/thaumaturge/tags/item/scribing_tools.json',{replace:false,values:[N('infused_inkwell')]});
const aspectNames={aqua:['Water','水'],humanus:['Man','人'],aer:['Air','风'],volatus:['Flight','飞行'],ignis:['Fire','火'],praecantatio:['Magic','魔法'],exanimis:['Undead','亡灵'],corpus:['Flesh','血肉'],bestia:['Beast','野兽'],venenum:['Poison','毒'],terra:['Earth','地'],alienis:['Eldritch','异域'],iter:['Travel','旅行'],metallum:['Metal','金属'],limus:['Slime','粘液'],messis:['Crop','作物']};
const baseAspects={};
for(const [aspect,[en,zh]]of Object.entries(aspectNames))for(let tier=0;tier<3;tier++){
 const id=`${tier===2?'infused_':tier===1?'condensed_':''}mob_aspect_${aspect}`;
 let texture=aspect==='messis'?'fruit_terra':aspect+(tier===1?'_condensed':'');model(id,texture);
 languages.en_us[`item.${ns}.${id}`]=`${tier===2?'Infused ':tier===1?'Condensed ':''}${en} Soul Aspect`;
 languages.zh_cn[`item.${ns}.${id}`]=`${tier===2?'注魔':tier===1?'浓缩':''}${zh}之生物要素`;
 if(tier===0)baseAspects[N(id)]=aspects({[aspect]:8});
 if(tier===1)shaped(id,['XXX','XXX','XXX'],{X:N(`mob_aspect_${aspect}`)});
 if(tier===2)infusion(id,N(`condensed_mob_aspect_${aspect}`),Array(8).fill(N(`condensed_mob_aspect_${aspect}`)),{[aspect]:10},4,'blood_sword');
}
const spellValues={smelt:[0.57,0xff0000,'ignis'],flight:[0.15,0x9ef2ff,'aer'],deflect:[2.4,0xffffff,'ordo'],heal:[0.6,0xfd00d6,'victus'],ender_chest:[2,0x132223,'alienis'],telekinesis:[2,0x9c00be,'motus'],dislocation:[0,0xffb200,'permutatio'],shadowbeam:[13,0x4b0053,'tenebrae'],xp_drain:[0,0xffffff,'cognitio'],recall:[100,0x9cf8ff,'motus']};
const focusMap={};
for(const [action,[vis,color,aspect]]of Object.entries(spellValues)){
 const id=`focus_${action}`;
 // The recall part's 100 complexity yields Thaumaturge's 60-tick charge time; the event also enforces 60 ticks.
 const complexity=action==='recall'?100:0;
 write(`data/${ns}/thaumaturge/spell_part/${action}.json`,{behavior:{type:N('tinkering'),action},icon:`${ns}:textures/item/${id}.png`,color,complexity,vis,aspect:T(aspect),research:{entry:N(id)},max_per_spell:1});
 focusMap[N(id)]={complexity:action==='recall'?160:75,depth:14,branches:4,repeats:4};
 for(const locale of ['en_us','zh_cn']){
  languages[locale][`spell.${ns}.${action}.name`]=names[id][locale==='zh_cn'?1:0];
 }
}
write('data/thaumaturge/data_maps/item/spell_focus.json',{values:focusMap});
const descriptions={
 focus_smelt:['Socket this focus in a Thaumaturge wand. Hold use at a block for one second to smelt it in place when its furnace result is another block. Payment and pulses follow Thaumaturge’s channel system.','将核心装入 Thaumaturge 法杖，持续对准方块约一秒，把烧炼产物仍为方块的材料原地熔炼。施法消耗和脉冲遵循 Thaumaturge 的引导系统。'],
 focus_flight:['Socket in a Thaumaturge wand. Cast to surge in the direction you look; each cast resets fall distance. This is a burst of motion, so plan a safe landing.','装入 Thaumaturge 法杖，施法会沿视线方向腾飞，并重置下落距离。腾飞提供一次推力，请留意落点。'],
 focus_deflect:['Hold the wand to repel ordinary hostile projectiles within four blocks. Reflected projectiles become yours; your own projectiles, experience bottles, fireballs and wither skulls are ignored.','持续使用法杖，偏转四格内的普通敌方弹射物。偏转后归你所有；自己的弹射物、经验瓶、火球和凋灵之首不受影响。'],
 focus_heal:['Hold use on a Thaumaturge wand to restore your health gradually, matching the original unmodified rate of one health point per thirty ticks.','将核心装入 Thaumaturge 法杖并持续使用，以原版未升级时每三十刻恢复一点生命的速率逐渐治疗自己。'],
 focus_ender_chest:['Use the socketed wand to access your own Ender Chest from anywhere. The inventory is the same one used by vanilla Ender Chests.','使用装有此核心的法杖，随时打开自己与原版末影箱共用的库存。'],
 focus_telekinesis:['Hold use to gather dropped items around a point five blocks ahead. Sneak while channeling to draw the items to yourself.','持续使用，使前方五格处周围六格内的掉落物聚集；引导时潜行，可把物品吸到自己身边。'],
 focus_dislocation:['Use the wand on a block to carry it in the socketed focus, preserving its block state and block entity data. Use a block face to put it back. Ordinary blocks cost 5 Perditio, 5 Ordo and 1 Terra vis; block entities cost 25/25/5, and spawners cost 100/100/50. Placement costs no further vis. Unbreakable and fluid blocks are excluded, and break/place events are respected.','对方块施法，将其保存在核心中，保留状态和方块实体数据；再对方块表面施法放回。普通方块消耗 5 混沌、5 秩序、1 地之魔力；方块实体为 25/25/5，刷怪笼为 100/100/50。放回不再消耗魔力。无法破坏的方块和流体不可搬运，并遵守破坏/放置事件。'],
 focus_shadowbeam:['Channel a shadow beam that bounces off solid surfaces, up to eight reflections and forty-five blocks of total travel. It damages hostile living entities along its path and respects allied targets.','引导暗影光束，在实体表面反射，最多八次，总路程四十五格。伤害光路上的敌对活物，并避开友方。'],
 focus_xp_drain:['Channel through a Thaumaturge wand to exchange thirty experience points for up to five vis of the first primal aspect that has room. Each successful pulse pays XP; no vis is consumed.','通过 Thaumaturge 法杖引导，每次脉冲消耗三十点经验，为首个未满的基础要素补充最多五点魔力，不消耗魔力。'],
 focus_recall:['Charge a Thaumaturge wand for at least three seconds, then release to return to the first attuned Celestial Pearl in your hotbar. The destination must be an unlocked warp gate in the same dimension. Costs 120 vis before wand discounts.','使用 Thaumaturge 法杖蓄力至少三秒，松开后回到快捷栏中首颗已绑定天穹珍珠的目标。目标必须是同维度内未锁定的跃迁之门。折扣前消耗一百二十点魔力。'],
 bright_nitor:['Carry this item anywhere in your inventory to create temporary light at your head. The light blocks expire shortly after you move away.','携带在背包任意位置，即可在头部周围产生照明。离开后光源方块会很快消失。'],
 gaseous_light:['Use to release an invisible, persistent illuminating gas. It spreads up to four orthogonal blocks through open air and emits full light. One vial is consumed.','使用释放无形且持久的发光气体，通过空气沿六个方向扩散最多四格，发出最强光照。每次消耗一瓶。'],
 gaseous_tenebrae:['Use to release an invisible, persistent gas that blocks light. It spreads up to four orthogonal blocks through open air. One vial is consumed.','使用释放无形且持久的遮光气体，通过空气沿六个方向扩散最多四格。每次消耗一瓶。'],
 gas_remover:['Sneak-use to clear illuminating, shadow and temporary nitor gas from the original six-block cube around you.','潜行使用，清除周围原版六格立方区域中的发光、黑暗和临时炽焱气体。'],
 blood_sword:['This 950-durability sword grants ten attack damage and 25% movement speed while held. Attacking costs two health; guarding replaces incoming nonmagical damage with three magic damage. Sneak-use toggles mob-aspect harvesting: supported mobs drop their three original aspects instead of ordinary loot. Nine aspects condense into one; eight condensed aspects can be infused into a ninth to make an infused aspect.','这柄耐久九百五十的剑提供十点攻击伤害和百分之二十五移速。攻击消耗两点生命；格挡将传入的非魔法伤害替换为三点魔法伤害。潜行使用切换生物要素采集，使支持的生物以三个原版要素替代普通战利品。九个要素可浓缩；把八个浓缩要素注入第九个，可制成注魔要素。'],
 revealing_helm:['A thaumium helmet with the revealing component and five percent vis discount from Thaumaturge. It has 500 durability and repairs with thaumium. Its armor rendering uses Thaumaturge’s modern thaumium model.','具备 Thaumaturge 揭示功能和百分之五魔力减免的神秘金属头盔。耐久五百，可用神秘锭修复。盔甲渲染采用 Thaumaturge 的现代神秘金属模型。'],
 infused_inkwell:['An 800-use set of scribing tools recognized by Thaumaturge research tables through its scribing_tools item tag. Surround it with eight ink sacs to refill it.','可使用八百次的书写工具，通过 Thaumaturge 的 scribing_tools 标签供研究台使用。用八个墨囊围住即可补满。'],
 share_book:['Use a blank tome to record all your completed Thaumaturge research. Give it to another player to teach the recorded entries. The author can sneak-use it to refresh the snapshot. This intentionally enables the original survival sharing recipe.','使用空白书记录自己已完成的全部 Thaumaturge 研究，再交给其他玩家使用即可传授记录的条目。作者可潜行使用更新记录。本移植启用原版可选的生存分享书配方。'],
 soul_mould:['Use on a living nonplayer entity to record its registry type for mob magnets. Sneak-use clears the pattern.','对玩家以外的活物使用，记录其注册类型，供生物磁铁过滤使用。潜行使用清除记录。'],
 infused_potion_aer:['Drink for three minutes of air imbuement. Your attacks cause the target to tumble in random directions every five ticks for one second. Brew the matching infused grain with five Auram and five Aer.','饮用后获得三分钟风之注魔。你的攻击使目标在一秒内每五刻受到随机方向的推力。用对应注魔谷物、五点灵气和五点风要素炼制。'],
 infused_potion_ignis:['Drink for three minutes of fire imbuement. Your attacks mark the victim with the original lingering flame, refreshing six seconds of fire every five ticks for five minutes. Brew with infused fire grain, five Auram and five Ignis.','饮用后获得三分钟火之注魔。你的攻击给目标留下原版持续火焰印记，之后五分钟内每五刻刷新六秒着火时间。以火之注魔谷物、五点灵气和五点火要素炼制。'],
 infused_potion_terra:['Drink for three minutes of earth imbuement. Your attacks raise a five-by-five wall between you and the target; the forcefield dissolves after sixty ticks. The wall only fills air and respects placement events.','饮用后获得三分钟地之注魔。你的攻击会在你与目标之间形成五乘五的屏障墙，六十刻后消散。仅填充空气，并遵守放置事件。'],
 infused_potion_aqua:['Drink for three minutes of water imbuement. Lava in the four-block cube centered around you turns to obsidian, matching the original effect. Brew with infused water grain, five Auram and five Aqua.','饮用后获得三分钟水之注魔。周围四格立方区域内的熔岩凝固为黑曜石，与原版效果一致。以水之注魔谷物、五点灵气和五点水要素炼制。'],
};
const research={
 gaseous_light:[0,-3,['thaumaturge:base_alchemy'],{LIGHT:2,AIR:1}],gaseous_tenebrae:[-1,-5,[N('gaseous_light')],{DARKNESS:2,AIR:1,MOTION:4}],gas_remover:[-2,-7,[N('gaseous_tenebrae')],{DARKNESS:2,LIGHT:2}],bright_nitor:[1,-5,[N('gaseous_light')],{LIGHT:2,FIRE:1,ENERGY:1,AIR:1}],
 focus_smelt:[-2,-2,['thaumaturge:focus_break'],{FIRE:2,ENERGY:1,MAGIC:1}],focus_flight:[-3,-4,[N('focus_smelt')],{MOTION:1,MAGIC:1,AIR:2}],focus_deflect:[-4,-3,[N('focus_smelt')],{MOTION:2,AIR:1,ORDER:1,DEATH:1}],focus_heal:[-6,-4,[N('focus_deflect')],{HEAL:2,SOUL:1,MAGIC:1}],focus_ender_chest:[-6,-2,[N('focus_deflect')],{ELDRITCH:2,VOID:1,MAGIC:1}],focus_telekinesis:[-4,-6,[N('focus_flight')],{ELDRITCH:2,MAGIC:1,MOTION:1}],focus_dislocation:[-5,-5,[N('focus_flight')],{ELDRITCH:2,MAGIC:1,EXCHANGE:1}],
 focus_shadowbeam:[14,4,[N('ichorcloth_rod')],{DARKNESS:2,MAGIC:1,ELDRITCH:1,TAINT:1}],focus_xp_drain:[12,3,[N('ichorcloth_rod')],{MIND:2,MAGIC:1,AURA:1,MAN:1}],focus_recall:[20,8,[N('warp_gate'),N('ichorcloth_rod')],{TRAVEL:2,ELDRITCH:1,FLIGHT:1,MAGIC:1}],
 blood_sword:[-4,6,[N('cleansing_talisman')],{HUNGER:2,WEAPON:1,FLESH:1,SOUL:1}],revealing_helm:[0,0,['thaumaturge:unlock_artifice'],{AURA:2,ARMOR:1}],infused_inkwell:[1,-1,['thaumaturge:base_infusion'],{VOID:1,DARKNESS:1}],share_book:[0,-1,[],{}],soul_mould:[-2,0,[N('mob_magnet')],{BEAST:1,MIND:1}],
};
for(const [offset,aspect]of ['aer','ignis','terra','aqua'].entries())research['infused_potion_'+aspect]=[7+offset,-5,['aer','ignis','terra','aqua','ordo','perditio'].map(a=>N('fire_'+a)),{WATER:5,ENTROPY:5}];
for(const [id,[x,y,parents,cost]]of Object.entries(research)){
 for(const parent of parents)if(parent.startsWith('thaumaturge:')&&!index[`data/thaumaturge/thaumaturge/research_entry/${parent.split(':')[1]}.json`])throw Error(`Missing research ${parent}`);
 write(`data/${ns}/thaumaturge/research_entry/${id}.json`,{category:N('tinkering'),name:`research.${ns}.${id}.title`,parents,column:x,row:y,icons:[N(id)],note_aspects:aspects(cost),complexity:id.startsWith('focus_')?2:1,...(id==='share_book'?{meta:['autounlock','round']}:{}),stages:[{text:`research.${ns}.${id}.text`,recipes:[N(id),...(id==='infused_inkwell'?[N('infused_inkwell_refill')]:[])]}]});
 for(const locale of ['en_us','zh_cn']){
  languages[locale][`research.${ns}.${id}.title`]=names[id][locale==='zh_cn'?1:0];
  languages[locale][`research.${ns}.${id}.text`]=descriptions[id][locale==='zh_cn'?1:0];
  if(id.startsWith('focus_'))languages[locale][`spell.${ns}.${id.slice(6)}.desc`]=descriptions[id][locale==='zh_cn'?1:0];
 }
}
for(const [key,en,zh]of [
 ['share_written','Your completed research has been recorded.','已记录你完成的研究。'],['share_read','The tome has shared its recorded research.','已学习书中记录的研究。'],['blood_active','Mob-aspect harvesting enabled','生物要素采集已启用'],['blood_inactive','Mob-aspect harvesting disabled','生物要素采集已停用'],['recall_failed','No valid attuned pearl and unlocked gate were found in this dimension.','当前维度没有有效的已绑定珍珠与未锁定跃迁之门。'],
]){languages.en_us[`message.${ns}.${key}`]=en;languages.zh_cn[`message.${ns}.${key}`]=zh;}
const coverage={ItemFocusSmelt:'focus_smelt',ItemFocusFlight:'focus_flight',ItemFocusDeflect:'focus_deflect',ItemFocusHeal:'focus_heal',ItemFocusEnderChest:'focus_ender_chest',ItemFocusTelekinesis:'focus_telekinesis',ItemFocusDislocation:'focus_dislocation',ItemFocusShadowbeam:'focus_shadowbeam',ItemFocusXPDrain:'focus_xp_drain',ItemFocusRecall:'focus_recall',ItemBrightNitor:'bright_nitor',ItemGas:['gaseous_light','gaseous_tenebrae'],ItemGasRemover:'gas_remover',BlockGaseousLight:'light_gas',BlockGaseousShadow:'shadow_gas',BlockNitorGas:'nitor_gas',ItemBloodSword:'blood_sword',ItemRevealingHelm:'revealing_helm',ItemInfusedInkwell:'infused_inkwell',ItemShareBook:'share_book',ItemSoulMould:'soul_mould',ItemMobAspect:'MobAspects (16 × 3 independent registry items)',ItemMobDisplay:'Original shouldRegister=false; its summon-guide role is handled by summon tablet research, with no unused inventory item'};
coverage.ItemInfusedPotion=['infused_potion_aer','infused_potion_ignis','infused_potion_terra','infused_potion_aqua'];
coverage.PotionEffectHandler='TinkerPotions: original air/fire hit timers, temporary earth wall and water lava solidification';
const adaptations=[
 'All ten foci carry real Thaumaturge SPELL components, participate in the official spell_focus data map, and cast registered SpellBehavior nodes through ordinary Thaumaturge wands. Nodes are research gated and also available in the focal manipulator.',
 'Old specialized Thaumcraft focus recipe inputs become Thaumaturge blank foci of suitable tier; old primal shards become primal crystals. Original recipe shape, material intent, infusion instability and essentia quantities are retained.',
 'Channel effects follow the modern spell engine pulse scheduling and vis discounts rather than old per-tick callbacks; self healing retains the original one health per thirty ticks. XP conversion is thirty points for up to five primal vis per pulse, independent of vis spending.',
 'Recall uses a charged spell with a mandatory sixty-tick minimum; same-dimension valid gate and pearl logic is provided by KamiPearl. Dislocation preserves full block entity NBT and observes NeoForge break/place protection events.',
 'Shadowbeam uses bounded server ray casts (forty-five blocks, eight bounces) and visible vanilla witch particles in place of the obsolete immediately-ticked throwable entity and third-party Thaumcraft particles.',
 'Mob aspects use sixteen named species and three tiers, retaining flesh/slime/crop distinctions even where the modern essentia system merges their costs. The missing original crop aspect index is repaired so chicken harvesting works.',
 'The infused inkwell is recognized through the official scribing_tools tag. Revealing helmet uses the official goggles component, thaumium armor and five percent discount, rendering as modern thaumium armor.',
 'The original optional survival knowledge-sharing recipe is enabled. The tome stores completed research as a portable snapshot; the author may refresh it by sneak use.',
 'Four three-minute elemental potions retain the original combat/world effects, including the original five-minute lingering-fire mark, five-by-five temporary forcefield wall, one-second air disruption and nearby lava solidification. Effects tick only on the server and honor placement/break protection events.',
];
const catalog={languages,coverage,adaptations,baseAspects,items:[...Object.keys(names),...Object.keys(aspectNames).flatMap(a=>[`mob_aspect_${a}`,`condensed_mob_aspect_${a}`,`infused_mob_aspect_${a}`])],blocks:['light_gas','shadow_gas','nitor_gas'],research:Object.keys(research),spellParts:Object.keys(spellValues),unimplemented:[]};
fs.mkdirSync(path.join(ROOT,'catalog'),{recursive:true});fs.writeFileSync(path.join(ROOT,'catalog/focus.json'),JSON.stringify(catalog,null,2)+'\n');
console.log(`Generated ${catalog.items.length} focus/utility items, three gas blocks, ten spell parts and ${catalog.research.length} research entries.`);
