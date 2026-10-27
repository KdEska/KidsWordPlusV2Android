"""Extract literal interface calls, preserving Swift/Kotlin interpolation as numbered slots."""
import re
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
LANGS=['tr','en','de','fr','es','zh','ja','ko','it','pt','hi','ar','ru']
def quoted(s,i,kotlin=False):
 j=i+1;out='';args=[]
 while j<len(s):
  c=s[j]
  if c=='"':return out,args,j+1
  if not kotlin and s[j:j+2]=='\\(':
   end=balanced(s,j+2,')',kotlin);args.append(s[j+2:end-1]);out+='{'+str(len(args)-1)+'}';j=end;continue
  if kotlin and c=='$':
   if s[j+1:j+2]=='{':
    end=balanced(s,j+2,'}',True);args.append(s[j+2:end-1]);j=end
   else:
    m=re.match(r'[A-Za-z_][A-Za-z_0-9]*',s[j+1:])
    if not m:out+=c;j+=1;continue
    args.append(m[0]);j+=len(m[0])+1
   out+='{'+str(len(args)-1)+'}';continue
  if c=='\\':
   out+= {'n':'\n','r':'\r','t':'\t','"':'"','\\':'\\','$':'$'}.get(s[j+1],s[j+1]);j+=2;continue
  out+=c;j+=1
 raise ValueError('unterminated string')
def balanced(s,i,close=')',kotlin=False):
 j=i
 while j<len(s):
  c=s[j]
  if c=='"':_,_,j=quoted(s,j,kotlin);continue
  if c in '({[':j=balanced(s,j+1,{'(':')','{':'}','[':']'}[c],kotlin);continue
  if c==close:return j+1
  j+=1
 raise ValueError('unterminated expression')
def arguments(s,start,kotlin=False):
 end=balanced(s,start,')',kotlin)-1;j=start;last=start;parts=[]
 while j<end:
  c=s[j]
  if c=='"':_,_,j=quoted(s,j,kotlin);continue
  if c in '({[':j=balanced(s,j+1,{'(':')','{':'}','[':']'}[c],kotlin);continue
  if c==',':parts.append(s[last:j].strip());last=j+1
  j+=1
 parts.append(s[last:end].strip());return parts,end+1

def calls():
 for platform,folder,suffix in [('ios','KidsWordPlusV2/App','swift'),('android','KidsWordPlusAndroid/app/src/main/java','kt')]:
  for p in sorted((ROOT/folder).rglob('*.'+suffix)):
   s=p.read_text();kt=platform=='android'
   for m in re.finditer(r'(?<![A-Za-z_])(?:model\.t|InterfaceStrings\.(?:text|render|named)|InterfaceLocalization\.text|t|localized)\(',s):
    try:
     parts,end=arguments(s,m.end(),kt)
     if m[0].startswith('InterfaceLocalization'):index=0
     elif m[0].startswith('InterfaceStrings'):index=2
     else:index=1
     if len(parts)<=index:continue
     value=parts[index]
     if not value.startswith('"'):continue
     key,args,stop=quoted(value,0,kt)
     if stop!=len(value):continue
     if not key.strip() or key in ('tr','en') or not re.search('[A-Za-z]',key):continue
     tr=None;trargs=[]
     if index and parts[index-1].startswith('"'):
      tr,trargs,tstop=quoted(parts[index-1],0,kt)
      if tstop!=len(parts[index-1]):tr=None
     yield dict(platform=platform,file=str(p.relative_to(ROOT)),line=s.count('\n',0,m.start())+1,key=key,args=args,turkish=tr,trargs=trargs,start=m.start(),end=end,parts=parts,call=m[0][:-1])
    except (ValueError,IndexError):continue

def inventory_keys():
 """Also include literals selected by conditional English expressions."""
 keys={x['key'] for x in calls()}
 for platform,folder,suffix in [('ios','KidsWordPlusV2/App','swift'),('android','KidsWordPlusAndroid/app/src/main/java','kt')]:
  kt=platform=='android'
  for p in (ROOT/folder).rglob('*.'+suffix):
   s=p.read_text()
   for m in re.finditer(r'(?<![A-Za-z_])(?:model\.t|InterfaceStrings\.(?:text|render|named)|InterfaceLocalization\.text|t|localized)\(',s):
    try:
     parts,_=arguments(s,m.end(),kt)
     index=0 if m[0].startswith('InterfaceLocalization') or (not kt and m[0].startswith('localized')) else 2 if m[0].startswith('InterfaceStrings') else 1
     if len(parts)<=index:continue
     text=parts[index];j=0
     while j<len(text):
      if text[j]=='"':
       value,_,j=quoted(text,j,kt)
       if value.strip() and value not in ('tr','en') and re.search('[A-Za-z]',value):keys.add(value)
      else:j+=1
    except (ValueError,IndexError):continue
 return keys
