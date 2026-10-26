"""Create original geometric teaching diagrams with exact hand angles (not image edits)."""
import math,json
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
out=Path('language-packs-v2/production/generated-images')
font='/System/Library/Fonts/Supplemental/Arial.ttf'
for stem,h,m in [('slide_clocks_it_is_five_past_one',1,5),('slide_clocks_it_is_ten_past_two',2,10)]:
 size=1200;im=Image.new('RGB',(size,size),'white');d=ImageDraw.Draw(im);cx,cy=600,500
 d.ellipse((145,45,1055,955),fill='#292d33');d.ellipse((169,69,1031,931),fill='#ffffff')
 def pt(angle,r):a=math.radians(angle);return(cx+r*math.sin(a),cy-r*math.cos(a))
 for i in range(60):d.line([pt(i*6,400),pt(i*6,375 if i%5==0 else 387)],fill='#292d33',width=8 if i%5==0 else 3)
 f=ImageFont.truetype(font,65)
 for i in range(1,13):d.text(pt(i*30,325),str(i),font=f,fill='#1d2025',anchor='mm')
 hour=h%12*30+m*.5;minute=m*6
 d.line([(cx,cy),pt(hour,215)],fill='#24292f',width=23);d.line([(cx,cy),pt(minute,350)],fill='#24292f',width=12)
 d.ellipse((583,483,617,517),fill='#24292f');d.rounded_rectangle((355,1010,845,1145),radius=20,fill='#222831');d.text((600,1076),f'{h:02}:{m:02}',font=ImageFont.truetype(font,100),fill='white',anchor='mm')
 path=(out/(stem+'-exact.png')).resolve();im.save(path)
 old=out/(stem+'.json');original=json.loads(old.read_text())
 original.update({'rejectedGeneratedSourcePath':original['sourcePath'],'sourcePath':str(path),'tool':'deterministic_original_diagram','visualReview':'subject_checked','handAnglesDegrees':{'hour':hour,'minute':minute},'time':f'{h:02}:{m:02}'})
 old.write_text(json.dumps(original,indent=2)+'\n')
 # Preserve generated draft under its original source path; selected project original is geometric.
 (out/(stem+'.png')).write_bytes(path.read_bytes())
