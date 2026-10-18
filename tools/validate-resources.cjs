const fs=require('node:fs'),path=require('node:path');
const {ROOT}=require('./resource-helpers.cjs');
function files(dir) {if(!fs.existsSync(dir))return[];return fs.readdirSync(dir,{withFileTypes:true}).flatMap(entry=>entry.isDirectory()?files(path.join(dir,entry.name)):[path.join(dir,entry.name)]);}
let errors=[];
const versions=process.argv[2]?[process.argv[2]]:['1.21.1','26.1.2'];
for(const version of versions) {
    const resources=new Map();
    for(const directory of ['common/src/main/resources',`neoforge-${version}/src/main/resources`]) {
        const root=path.join(ROOT,directory);
        for(const file of files(root))resources.set(path.relative(root,file).replaceAll('\\','/'),file);
    }
    const index=JSON.parse(fs.readFileSync(path.join(ROOT,`dev-libs/thaumaturge-index-${version}.json`),'utf8').replace(/^\uFEFF/,''));
    const jsons=new Map();
    for(const [relative,file] of resources) {
        if(relative.endsWith('.json'))try{jsons.set(relative,JSON.parse(fs.readFileSync(file,'utf8')));}catch(error){errors.push(`${version} ${relative}: invalid JSON: ${error.message}`);}
        if(relative.endsWith('.png')) {
            const bytes=fs.readFileSync(file);
            if(bytes.length<24||bytes.subarray(0,8).toString('hex')!=='89504e470d0a1a0a'||bytes.readUInt32BE(16)<1||bytes.readUInt32BE(20)<1)errors.push(`${version} ${relative}: invalid PNG header`);
        }
    }
    const ownResearch=new Map([...jsons].filter(([file])=>file.includes('/thaumaturge/research_entry/')).map(([file,value])=>[file.replace(/^data\/([^/]+)\/thaumaturge\/research_entry\/(.*)\.json$/,'$1:$2'),value]));
    const idPath=(id,type)=>{const separator=id.indexOf(':');return `data/${id.slice(0,separator)}/thaumaturge/${type}/${id.slice(separator+1)}.json`;};
    function registryRef(id,type,file) {
        if(typeof id!=='string'||!id.includes(':')) {errors.push(`${version} ${file}: invalid ${type} ID ${JSON.stringify(id)}`);return;}
        const target=idPath(id,type);
        if(!jsons.has(target)&&!index[target])errors.push(`${version} ${file}: missing ${type} ${id}`);
    }
    function itemRef(id,file) {
        if(typeof id!=='string'||id.startsWith('#'))return;
        const [namespace,...pieces]=id.split(':'),name=pieces.join(':');
        if(!name){errors.push(`${version} ${file}: invalid item ${id}`);return;}
        if(namespace==='thaumictinkerer'&&!resources.has(`assets/${namespace}/models/item/${name}.json`))errors.push(`${version} ${file}: missing own item model ${id}`);
        if(namespace==='thaumaturge'&&!index[`assets/${namespace}/models/item/${name}.json`]&&!index[`assets/${namespace}/items/${name}.json`])errors.push(`${version} ${file}: missing dependency item ${id}`);
    }
    function walk(value,file) {
        if(!value||typeof value!=='object')return;
        if(value.aspect&&typeof value.aspect==='string')registryRef(value.aspect,'aspect',file);
        if(typeof value.item==='string')itemRef(value.item,file);
        if(typeof value.id==='string'&&file.includes('/recipe/'))itemRef(value.id,file);
        if(value.amount!==undefined&&(!Number.isInteger(value.amount)||value.amount<1))errors.push(`${version} ${file}: nonpositive amount ${value.amount}`);
        if(Array.isArray(value))for(const entry of value)walk(entry,file);
        else for(const entry of Object.values(value))walk(entry,file);
    }
    for(const [file,value] of jsons) {
        walk(value,file);
        if(file.includes('/research_entry/')) {
            registryRef(value.category,'research_category',file);
            if(!Array.isArray(value.stages)||!value.stages.length)errors.push(`${version} ${file}: no research stages`);
            if(!Number.isInteger(value.complexity??1)||(value.complexity??1)<1||(value.complexity??1)>3)errors.push(`${version} ${file}: invalid complexity`);
            for(const parent of value.parents||[])registryRef(typeof parent==='string'?parent:parent.entry,'research_entry',file);
            for(const stage of value.stages||[]) {
                for(const required of stage.required_research||[])registryRef(required,'research_entry',file);
                for(const recipe of stage.recipes||[]) {
                    const [namespace,name]=recipe.split(':');const recipePath=`data/${namespace}/recipe/${name}.json`;
                    if(!jsons.has(recipePath)&&!index[recipePath])errors.push(`${version} ${file}: missing recipe ${recipe}`);
                }
            }
            for(const icon of value.icons||[]) {
                if(typeof icon!=='string')errors.push(`${version} ${file}: research icon must be a string`);
                else if(!icon.endsWith('.png')&&!icon.startsWith('focus:'))itemRef(icon,file);
            }
        }
        if(file.includes('/recipe/')) {
            if(typeof value.type!=='string')errors.push(`${version} ${file}: recipe has no type`);
            if(value.pattern&&value.key) {
                const used=new Set(value.pattern.join('').replaceAll(' ','').split(''));
                for(const key of used)if(!value.key[key])errors.push(`${version} ${file}: unmapped pattern key ${key}`);
                for(const key of Object.keys(value.key))if(!used.has(key))errors.push(`${version} ${file}: unused pattern key ${key}`);
                const widths=new Set(value.pattern.map(row=>row.length));if(widths.size!==1)errors.push(`${version} ${file}: unequal recipe row widths`);
            }
            if(value.research)registryRef(value.research.entry,'research_entry',file);
        }
        if(file.includes('/models/')) {
            if(typeof value.parent==='string'&&value.parent.startsWith('thaumictinkerer:')) {
                const target='assets/thaumictinkerer/models/'+value.parent.split(':')[1]+'.json';
                if(!resources.has(target))errors.push(`${version} ${file}: missing parent model ${value.parent}`);
            }
            for(const texture of Object.values(value.textures||{}))if(typeof texture==='string'&&texture.startsWith('thaumictinkerer:')) {
                const target='assets/thaumictinkerer/textures/'+texture.split(':')[1]+'.png';
                if(!resources.has(target))errors.push(`${version} ${file}: missing texture ${texture}`);
            }
        }
    }
    const visiting=new Set(),visited=new Set();
    function cycle(id,trail) {
        if(visiting.has(id)){errors.push(`${version} research cycle: ${[...trail,id].join(' -> ')}`);return;}
        if(visited.has(id)||!ownResearch.has(id))return;
        visiting.add(id);const value=ownResearch.get(id);
        const required=[...(value.parents||[]).map(p=>typeof p==='string'?p:p.entry),...(value.stages||[]).flatMap(s=>s.required_research||[])];
        for(const parent of required)cycle(parent,[...trail,id]);
        visiting.delete(id);visited.add(id);
    }
    for(const id of ownResearch.keys())cycle(id,[]);
    console.log(`${version}: checked ${jsons.size} JSON resources, ${ownResearch.size} research entries, ${[...resources.keys()].filter(x=>x.endsWith('.png')).length} PNGs.`);
}
errors=[...new Set(errors)];
fs.mkdirSync(path.join(ROOT,'validation'),{recursive:true});
fs.writeFileSync(path.join(ROOT,'validation/resource-report.json'),JSON.stringify({errors,checkedVersions:versions},null,2)+'\n');
if(errors.length){console.error(errors.join('\n'));process.exitCode=1;}else console.log('Resource references and research graphs passed.');
