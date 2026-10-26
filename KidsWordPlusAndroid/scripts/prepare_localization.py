"""Copy existing iOS menu translations into owned Android assets; reference stays read-only."""
import json
from pathlib import Path
root=Path(__file__).resolve().parents[2]
source=root/'KidsWordPlusV2/Localization/interface.json'
data=json.loads(source.read_text())
for code in ('de','fr','es','zh','ja','ko','it','pt','hi'):
    if not isinstance(data.get(code),dict) or len(data[code])<200:
        raise ValueError(f'Missing interface translations: {code}')
# Chess has its own keyed dictionaries in iOS; expose the English text keys used by Android.
for filename in ('interface.json','content.json'):
    chess=json.loads((root/'KidsWordPlusV2/Chess'/filename).read_text())
    for code,values in chess.items():
        for key,english in chess['en'].items():
            if isinstance(english,str) and isinstance(values.get(key),str):
                data.setdefault(code,{}).setdefault(english,values[key])
aliases={
    'The Base language pack with shared images installs with your first language. Then you can learn offline.':'The Base language pack with shared images installs automatically with your first language. Other languages reuse those images. Then you can learn offline.',
    "Let's start learning":'Start learning',
    'Each side can have at most 16 pieces and 8 pawns.':'Each side may have up to 16 pieces and 8 pawns.',
    'Kings cannot be adjacent.':'Kings cannot be next to each other.',
    'The side not to move cannot be in check. Change the position or side to move.':'The side not moving first cannot be in check. Change the first side or the position.',
    'This position is already finished. Change the checkmate, stalemate or insufficient material.':'The game is already over in this position. Set up a position where play can continue.',
    'Won':'Wins','Lost':'Losses',
    'Time to Review'  :'Time to review',
    'Audio could not play. Try listening again.':'Audio could not play. Try again.',
    'Remove pack?':'Remove this pack? Your progress will be kept.',
    'This language pack will be removed from this device. Learning progress is kept.':'Remove this pack? Your progress will be kept.',
    'Install a language pack to start learning.':'Prepare your chosen language pack to start learning.',
    'Install the selected language before continuing.':'Prepare your chosen language pack to start learning.',
    'Chess':'Chess World' ,'Chess world':'Chess World','Match settings':'Settings',
    'Narrate moves':'Spoken moves','Move animations':'Piece movement effects',
    'Chess clock':'Timed game','Time expired':'Time is up',
    'Computer difficulty':'Level','Statistics':'Your statistics',
    'Your name':'Your name','Name':'Your name',
    'Refresh list':'Refresh','Dismiss':'Close','Flip board':'Flip board',
}
for values in data.values():
    for alias,canonical in aliases.items():
        if canonical in values: values.setdefault(alias,values[canonical])
extra=root/'KidsWordPlusAndroid/app/src/main/assets/localization/android.json'
if extra.exists():
    for code,values in json.loads(extra.read_text()).items():
        data.setdefault(code,{}).update(values)
for values in data.values():
    for alias,canonical in aliases.items():
        if canonical in values: values.setdefault(alias,values[canonical])
target=root/'KidsWordPlusAndroid/app/src/main/assets/localization/interface.json'
target.parent.mkdir(parents=True,exist_ok=True)
target.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
print('iOS menu and chess translations prepared for Android.')
