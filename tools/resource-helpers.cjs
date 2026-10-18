const fs = require('node:fs');
const path = require('node:path');
const ROOT = path.resolve(__dirname,'..');
const aliases = {
    air:'aer',earth:'terra',fire:'ignis',water:'aqua',order:'ordo',entropy:'perditio',void:'vacuos',light:'lux',
    weather:'aer',tempestas:'aer',motion:'motus',cold:'gelum',crystal:'vitreus',life:'victus',poison:'vitium',venenum:'vitium',
    energy:'potentia',exchange:'permutatio',metal:'metallum',death:'mortuus',flight:'volatus',darkness:'tenebrae',soul:'spiritus',
    heal:'victus',sano:'victus',travel:'motus',iter:'motus',eldritch:'alienis',magic:'praecantatio',aura:'auram',taint:'vitium',
    slime:'victus',limus:'victus',plant:'herba',tree:'herba',arbor:'herba',beast:'bestia',flesh:'victus',corpus:'victus',
    undead:'exanimis',mind:'cognitio',senses:'sensus',man:'humanus',crop:'herba',messis:'herba',mine:'instrumentum',perfodio:'instrumentum',
    tool:'instrumentum',harvest:'instrumentum',meto:'instrumentum',weapon:'aversio',telum:'aversio',armor:'praemunio',tutamen:'praemunio',
    hunger:'desiderium',fames:'desiderium',greed:'desiderium',lucrum:'desiderium',craft:'fabrico',cloth:'fabrico',pannus:'fabrico',
    mechanism:'machina',trap:'vinculum',
};
function modernAspect(name) {
    const bare = String(name).replace(/^.*:/,'').toLowerCase();
    return aliases[bare] || bare;
}
function aspects(cost) {
    const combined = new Map();
    for (const [name, amount] of Object.entries(cost)) {
        const key = modernAspect(name);
        combined.set(key, (combined.get(key) || 0) + amount);
    }
    return [...combined].map(([aspect,amount])=>({aspect:`thaumaturge:${aspect}`,amount}));
}
function ingredient(id) {return id.startsWith('#')?{tag:id.slice(1)}:{item:id};}
function write(relative, value, target='common/src/main/resources') {
    const destination=path.join(ROOT,target,relative);
    fs.mkdirSync(path.dirname(destination),{recursive:true});
    fs.writeFileSync(destination,typeof value==='string'?value:JSON.stringify(value,null,2)+'\n');
}
module.exports={ROOT, modernAspect,aspects,ingredient,write,aliases};
