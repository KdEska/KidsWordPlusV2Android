"""Generate original, short, nonverbal UI cues identically for Android and iOS."""
from pathlib import Path
import math,struct,wave
ROOT=Path(__file__).resolve().parents[1]
CUES={'tap':[(700,.055),(1050,.055)],'page':[(440,.07),(660,.08)],'welcome':[(523.25,.10),(659.25,.10),(783.99,.18)],'start':[(659.25,.07),(783.99,.07),(1046.5,.13)],'success':[(783.99,.08),(1046.5,.13)],'retry':[(392,.08),(349.23,.12)]}
CUES['chessTouch']=[(880,.045),(1174,.045)]
CUES.update({'category':[(880,.04),(1174,.055)],'success2':[(659,.07),(880,.07),(1318,.12)],'success3':[(1046,.06),(1318,.06),(1568,.13)],'celebration':[(523,.09),(659,.09),(784,.09),(1046,.22)],'unlock':[(392,.10),(523,.10),(659,.10),(1046,.25)]})
for folder in [ROOT/'KidsWordPlusV2/InteractionAudio',ROOT/'KidsWordPlusAndroid/app/src/main/assets/interaction']:
 folder.mkdir(parents=True,exist_ok=True)
 for name,notes in CUES.items():
  samples=[]
  for frequency,duration in notes:
   n=int(duration*22050)
   for i in range(n):
    t=i/22050;envelope=min(1,i/180,(n-i)/350)*math.exp(-3*i/n)
    samples.append(int(32767*.32*envelope*(math.sin(2*math.pi*frequency*t)+.18*math.sin(4*math.pi*frequency*t))/1.18))
  with wave.open(str(folder/f'{name}.wav'),'wb') as w:w.setnchannels(1);w.setsampwidth(2);w.setframerate(22050);w.writeframes(struct.pack('<'+'h'*len(samples),*samples))
print(f'{len(CUES)} original cues generated for both platforms.')

# A short original steam-train departure: whistle, accelerating wheel pulses and steam.
import random
rng=random.Random(7923)
rate=22050
duration=4.8
train=[]
for i in range(int(rate*duration)):
 t=i/rate
 fade=min(1,t/.05,(duration-t)/.45)
 whistle_gate=max(0,1-abs(t-.38)/.36)+.7*max(0,1-abs(t-1.0)/.25)
 whistle=whistle_gate*(math.sin(2*math.pi*311*t)+.6*math.sin(2*math.pi*466*t))*.16
 pulse_phase=(3.2*t+.6*t*t)%1
 chuff=math.exp(-pulse_phase*13)
 noise=rng.uniform(-1,1)
 wheels=chuff*(noise*.15+math.sin(2*math.pi*82*t)*.08)
 steam=noise*.018
 train.append(int(32767*max(-.9,min(.9,fade*(whistle+wheels+steam)))))
for folder in [ROOT/'KidsWordPlusV2/InteractionAudio',ROOT/'KidsWordPlusAndroid/app/src/main/assets/interaction']:
 with wave.open(str(folder/'train.wav'),'wb') as w:
  w.setnchannels(1);w.setsampwidth(2);w.setframerate(rate);w.writeframes(struct.pack('<'+'h'*len(train),*train))
print('Original 4.8-second train departure generated for both platforms.')

# Bright original bell alternatives; each fits the 250 ms star cadence.
rate=44100
previews=ROOT/'planning/interaction-audio/star-sounds'
previews.mkdir(parents=True,exist_ok=True)
variants={'ting':[(1568,0,1)],'bling':[(1318.51,0,.82),(2093,.065,1)],'chime':[(1046.5,0,.72),(1318.51,.045,.80),(1568,.09,1)]}
for name,notes in variants.items():
 samples=[]
 for i in range(int(rate*.24)):
  t=i/rate
  sample=0
  for frequency,onset,weight in notes:
   u=t-onset
   if u<0:continue
   attack=min(1,u/.0012)
   ring=sum(gain*math.sin(2*math.pi*frequency*ratio*u)*math.exp(-decay*u)
            for ratio,gain,decay in [(1,1,12),(2,.38,18),(2.756,.15,24),(5.404,.05,40)])
   sample+=weight*attack*ring
  samples.append(sample*min(1,max(0,(.24-t)/.025)))
 peak=max(abs(x) for x in samples)
 pcm=struct.pack('<'+'h'*len(samples),*(int(32767*.72*x/peak) for x in samples))
 targets=[previews/f'{name}.wav']
 if name=='bling':targets += [ROOT/'KidsWordPlusV2/InteractionAudio/coin.wav',ROOT/'KidsWordPlusAndroid/app/src/main/assets/interaction/coin.wav']
 for target in targets:
  with wave.open(str(target),'wb') as w:
   w.setnchannels(1);w.setsampwidth(2);w.setframerate(rate);w.writeframes(pcm)
print('Ting, bling and chime previews generated; bling installed as the star-arrival cue on both platforms.')
