/*
 * Native block models from Thaumic Tinkerer 1.7.10 model geometry and artwork.
 * Original ModelRepairer/ModelMagnet: Vazkii; CC BY-NC-SA 3.0, see LICENSE.
 * No dependency model, dependency texture, or legacy renderer is bundled.
 */
const fs = require('node:fs');
const path = require('node:path');
const { ROOT, write } = require('./resource-helpers.cjs');
const UPSTREAM = path.resolve(ROOT, '../.thaumic-tinkerer-1.7.10-upstream');
const ART = path.join(UPSTREAM, 'src/main/resources/assets/ttinkerer');
const MODEL_JAVA = path.join(UPSTREAM, 'src/main/java/thaumic/tinkerer/client/model');
const ASSETS = 'assets/thaumictinkerer';
const DIRECTIONS = ['down', 'up', 'north', 'south', 'west', 'east'];
const ROUND = n => Math.round(n * 1e6) / 1e6;

// The exact vertex order used by vanilla FaceInfo and BlockFaceUV. Kept explicit
// because swapping a face or merely copying a UV rectangle mirrors legacy art.
const FACE_VERTICES = {
    down:  [[0,0,1], [0,0,0], [1,0,0], [1,0,1]],
    up:    [[0,1,0], [0,1,1], [1,1,1], [1,1,0]],
    north: [[1,1,0], [1,0,0], [0,0,0], [0,1,0]],
    south: [[0,1,1], [0,0,1], [1,0,1], [1,1,1]],
    west:  [[0,1,0], [0,0,0], [0,0,1], [0,1,1]],
    east:  [[1,1,1], [1,0,1], [1,0,0], [1,1,0]],
};
function assert(condition, message) { if (!condition) throw new Error(message); }
function numbers(text) { return text.split(',').map(n => Number(n.trim().replace(/[fFdD]$/, ''))); }
function matchNumbers(text, expression, name) {
    const result = text.match(expression);
    assert(result, `Missing ${name}`);
    const value = numbers(result[1]);
    assert(value.every(Number.isFinite), `Invalid numeric ${name}`);
    return value;
}
function parseLegacyModel(name) {
    const source = fs.readFileSync(path.join(MODEL_JAVA, `${name}.java`), 'utf8');
    const width = Number(source.match(/textureWidth\s*=\s*(\d+)/)[1]);
    const height = Number(source.match(/textureHeight\s*=\s*(\d+)/)[1]);
    const starts = [...source.matchAll(/(\w+)\s*=\s*new ModelRenderer\(this,\s*(-?[\d.]+),\s*(-?[\d.]+)\);/g)];
    const parts = starts.map((start, index) => {
        const name = start[1];
        const body = source.slice(start.index, index + 1 < starts.length ? starts[index + 1].index : source.indexOf('public void render()'));
        const box = matchNumbers(body, new RegExp(`${name}\\.addBox\\(([^)]+)\\)`), `${name}.addBox`);
        const pivot = matchNumbers(body, new RegExp(`${name}\\.setRotationPoint\\(([^)]+)\\)`), `${name}.pivot`);
        const rotation = matchNumbers(body, new RegExp(`setRotation\\(${name},\\s*([^)]+)\\)`), `${name}.rotation`);
        assert(box.length === 6 && pivot.length === 3 && rotation.length === 3, `Unsupported legacy part ${name}`);
        for (const angle of rotation) assert(Math.abs(angle / (Math.PI / 2) - Math.round(angle / (Math.PI / 2))) < 1e-5,
            `${name}: only exact quarter-turn source rotations can be baked into native elements`);
        return { name, uv: [Number(start[2]), Number(start[3])], box, pivot, rotation, width, height };
    });
    assert(parts.length, `No boxes parsed from ${name}`);
    return parts;
}
function rotate(vector, rotation) {
    let [x, y, z] = vector;
    for (let axis = 0; axis < 3; axis++) {
        const angle = Math.round(rotation[axis] / (Math.PI / 2)) * Math.PI / 2;
        const c = Math.round(Math.cos(angle)), s = Math.round(Math.sin(angle));
        if (axis === 0) [y, z] = [y*c - z*s, y*s + z*c];
        if (axis === 1) [x, z] = [x*c + z*s, -x*s + z*c];
        if (axis === 2) [x, y] = [x*c - y*s, x*s + y*c];
    }
    return [x, y, z];
}
function transform(point, part) {
    const value = rotate(point, part.rotation).map((v, axis) => v + part.pivot[axis]);
    // Both original TESRs use translate(0.5,1.5,0.5), scale(1,-1,-1).
    return [8 + value[0], 24 - value[1], 8 - value[2]].map(ROUND);
}
function nativeVertices(face, from, to) { return FACE_VERTICES[face].map(v => v.map((max, axis) => max ? to[axis] : from[axis])); }
function samePoint(a, b) { return a.every((n, i) => Math.abs(n - b[i]) < 1e-5); }
function faceDirection(points) {
    const a = points[1].map((v, i) => v - points[0][i]), b = points[2].map((v, i) => v - points[0][i]);
    const n = [a[1]*b[2]-a[2]*b[1], a[2]*b[0]-a[0]*b[2], a[0]*b[1]-a[1]*b[0]];
    const axis = n.findIndex(v => Math.abs(v) > 1e-5);
    if (axis < 0) return null; // The four side quads of a zero-thickness plane have no area.
    return axis === 0 ? n[axis] > 0 ? 'east' : 'west' : axis === 1 ? n[axis] > 0 ? 'up' : 'down' : n[axis] > 0 ? 'south' : 'north';
}
function nativeUV(uv, rotation, index) {
    const shifted = (index + rotation / 90) % 4;
    return [uv[shifted === 0 || shifted === 1 ? 0 : 2], uv[shifted === 0 || shifted === 3 ? 1 : 3]];
}
function solveNativeUV(points, legacyUV, face, from, to, name) {
    const ordered = nativeVertices(face, from, to).map(vertex => {
        const index = points.findIndex(point => samePoint(point, vertex));
        assert(index >= 0, `${name}/${face}: source and target corner mismatch`);
        return legacyUV[index];
    });
    const us = ordered.map(uv => uv[0]), vs = ordered.map(uv => uv[1]);
    const u = [Math.min(...us), Math.max(...us)], v = [Math.min(...vs), Math.max(...vs)];
    for (const mirrorU of [false, true]) for (const mirrorV of [false, true]) for (const rotation of [0,90,180,270]) {
        const uv = [u[mirrorU ? 1 : 0],v[mirrorV ? 1 : 0],u[mirrorU ? 0 : 1],v[mirrorV ? 0 : 1]].map(ROUND);
        if (ordered.every((expected,index) => nativeUV(uv,rotation,index).every((value,axis) => Math.abs(value-expected[axis]) < 1e-5)))
            return { uv, ...(rotation ? { rotation } : {}), texture: '#texture' };
    }
    throw new Error(`${name}/${face}: legacy UVs cannot be expressed by a native face`);
}
function convertPart(part) {
    const [x,y,z,dx,dy,dz] = part.box, [u,v] = part.uv;
    const source = [[x,y,z],[x+dx,y,z],[x+dx,y+dy,z],[x,y+dy,z],[x,y,z+dz],[x+dx,y,z+dz],[x+dx,y+dy,z+dz],[x,y+dy,z+dz]];
    // ModelBox/TexturedQuad unfold: east, west, top, bottom, north, south.
    const quads = [
        [[5,1,2,6],[u+dz+dx,v+dz,u+dz+dx+dz,v+dz+dy]],
        [[0,4,7,3],[u,v+dz,u+dz,v+dz+dy]],
        [[5,4,0,1],[u+dz,v,u+dz+dx,v+dz]],
        [[2,3,7,6],[u+dz+dx,v+dz,u+dz+dx+dx,v]],
        [[1,0,3,2],[u+dz,v+dz,u+dz+dx,v+dz+dy]],
        [[4,5,6,7],[u+dz+dx+dz,v+dz,u+dz+dx+dz+dx,v+dz+dy]],
    ];
    const vertices = source.map(vertex => transform(vertex, part));
    const from = [0,1,2].map(axis => Math.min(...vertices.map(v => v[axis])));
    const to = [0,1,2].map(axis => Math.max(...vertices.map(v => v[axis])));
    const faces = {};
    for (const [indices, rect] of quads) {
        const points = indices.map(i => vertices[i]), face = faceDirection(points);
        if (!face) continue;
        const [u0,v0,u1,v1] = rect;
        const uv = [[u1,v0],[u0,v0],[u0,v1],[u1,v1]].map(pair => [pair[0]*16/part.width,pair[1]*16/part.height]);
        faces[face] = solveNativeUV(points, uv, face, from, to, part.name);
    }
    return { name: part.name, from, to, faces };
}
function copyTexture(source, id) {
    const target = path.join(ROOT, 'common/src/main/resources', ASSETS, 'textures/block', `${id}.png`);
    fs.mkdirSync(path.dirname(target), { recursive: true });
    const original = path.join(ART, 'textures', `${source}.png`);
    assert(fs.existsSync(original), `Missing original texture ${original}`);
    fs.copyFileSync(original, target);
    if (fs.existsSync(original + '.mcmeta')) fs.copyFileSync(original + '.mcmeta', target + '.mcmeta');
    return `thaumictinkerer:block/${id}`;
}
function modelFiles(id, model, orientation = 'horizontal') {
    write(`${ASSETS}/models/block/${id}.json`, model);
    const variants = {};
    for (const face of DIRECTIONS) {
        const rotation = orientation === 'six' ? { north:{},east:{y:90},south:{y:180},west:{y:270},up:{x:270},down:{x:90} }[face]
            : orientation === 'horizontal' ? { north:{},east:{y:90},south:{y:180},west:{y:270},up:{},down:{} }[face] : {};
        variants[`facing=${face}`] = { model: `thaumictinkerer:block/${id}`, ...rotation };
    }
    write(`${ASSETS}/blockstates/${id}.json`, { variants });
    write(`${ASSETS}/models/item/${id}.json`, { parent: `thaumictinkerer:block/${id}` });
    write(`${ASSETS}/items/${id}.json`, { model: { type:'minecraft:model',model:`thaumictinkerer:item/${id}` } }, 'neoforge-26.1.2/src/main/resources');
}
function box(from, to, textures) {
    const [x0,y0,z0] = from, [x1,y1,z1] = to;
    const uv = {
        down:[x0,16-z1,x1,16-z0], up:[x0,z0,x1,z1],
        north:[16-x1,16-y1,16-x0,16-y0], south:[x0,16-y1,x1,16-y0],
        west:[z0,16-y1,z1,16-y0], east:[16-z1,16-y1,16-z0,16-y0],
    };
    return { from,to,faces:Object.fromEntries(DIRECTIONS.map(face => [face,{texture:`#${textures[face] || textures.side || 'all'}`,uv:uv[face]}])) };
}
function regular(id, layers, {height=16,bottom=0,front=false,orientation='horizontal',renderType}={}) {
    const textures = Object.fromEntries(Object.entries(layers).map(([key,source]) => [key,source.startsWith('minecraft:') ? source : copyTexture(source,`${id}_${key}`)]));
    const faces = layers.all ? {} : { down:'bottom',up:'top',side:'side',...(front ? {north:'front'} : {}) };
    const model = { parent:'minecraft:block/block',textures:{...textures,particle:textures.side || textures.all || textures.top},elements:[box([0,bottom,0],[16,bottom+height,16],faces)] };
    if (renderType) model.render_type = renderType;
    modelFiles(id, model, orientation);
    return model;
}
function generateDeviceModels() {
    const stats = [];
    for (const [id,java,art,type,orientation] of [
        ['repairer','ModelRepairer','repairer','minecraft:translucent','six'],
        ['magnet','ModelMagnet','magnetS','minecraft:cutout','static'],
        ['mob_magnet','ModelMagnet','mobMagnetS','minecraft:cutout','static'],
    ]) {
        const parts = parseLegacyModel(java), elements = parts.map(convertPart);
        const texture = copyTexture(`model/${art}`,id);
        const model = { parent:'minecraft:block/block',ambientocclusion:false,render_type:type,
            textures:{texture,particle:'minecraft:block/iron_block'},elements };
        modelFiles(id,model,orientation);
        stats.push({id,parts:elements.length,faces:elements.reduce((n,e)=>n+Object.keys(e.faces).length,0)});
    }
    regular('funnel',{bottom:'blocks/funnel0',top:'blocks/funnel0',side:'blocks/funnel1'},{height:2,orientation:'static'});
    regular('enchanter',{bottom:'blocks/enchanter0',top:'blocks/enchanter1',side:'blocks/enchanter2'},{height:12});
    regular('animation_tablet',{bottom:'blocks/animationTablet0',top:'blocks/animationTablet1',side:'blocks/animationTablet2'},{height:2});
    regular('summon_tablet',{bottom:'minecraft:block/obsidian',top:'blocks/spawner1',side:'blocks/spawner2'},{height:2});
    regular('mobilizer',{bottom:'blocks/Levitational Locomotive0',top:'blocks/Levitational Locomotive1',side:'blocks/Levitational Locomotive2'});
    regular('mobilizer_relay',{bottom:'blocks/Levitational Locomotive Relay0',top:'blocks/Levitational Locomotive Relay1',side:'blocks/Levitational Locomotive Relay2'});
    regular('remote_placer',{bottom:'blocks/remotePlacer0',top:'blocks/remotePlacer0',side:'blocks/remotePlacer0',front:'blocks/remotePlacer1'},{front:true,orientation:'six'});
    regular('transvector_dislocator',{bottom:'blocks/dislocator0',top:'blocks/dislocator0',side:'blocks/dislocator0',front:'blocks/dislocator1'},{front:true,orientation:'six'});
    regular('transvector_interface',{all:'blocks/interface'},{orientation:'static'});
    regular('golem_connector',{all:'blocks/golemConnector'},{orientation:'static'});
    // These two are rendered with copied block appearances in-world; match their
    // default oak disguise and platform height in the inventory as well.
    regular('platform',{all:'minecraft:block/oak_planks'},{height:2,bottom:14,orientation:'static'});
    regular('camo',{all:'minecraft:block/oak_planks'},{orientation:'static'});
    const analyzer = regular('aspect_analyzer',{bottom:'blocks/aspectAnalyzer0',top:'blocks/aspectAnalyzer0',side:'blocks/aspectAnalyzer1'});
    for (const [face,index] of [['north',1],['south',2],['west',3],['east',4]]) {
        analyzer.textures[face] = copyTexture(`blocks/aspectAnalyzer${index}`,`aspect_analyzer_${face}`);
        analyzer.elements[0].faces[face].texture = `#${face}`;
    }
    modelFiles('aspect_analyzer',analyzer);
    modelFiles('forcefield',{parent:'minecraft:block/block',textures:{particle:'minecraft:block/glass'},elements:[]},'static');
    validateDeviceModels();
    return stats;
}
function validateDeviceModels() {
    const ids = ['repairer','magnet','mob_magnet','funnel','enchanter','animation_tablet','summon_tablet','mobilizer','mobilizer_relay','remote_placer','transvector_dislocator','transvector_interface','golem_connector','platform','camo','aspect_analyzer','forcefield'];
    for (const id of ids) {
        const model = JSON.parse(fs.readFileSync(path.join(ROOT,'common/src/main/resources',ASSETS,'models/block',`${id}.json`)));
        const states = JSON.parse(fs.readFileSync(path.join(ROOT,'common/src/main/resources',ASSETS,'blockstates',`${id}.json`)));
        assert(Object.keys(states.variants).length === 6, `${id}: missing facing variants`);
        for (const element of model.elements) {
            assert(element.from.every((v,i) => v >= -16 && v <= element.to[i] && element.to[i] <= 32),`${id}: invalid model bounds`);
            for (const [face,value] of Object.entries(element.faces)) {
                assert(value.uv.length===4 && value.uv.every(v => Number.isFinite(v) && v>=0 && v<=16),`${id}/${element.name || 'body'}/${face}: invalid UV`);
                assert(model.textures[value.texture.slice(1)],`${id}: unresolved face texture ${value.texture}`);
            }
        }
        for (const texture of Object.values(model.textures)) if (texture.startsWith('thaumictinkerer:'))
            assert(fs.existsSync(path.join(ROOT,'common/src/main/resources/assets/thaumictinkerer/textures',texture.split(':')[1]+'.png')),`${id}: missing texture ${texture}`);
    }
    assert(parseLegacyModel('ModelRepairer').length===18,'Repairer model lost an original part');
    assert(parseLegacyModel('ModelMagnet').length===7,'Magnet model lost an original part');
}
module.exports = {generateDeviceModels,validateDeviceModels,parseLegacyModel,convertPart,nativeVertices,nativeUV};
if (require.main===module) console.log(JSON.stringify({generated:17,legacyModels:generateDeviceModels(),validation:'passed'},null,2));
