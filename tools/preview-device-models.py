"""Offline, nearest-texture previews of native device JSON. Does not launch Minecraft.

Uses the native FaceInfo / BlockFaceUV corner order, not a second legacy-model
converter, so atlas orientation mistakes in generated JSON remain visible here.
"""
from pathlib import Path
from zipfile import ZipFile
from io import BytesIO
import json
import math
import numpy as np
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "common/src/main/resources/assets"
VANILLA = ZipFile(ROOT / "neoforge-1.21.1/build/moddev/artifacts/neoforge-21.1.238-client-extra-aka-minecraft-resources.jar")
FACES = {
    "down": [(0,0,1),(0,0,0),(1,0,0),(1,0,1)],
    "up": [(0,1,0),(0,1,1),(1,1,1),(1,1,0)],
    "north": [(1,1,0),(1,0,0),(0,0,0),(0,1,0)],
    "south": [(0,1,1),(0,0,1),(1,0,1),(1,1,1)],
    "west": [(0,1,0),(0,0,0),(0,0,1),(0,1,1)],
    "east": [(1,1,1),(1,0,1),(1,0,0),(1,1,0)],
}
NORMALS = {"down": (0,-1,0), "up": (0,1,0), "north": (0,0,-1), "south": (0,0,1), "west": (-1,0,0), "east": (1,0,0)}
TEXTURES = {}


def texture(name):
    if name in TEXTURES:
        return TEXTURES[name]
    namespace, relative = name.split(":", 1)
    local = ASSETS / namespace / "textures" / (relative + ".png")
    if local.exists():
        image = Image.open(local).convert("RGBA")
        animated = local.with_suffix(".png.mcmeta").exists()
    else:
        member = "assets/" + namespace + "/textures/" + relative + ".png"
        image = Image.open(BytesIO(VANILLA.read(member))).convert("RGBA")
        animated = member + ".mcmeta" in VANILLA.namelist()
    if animated:
        image = image.crop((0,0,image.width,image.width))
    TEXTURES[name] = np.asarray(image, dtype=float) / 255
    return TEXTURES[name]


def draw_triangle(canvas, points, uv, pixels, brightness):
    minimum = np.maximum(np.floor(points.min(axis=0)).astype(int), 0)
    maximum = np.minimum(np.ceil(points.max(axis=0)).astype(int), np.array([canvas.shape[1]-1, canvas.shape[0]-1]))
    if np.any(maximum < minimum):
        return
    x, y = np.meshgrid(np.arange(minimum[0], maximum[0]+1)+.5, np.arange(minimum[1], maximum[1]+1)+.5)
    a, b, c = points
    determinant = (b[1]-c[1])*(a[0]-c[0])+(c[0]-b[0])*(a[1]-c[1])
    if abs(determinant) < 1e-7:
        return
    p = ((b[1]-c[1])*(x-c[0])+(c[0]-b[0])*(y-c[1])) / determinant
    q = ((c[1]-a[1])*(x-c[0])+(a[0]-c[0])*(y-c[1])) / determinant
    r = 1-p-q
    mask = (p >= 0) & (q >= 0) & (r >= 0)
    sampling = p[...,None]*uv[0]+q[...,None]*uv[1]+r[...,None]*uv[2]
    tx = np.clip((sampling[...,0]*pixels.shape[1]/16).astype(int), 0, pixels.shape[1]-1)
    ty = np.clip((sampling[...,1]*pixels.shape[0]/16).astype(int), 0, pixels.shape[0]-1)
    colors = pixels[ty,tx].copy()
    colors[...,:3] *= brightness
    alpha = colors[...,3,None]*mask[...,None]
    region = canvas[minimum[1]:maximum[1]+1,minimum[0]:maximum[0]+1]
    region[:] = colors[...,:3]*alpha+region*(1-alpha)


def render(id, width=400, height=380, camera=(1.15,.85,-1.5)):
    model = json.loads((ASSETS / "thaumictinkerer/models/block" / (id+".json")).read_text())
    camera = np.array(camera, dtype=float)
    camera /= np.linalg.norm(camera)
    right = np.cross([0,1,0],camera)
    right /= np.linalg.norm(right)
    up = np.cross(camera,right)
    project = np.array([right, -up])
    scale = min(width/32, (height-55)/28)
    canvas = np.full((height,width,3), (.075,.091,.12))
    faces = []
    for element in model["elements"]:
        lo, hi = np.array(element["from"]), np.array(element["to"])
        for direction, face in element["faces"].items():
            normal = np.array(NORMALS[direction])
            if normal @ camera <= 0:
                continue
            points = np.array([np.where(corner,hi,lo) for corner in FACES[direction]])
            coordinates = (points-8) @ project.T*scale+[width/2,(height-45)/2]
            u0,v0,u1,v1 = face["uv"]
            uv = np.array([[u0,v0],[u0,v1],[u1,v1],[u1,v0]])
            uv = np.roll(uv,-face.get("rotation",0)//90,axis=0)
            tex = model["textures"][face["texture"][1:]]
            brightness = {"up":1.0,"down":.5,"north":.85,"south":.85,"west":.7,"east":.7}[direction]
            faces.append((points.mean(axis=0)@camera,coordinates,uv,texture(tex),brightness))
    for _,points,uv,pixels,brightness in sorted(faces,key=lambda face:face[0]):
        for triangle in [(0,1,2),(0,2,3)]:
            draw_triangle(canvas,points[list(triangle)],uv[list(triangle)],pixels,brightness)
    output = Image.fromarray((np.clip(canvas,0,1)*255).astype("uint8"))
    draw = ImageDraw.Draw(output)
    draw.text((18,height-35),id,fill="#e7ecf4",font=ImageFont.truetype("C:/Windows/Fonts/consola.ttf",19))
    if not faces:
        draw.text((20,height//2),"invisible temporary field",fill="#8193aa",font=ImageFont.truetype("C:/Windows/Fonts/consola.ttf",14))
    return output


def contact(ids, columns, target, camera=(1.15,.85,-1.5)):
    size = (400,380)
    sheet = Image.new("RGB",(columns*size[0],math.ceil(len(ids)/columns)*size[1]+55),(15,19,26))
    ImageDraw.Draw(sheet).text((20,17),"Native JSON geometry + original UVs | offline preview",fill="#e7ecf4",font=ImageFont.truetype("C:/Windows/Fonts/consola.ttf",23))
    for index,id in enumerate(ids):
        sheet.paste(render(id,camera=camera),((index%columns)*size[0],55+(index//columns)*size[1]))
    target.parent.mkdir(parents=True,exist_ok=True)
    sheet.save(target)
    print(target)


if __name__ == "__main__":
    ids = ["repairer","magnet","mob_magnet","funnel","enchanter","animation_tablet","summon_tablet","mobilizer","mobilizer_relay","remote_placer","transvector_dislocator","transvector_interface","golem_connector","aspect_analyzer","platform","camo","forcefield"]
    contact(ids,4,ROOT/"docs/device-model-overview.png")
    contact(ids[:3],3,ROOT/"docs/device-model-front.png")
    contact(ids[:3],3,ROOT/"docs/device-model-back.png",camera=(-1.15,.85,1.5))
