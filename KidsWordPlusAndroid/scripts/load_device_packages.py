"""Copy full development packages to one explicitly selected Android Debug device.
Does not upload content to any server or alter the reference archives.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import subprocess
import shlex

parser = argparse.ArgumentParser()
parser.add_argument('--serial', required=True, help='Device serial from adb devices')
parser.add_argument('--adb', default=str(Path(os.environ.get('ANDROID_HOME', str(Path.home() / 'Library/Android/sdk'))) / 'platform-tools/adb'))
args = parser.parse_args()
root = Path(__file__).resolve().parents[2]
package = 'com.demarpsoftware.kidswordplus.v2'
adb = [args.adb, '-s', args.serial]
# Install and launch the Debug APK once before using this helper.
subprocess.run(adb + ['shell', 'run-as', package, 'pwd'], check=True)
destination = 'files/packages'
subprocess.run(adb + ['shell', 'run-as', package, 'mkdir', '-p', destination], check=True)
catalog = json.loads((root / 'language-packs-v2/catalog.json').read_text())
for entry in catalog['packages']:
    if entry['language'] not in ('base','tr','en','fr','de','es','pt','zh','ja','ko','it','hi','ar','ru'):
        continue
    archive = root / 'language-packs-v2' / entry['localArchive']
    with archive.open('rb') as stream:
        digest = hashlib.file_digest(stream, 'sha256').hexdigest()
    if digest != entry['archiveSha256'] or archive.stat().st_size != entry['downloadBytes']:
        raise ValueError(f'Archive mismatch: {archive.name}')
    name = f"{entry['language']}-{digest}.zip"
    remote = 'run-as ' + shlex.quote(package) + ' sh -c ' + shlex.quote(f'cat > {destination}/{name}')
    with archive.open('rb') as stream:
        subprocess.run(adb + ['shell', '-T', remote], stdin=stream, check=True)
    copied = subprocess.check_output(adb + ['shell', 'run-as', package, 'stat', '-c', '%s', f'{destination}/{name}'], text=True).strip()
    if int(copied) != entry['downloadBytes']:
        raise ValueError(f'Device copy incomplete: {name}')
    print(f"Copied {entry['language']}: {copied} bytes", flush=True)
print('Development packages copied. Open Language packs in the app and install a language.')
