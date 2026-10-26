# Beer content removal

The `food:792` concept was removed from every active language database (tr, en, de, fr, es, zh, ja, ko, it, pt, hi, ar, ru). Its 13 audio files and the unreferenced shared `images/slide_my_home_beer.jpg` were removed from active package folders and newly exported archives.

All 14 archive inventories, byte counts, SHA-256 checksums and shared-base dependency hashes were rebuilt and independently verified. The local catalog and Android Debug catalog select only the revised versions. Firebase preview upload files/catalog were regenerated locally; no upload to live Firebase Storage was performed. Historical archives, audit reports and backups are retained as history, and are not selected by the current catalogs.

Both Android LearningPack loading and iOS Word.enabled also exclude this concept ID, so older installed/downloaded packages cannot reintroduce it into lessons or games after updating the app. Existing progress is retained.

Run `python3 tools/remove_beer_from_packages.py` after regenerating raw vocabulary exports, then regenerate app catalogs and Firebase staging with their existing preparation scripts.

Validation: all 13 databases and 14 archives contain no beer record/media; all inventory checksums and dependencies pass. Android Debug builds and 142 unit tests pass, including an older package containing the excluded record. iOS core checks cover the disabled legacy record and updated lesson counts; package installation tests cover every language.
