#!/usr/bin/env python3
"""Sync the original 13-language secret catalog to both native clients."""
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parent.parent
master=json.loads((ROOT/'planning/portal-refresh/chess-secrets.json').read_text())
tips=master['tips'];languages=master['languages']
assert len(tips)>=40 and len({t['id'] for t in tips})==len(tips)
for tip in tips:
    assert set(tip['text'])==set(languages)
    assert all(isinstance(v,str) and v.strip() for v in tip['text'].values())
for language in languages:
    assert len({t['text'][language] for t in tips})==len(tips),language
for relative in ['KidsWordPlusV2/Localization/interface.json','KidsWordPlusAndroid/app/src/main/assets/localization/interface.json']:
    path=ROOT/relative;catalog=json.loads(path.read_text())
    for language in languages:
        if language in ['tr','en']:continue
        for tip in tips:catalog[language][tip['text']['en']]=tip['text'][language]
    path.write_text(json.dumps(catalog,ensure_ascii=False,sort_keys=True,indent=2)+'\n')
def quoted(value):return json.dumps(value,ensure_ascii=False)
swift='// BEGIN GENERATED CHESS SECRETS\nenum ChessSecretCatalog {\n    static let all:[(id:String,turkish:String,english:String)] = [\n'
swift+=',\n'.join('        ('+','.join(quoted(v) for v in [tip['id'],tip['text']['tr'],tip['text']['en']])+')' for tip in tips)
swift+='''
    ]
    static let selected:(id:String,turkish:String,english:String) = {
        let key="chess.secret.deck.v1"
        var deck=AppStorageLocation.defaults.data(forKey:key).flatMap {try? JSONDecoder().decode(ChessSecretDeck.self,from:$0)} ?? ChessSecretDeck()
        var random=SystemRandomNumberGenerator()
        let id=deck.draw(from:all.map(\\.id),using:&random)
        if let data=try? JSONEncoder().encode(deck) {AppStorageLocation.defaults.set(data,forKey:key)}
        return all.first {$0.id==id}!
    }()
}
// END GENERATED CHESS SECRETS
'''
path=ROOT/'KidsWordPlusV2/App/HomeViews.swift';source=path.read_text();start=source.find('// BEGIN GENERATED CHESS SECRETS')
if start>=0:source=source[:start]+source[source.index('// END GENERATED CHESS SECRETS',start)+len('// END GENERATED CHESS SECRETS'):].lstrip('\n')
path.write_text(source.rstrip()+'\n\n'+swift)
kotlin='// BEGIN GENERATED CHESS SECRETS\ninternal val chessSecretCopies=listOf(\n'
kotlin+=',\n'.join('    ChessSecretCopy('+','.join(quoted(v) for v in [tip['id'],tip['text']['tr'],tip['text']['en']])+')' for tip in tips)
kotlin+='\n)\n// END GENERATED CHESS SECRETS\n'
path=ROOT/'KidsWordPlusAndroid/app/src/main/java/com/demarpsoftware/kidswordplus/ChessSecrets.kt';source=path.read_text();start=source.find('// BEGIN GENERATED CHESS SECRETS')
if start>=0:source=source[:start]+source[source.index('// END GENERATED CHESS SECRETS',start)+len('// END GENERATED CHESS SECRETS'):].lstrip('\n')
path.write_text(source.rstrip()+'\n\n'+kotlin)
print(f'Synced {len(tips)} secrets in {len(languages)} languages.')
