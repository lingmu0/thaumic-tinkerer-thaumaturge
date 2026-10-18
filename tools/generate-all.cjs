const fs=require('node:fs'),path=require('node:path');
const {ROOT,write}=require('./resource-helpers.cjs');
for(const script of ['generate-resources.cjs','generate-enchantments.cjs','generate-devices.cjs','generate-focus.cjs','generate-kami.cjs']) {
    const file=path.join(__dirname,script);
    if(fs.existsSync(file)) require(file);
}
const catalogs=fs.readdirSync(path.join(ROOT,'catalog')).filter(file=>file.endsWith('.json')).map(file=>JSON.parse(fs.readFileSync(path.join(ROOT,'catalog',file),'utf8').replace(/^\uFEFF/,'')));
const baseAspects={};
for(const catalog of catalogs)Object.assign(baseAspects,catalog.baseAspects||{});
if(Object.keys(baseAspects).length)write('data/thaumaturge/data_maps/item/base_aspects.json',{values:baseAspects});
for(const language of ['en_us','zh_cn']) {
    const file=path.join(ROOT,`common/src/main/resources/assets/thaumictinkerer/lang/${language}.json`);
    const combined=JSON.parse(fs.readFileSync(file,'utf8'));
    for(const catalog of catalogs) Object.assign(combined,catalog.languages?.[language]||{});
    fs.writeFileSync(file,JSON.stringify(combined,null,2)+'\n');
}
function files(dir) {return fs.readdirSync(dir,{withFileTypes:true}).flatMap(entry=>entry.isDirectory()?files(path.join(dir,entry.name)):[path.join(dir,entry.name)]);}
const ownDir=path.join(ROOT,'common/src/main/resources/data/thaumictinkerer/thaumaturge/research_entry');
const own=new Map(files(ownDir).filter(file=>file.endsWith('.json')).map(file=>['thaumictinkerer:'+path.relative(ownDir,file).replaceAll('\\','/').replace(/\.json$/,''),{file,value:JSON.parse(fs.readFileSync(file,'utf8'))}]));
// KAMI originally requires completion of the base research tree. Exclude its descendants
// to avoid a circular dependency, and use research shared by both supported dependency builds.
const kami=new Set(['thaumictinkerer:ichor']);
let changed=true;
while(changed) {changed=false;for(const [id,{value}] of own) {
    const parents=(value.parents||[]).map(parent=>typeof parent==='string'?parent:parent.entry);
    if(!kami.has(id)&&parents.some(parent=>kami.has(parent))) {kami.add(id);changed=true;}
}}
const indexPaths=['1.21.1','26.1.2'].map(v=>path.join(ROOT,`dev-libs/thaumaturge-index-${v}.json`));
if(indexPaths.every(file=>fs.existsSync(file))) {
    const indexes=indexPaths.map(file=>JSON.parse(fs.readFileSync(file,'utf8').replace(/^\uFEFF/,'')));
    const requirements=[];
    for(const [file,value] of Object.entries(indexes[0])) {
        const match=file.match(/^data\/thaumaturge\/thaumaturge\/research_entry\/(.+)\.json$/);
        if(match&&!match[1].startsWith('scanned/')&&Array.isArray(value.parents)&&value.parents.length&&indexes[1][file]) requirements.push('thaumaturge:'+match[1]);
    }
    for(const [id] of own) if(!kami.has(id)) requirements.push(id);
    const start=own.get('thaumictinkerer:ichor');
    if(start) {
        start.value.stages[0].required_research=[...new Set(requirements)].sort();
        start.value.complexity=3;
        fs.writeFileSync(start.file,JSON.stringify(start.value,null,2)+'\n');
        console.log(`KAMI gate: ${start.value.stages[0].required_research.length} base research entries.`);
    }
}
const protectedItems=new Set(catalogs.flatMap(catalog=>catalog.cannotDisenchant||catalog.protectedItems||[]));
write('data/thaumictinkerer/tags/item/cannot_disenchant.json',{replace:false,values:[...protectedItems]});
for(const slot of ['necklace','belt','ring']) {
    const file=path.join(ROOT,`common/src/main/resources/data/curios/tags/item/${slot}.json`);
    const values=new Set(fs.existsSync(file)?JSON.parse(fs.readFileSync(file,'utf8')).values:[]);
    for(const catalog of catalogs)for(const item of catalog.curios?.[slot]||[])values.add(item);
    write(`data/curios/tags/item/${slot}.json`,{replace:false,values:[...values]});
}
const pickaxe='data/minecraft/tags/block/mineable/pickaxe.json';
const pickaxeTag=JSON.parse(fs.readFileSync(path.join(ROOT,'common/src/main/resources',pickaxe),'utf8'));
pickaxeTag.values=[...new Set([...pickaxeTag.values,'#thaumictinkerer:devices'])];
write(pickaxe,pickaxeTag);
const equipmentTags={
    head_armor:['ichor_helm','ichor_helm_gem','revealing_helm'],
    chest_armor:['ichor_chest','ichor_chest_gem'],
    leg_armor:['ichor_legs','ichor_legs_gem'],foot_armor:['ichor_boots','ichor_boots_gem'],
    pickaxes:['ichor_pick','ichor_pick_gem'],shovels:['ichor_shovel','ichor_shovel_gem'],
    axes:['ichor_axe','ichor_axe_gem'],swords:['ichor_sword','ichor_sword_gem','blood_sword']
};
for(const [tag,items] of Object.entries(equipmentTags))
    write(`data/minecraft/tags/item/${tag}.json`,{replace:false,values:items.map(id=>'thaumictinkerer:'+id)});
// Every item-model file must have a 26.1.2 item definition, including newly added modules.
const models=path.join(ROOT,'common/src/main/resources/assets/thaumictinkerer/models/item');
for(const file of files(models).filter(file=>file.endsWith('.json'))) {
    const name=path.relative(models,file).replaceAll('\\','/').replace(/\.json$/,'');
    const definition=`assets/thaumictinkerer/items/${name}.json`;
    if(!fs.existsSync(path.join(ROOT,'neoforge-26.1.2/src/main/resources',definition)))
        write(definition,{model:{type:'minecraft:model',model:`thaumictinkerer:item/${name}`}},'neoforge-26.1.2/src/main/resources');
}
console.log('Merged language catalogs and generated modern item definitions.');
