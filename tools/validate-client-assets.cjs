const fs=require('node:fs'),path=require('node:path');
const {ROOT}=require('./resource-helpers.cjs');
function files(directory){return fs.existsSync(directory)?fs.readdirSync(directory,{withFileTypes:true}).flatMap(e=>e.isDirectory()?files(path.join(directory,e.name)):[path.join(directory,e.name)]):[];}
function validateClientAssets(resources,jsons,version){
    const errors=[],ns='thaumictinkerer',base=`assets/${ns}/`;
    const fail=(file,message)=>errors.push(`${version} ${file}: ${message}`);
    const validId=(id,file)=>{
        if(typeof id!=='string'||!(/^[a-z0-9_.-]+:[a-z0-9/._-]+$/).test(id)){fail(file,`invalid resource identifier ${JSON.stringify(id)}`);return false;}
        return true;
    };
    for(const file of resources.keys())if(file.startsWith(base)&&!(/^[a-z0-9/._-]+$/).test(file))fail(file,'invalid resource filename (only lowercase paths are loadable)');
    function modelRef(id,file){
        if(!validId(id,file))return;
        if(id.startsWith(`${ns}:`)&&!jsons.has(`${base}models/${id.split(':')[1]}.json`))fail(file,`missing model ${id}`);
    }
    function textureRef(id,file){
        if(!validId(id,file))return;
        if(id.startsWith(`${ns}:`)&&!resources.has(`${base}textures/${id.split(':')[1]}.png`))fail(file,`missing texture ${id}`);
    }
    function inheritedTextures(file,trail=[]){
        if(trail.includes(file)){fail(file,'model parent cycle');return {};}
        const model=jsons.get(file)||{};
        const parent=model.parent?.startsWith(`${ns}:`)?`${base}models/${model.parent.split(':')[1]}.json`:null;
        return {...(parent?inheritedTextures(parent,[...trail,file]):{}),...model.textures};
    }
    function resolveTexture(reference,textures,file,trail=[]){
        if(!reference.startsWith('#'))return textureRef(reference,file);
        const key=reference.slice(1);
        if(trail.includes(key)){fail(file,`texture variable cycle ${key}`);return;}
        if(typeof textures[key]!=='string'){fail(file,`unresolved texture variable ${reference}`);return;}
        resolveTexture(textures[key],textures,file,[...trail,key]);
    }
    function itemDefinition(value,file){
        if(!value||typeof value!=='object')return;
        if(value.type==='minecraft:model')modelRef(value.model,file);
        for(const entry of Object.values(value))if(Array.isArray(entry))entry.forEach(v=>itemDefinition(v,file));else if(entry&&typeof entry==='object')itemDefinition(entry,file);
    }
    const blockIds=new Set(),models=new Set();
    for(const [file,value]of jsons){
        if(file.startsWith(`${base}models/`)){
            models.add(file);
            if(value.parent)modelRef(value.parent,file);
            const textures=inheritedTextures(file);
            for(const texture of Object.values(value.textures||{}))if(typeof texture==='string')resolveTexture(texture,textures,file);else fail(file,'texture reference must be a string');
            for(const element of value.elements||[]){
                for(const key of ['from','to'])if(!Array.isArray(element[key])||element[key].length!==3||element[key].some(x=>!Number.isFinite(x)||x < -16||x>32))fail(file,`invalid element ${key}`);
                if(element.rotation&&![-45,-22.5,0,22.5,45].includes(element.rotation.angle))fail(file,`unsupported element rotation ${element.rotation.angle}`);
                for(const face of Object.values(element.faces||{})){
                    if(typeof face.texture==='string')resolveTexture(face.texture,textures,file);else fail(file,'face has no texture');
                    if(face.uv&&(!Array.isArray(face.uv)||face.uv.length!==4||face.uv.some(x=>!Number.isFinite(x))))fail(file,'invalid face UV');
                    if(face.rotation!==undefined&&![0,90,180,270].includes(face.rotation))fail(file,`invalid face rotation ${face.rotation}`);
                }
            }
        }
        if(file.startsWith(`${base}blockstates/`)){
            blockIds.add(file.slice(`${base}blockstates/`.length,-5));
            function variants(value){
                if(!value||typeof value!=='object')return;
                if(typeof value.model==='string')modelRef(value.model,file);
                for(const entry of Object.values(value))if(entry&&typeof entry==='object')variants(entry);
            }
            variants(value);
        }
        if(file.startsWith(`${base}items/`))itemDefinition(value,file);
    }
    const ids=new Set();
    const baseSource=fs.readFileSync(path.join(ROOT,'common/src/main/java/net/lingmu/thaumictinkerer/TTContent.java'),'utf8');
    for(const match of baseSource.matchAll(/=\s*(?:item|block)\("([^"]+)"/g))ids.add(match[1]);
    for(const name of ['focus','kami'])for(const id of JSON.parse(fs.readFileSync(path.join(ROOT,`catalog/${name}.json`),'utf8')).items)ids.add(id);
    for(const id of JSON.parse(fs.readFileSync(path.join(ROOT,'catalog/devices.json'),'utf8')).implemented)if(!id.startsWith('infused_crop_'))ids.add(id);
    for(const language of ['en_us','zh_cn']){
        const file=`${base}lang/${language}.json`,lang=jsons.get(file)||{};
        for(const id of blockIds)if(!lang[`block.${ns}.${id}`])fail(file,`missing block translation ${id}`);
        for(const id of ids){
            const prefix=blockIds.has(id)?'block':'item';
            if(!lang[`${prefix}.${ns}.${id}`])fail(file,`missing ${prefix} translation ${id}`);
            if(blockIds.has(id)&&!lang[`item.${ns}.${id}`])fail(file,`missing saved-item name alias ${id}`);
        }
    }
    for(const id of ids){
        const file=`${base}models/item/${id}.json`;
        if(!models.has(file))fail(file,'registered item has no model');
        if(version==='26.1.2'&&!jsons.has(`${base}items/${id}.json`))fail(file,'registered item has no modern item definition');
    }
    return {errors:[...new Set(errors)],registeredItems:ids.size,blocks:blockIds.size,models:models.size};
}
if(require.main===module){
    const version=process.argv[2]||'26.1.2',resources=new Map();
    const roots=process.argv[3]?[path.resolve(process.argv[3])]:[path.join(ROOT,'common/src/main/resources'),path.join(ROOT,`neoforge-${version}/src/main/resources`)];
    for(const root of roots)for(const file of files(root))resources.set(path.relative(root,file).replaceAll('\\','/'),file);
    const jsons=new Map([...resources].filter(([file])=>file.endsWith('.json')).map(([relative,file])=>[relative,JSON.parse(fs.readFileSync(file,'utf8'))]));
    const report=validateClientAssets(resources,jsons,version);
    fs.writeFileSync(path.join(ROOT,`validation/client-assets-${version}.json`),JSON.stringify({version,...report},null,2)+'\n');
    console.log(`${version}: ${report.registeredItems} registered items, ${report.blocks} blockstates, ${report.models} models; ${report.errors.length} client asset errors.`);
    if(report.errors.length){console.error(report.errors.join('\n'));process.exitCode=1;}
}
module.exports={validateClientAssets};
