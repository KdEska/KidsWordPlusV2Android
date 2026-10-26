import json, re
from pathlib import Path
root = Path(__file__).resolve().parents[2]
source = (root / "app/src/main/java/com/demarpsoftware/kidswordplus/SpeakingPractice.kt").read_text()
keys = set(re.findall(r't\(\s*"(?:[^"\\]|\\.)*"\s*,\s*"((?:[^"\\]|\\.)*)"', source))
keys.add("Listen to the word, then say it yourself.")
data = json.loads((root / "app/src/main/assets/localization/interface.json").read_text())
for locale in ["de", "es", "fr", "hi", "it", "ja", "ko", "pt", "zh"]:
    assert all(data[locale].get(key) for key in keys), (locale, keys - data[locale].keys())
print("PASS:", len(keys), "speaking strings × 9 translated UI languages")
