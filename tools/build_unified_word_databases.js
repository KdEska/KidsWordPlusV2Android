const fs = require('fs');
const path = require('path');

const sourceDir = '/Users/kursatdemirci/Projects/KidsWordPlus/BACKUP/KidsWordPlusPro/KidsWordPlusPro/database';
const outputDir = path.resolve('database-output');

const sources = [
  ['animals', 'Animals', 'database_animal_world_en.json'],
  ['buildings', 'Buildings', 'database_buildings_en.json'],
  ['clocks', 'Clocks', 'database_clocks_en.json'],
  ['countries', 'Countries', 'database_countries_en.json'],
  ['space', 'Space', 'database_discover_space_en.json'],
  ['farm', 'Farm', 'database_farm_en.json'],
  ['flags', 'Flags', 'database_flags_en.json'],
  ['food', 'Food', 'database_foods_en.json'],
  ['fruits_vegetables', 'Fruits & Vegetables', 'database_fruits_vegetables_en.json'],
  ['heavy_equipment', 'Heavy Equipment', 'database_heavy_equipments_en.json'],
  ['human_body', 'Human Body', 'database_human_body_en.json'],
  ['musical_instruments', 'Musical Instruments', 'database_musical_instruments_en.json'],
  ['home', 'Home', 'database_my_home_en.json'],
  ['school', 'School', 'database_my_school_en.json'],
  ['professions', 'Professions', 'database_professions_en.json'],
];

const corrections = {
  'Mirket': 'Meerkat', 'Musium': 'Museum', 'Super market': 'Supermarket',
  'Astroid': 'Asteroid', 'Vegatables': 'Vegetables', 'Baloon': 'Balloon',
  'Breaker Exacavator': 'Breaker Excavator', 'Telescophic Crane': 'Telescopic Crane',
  'Hearth': 'Heart', 'Flut': 'Flute', 'Obua': 'Oboe', 'Diomand': 'Diamond',
  'Earing': 'Earring', 'Mintens': 'Mittens', 'Fourty': 'Forty',
  'Octagen': 'Octagon', 'Paranthesis': 'Parenthesis', 'Back-pack': 'Backpack',
  'Bath glove': 'Bath Glove', 'Bath puff': 'Bath Puff', 'Tea spoon': 'Teaspoon',
  'Tooth paste': 'Toothpaste', 'Wind pipe-trachea': 'Windpipe / Trachea',
  'Spine-backbone': 'Spine / Backbone', 'Little finger-pinkie': 'Little Finger / Pinkie',
  'Eswatini / Swaziland': 'Eswatini', 'Myanmar / Burma': 'Myanmar',
};

const beginnerByCategory = {
  animals: ['cat','dog','bird','fish','cow','duck','horse','lion','tiger','elephant','monkey','rabbit','sheep','pig','frog','mouse','bee','butterfly','chicken','goat'],
  buildings: ['home','school','hospital','hotel','park','airport','bank','cafe','castle','church','cinema','factory','library','mosque','office','playground','restaurant','street','theatre','gym'],
  countries: ['turkey','united states of america','united kingdom','canada','mexico','brazil','france','germany','italy','spain','china','japan','india','australia','russia','egypt','greece','netherlands','south korea','new zealand'],
  flags: ['turkey','united states of america','united kingdom','canada','mexico','brazil','france','germany','italy','spain','china','japan','india','australia','russia','egypt','greece','netherlands','south korea','new zealand'],
  space: ['earth','moon','sun','star','planet','mars','venus','jupiter','saturn','rocket','astronaut','telescope','comet','meteor','galaxy'],
  farm: ['farm','barn','cow','horse','pig','sheep','goat','chick','hen','rooster','duck','tractor','fence','grass','milk','corn','flower','tree','leaf','fruit'],
  food: ['bread','cake','candy','cheese','chocolate','coffee','cookie','egg','honey','ice cream','juice','milk','pizza','popcorn','salt','sandwich','soup','sugar','tea','water'],
  fruits_vegetables: ['apple','banana','orange','strawberry','watermelon','grape','lemon','peach','pear','cherry','pineapple','carrot','potato','tomato','onion','corn','pepper','cucumber','lettuce','mushroom'],
  heavy_equipment: ['car','automobile','boat','bulldozer','crane','excavator','fire truck','forklift','helicopter','plane','ship','taxi','tractor','train','truck','drill','hammer','saw','screwdriver','wrench'],
  human_body: ['arm','leg','hand','foot','head','eye','ear','nose','mouth','tooth','hair','neck','shoulder','knee','finger','thumb','toe','heart','brain','stomach'],
  musical_instruments: ['drum','guitar','piano','violin','flute','trumpet','saxophone','harp','accordion','xylophone'],
  home: ['bed','chair','table','door','window','cup','plate','spoon','fork','knife','clock','telephone','television','computer','bottle','bowl','carpet','key','soap','towel'],
  school: ['one','two','three','four','five','six','seven','eight','nine','ten','red','blue','green','black','white','circle','square','triangle','book','pencil'],
  professions: ['doctor','teacher','nurse','farmer','driver','chef','pilot','policeman','fireman','artist','student','dentist','waiter','baker','butcher'],
};

const additions = {
  animals: ['Puppy','Kitten','Hamster','Squirrel','Beaver','Otter','Koala','Pony','Raccoon','Badger'],
  buildings: ['Bakery','Museum','Zoo','Aquarium','Bus Station','Apartment Building','Pharmacy','Market','Sports Hall','Kindergarten'],
  clocks: ["It is five past one","It is ten past two","It is twenty past three","It is twenty-five past four","It is five to six","It is ten to seven","It is twenty to eight","It is twenty-five to nine","Noon","Midnight"],
  countries: ['England','Scotland','Wales','Northern Ireland','Palestine','Taiwan','Greenland','Puerto Rico','Hong Kong','Vatican City'],
  space: ['Milky Way','Solar System','Space Suit','Lunar Module','Moon Rock','Space Probe','Dwarf Planet','Asteroid Belt','Launch Pad','Gravity'],
  farm: ['Farmer','Farmhouse','Stable','Field','Haystack','Seed','Soil','Irrigation','Greenhouse','Orchard'],
  flags: ['England','Scotland','Wales','Northern Ireland','Palestine','Taiwan','Greenland','Puerto Rico','Hong Kong','Vatican City'],
  food: ['Rice','Pasta','Yogurt','Butter','Cereal','Pancake','Salad','Hamburger','Chicken','Fish'],
  fruits_vegetables: ['Mango','Pomegranate','Tangerine','Lime','Guava','Radish','Turnip','Sweet Potato','Leek','Okra'],
  heavy_equipment: ['Dump Truck','Road Roller','Skid Steer Loader','Trencher','Pile Driver','Asphalt Paver','Mobile Crane','Mining Truck','Compactor','Scraper'],
  human_body: ['Upper Arm','Forearm','Calf Muscle','Hip','Waist','Temple','Knuckle','Collarbone','Achilles Tendon','Vocal Cords'],
  musical_instruments: ['Ukulele','Tambourine','Triangle','Recorder','Trombone','French Horn','Bass Guitar','Double Bass','Cymbals','Keyboard'],
  home: ['Bed','Bedroom','Kitchen','Living Room','Wardrobe','Pillow','Blanket','Curtain','Kettle','Vacuum Cleaner'],
  school: ['Pen','Pencil','Eraser','Ruler','Notebook','School Bus','Classroom','Homework','Lesson','Break Time'],
  professions: ['Veterinarian','Astronaut','Paramedic','Mechanic','Hairdresser','Librarian','Mail Carrier','Security Guard','Translator','Web Designer'],
};

function normalized(value) { return value.toLowerCase().replace(/[^a-z0-9]+/g, ' ').trim(); }

function difficulty(category, title) {
  const n = normalized(title);
  if (category === 'clocks') {
    if (n.includes('o clock')) return 'beginner';
    if (n.includes('half past')) return 'intermediate';
    return 'advanced';
  }
  if ((beginnerByCategory[category] || []).includes(n)) return 'beginner';
  const words = n.split(' ').filter(Boolean);
  let score = Math.max(0, title.length - 6) + Math.max(0, words.length - 1) * 5;
  if (/\/|-/.test(title)) score += 3;
  if (category === 'countries' || category === 'flags') score += words.length > 1 ? 6 : 0;
  if (score <= 5) return 'beginner';
  if (score <= 13) return 'intermediate';
  return 'advanced';
}

function slug(value) { return normalized(value).replace(/ /g, '_'); }

const groups = {
  animals: {
    pets: ['cat','dog','puppy','kitten','hamster','rabbit','parrot','fish','turtle'],
    farm_animals: ['bull','calf','chick','cow','donkey','duck','goat','goose','hen','horse','lamb','pig','rooster','sheep','turkey','pony'],
    sea_animals: ['crab','dolphin','jellyfish','lobster','octopus','pelican','sea lion','seagull','seahorse','seal','shark','squid','starfish','stingray','walrus','whale'],
    birds: ['bird','eagle','flamingo','hawk','ostrich','owl','peacock','penguin','stork','swan'],
    insects_small_animals: ['ant','bee','butterfly','caterpillar','dragonfly','firefly','grasshopper','honey bee','housefly','ladybug','mosquito','snail','spider','worm'],
    forest_animals: ['badger','beaver','deer','fox','hedgehog','meerkat','mouse','otter','raccoon','squirrel','wolf'],
    reptiles_amphibians: ['chameleon','cobra','crocodile','frog','iguana','lizard','snake'],
  },
  food: {
    drinks: ['ayran','beer','coffee','coke','herbal tea','juice','milk','milkshake','sahlep','soda','tea','water','wine'],
    meals_fast_food: ['fried potatoes','pasta','pizza','rice','salad','sandwich','soup'],
    sweets_snacks: ['cake','candy','chocolate','cookie','honey','ice cream','jam','lollipop','popcorn','turkish delight'],
    dairy_breakfast: ['bread','butter','cereal','cheese','egg','pancake','yogurt'],
    spices_condiments: ['black pepper','cinnamon','cumin','ginger','green olive','olive','pepperoni','pickle','pollen','saffron','salt','sausage','scaly pepper','sugar','turmeric'],
  },
  fruits_vegetables: {
    common_fruits: ['apple','apricot','banana','cherry','grape','grapefruit','kiwi','lemon','lime','mango','melon','orange','peach','pear','pineapple','plum','pomegranate','strawberry','tangerine','watermelon'],
    berries_small_fruits: ['black grape','black mulberry','blackberry','blueberry','cranberry','golden strawberry','mulberry','raspberry','rowanberry'],
    tropical_exotic_fruits: ['avocado','coconut','date','dragon fruit','fig','guava','kaki persimmon','papaya','tamarind'],
    nuts_seeds: ['acorn','almond','cashew nut','hazelnut','peanut','pistachio','tiger nut','walnut'],
    leafy_green_vegetables: ['basil','cabbage','celery','dill','garden cress','garden rocket','lettuce','mint','parsley','scallion','spinach'],
    root_bulb_vegetables: ['beet','carrot','garlic','onion','potato','radish','sweet potato','turnip'],
    other_vegetables: ['artichoke','asparagus','broccoli','brussels sprout','cauliflower','corn','cucumber','eggplant','green bean','leek','mushroom','okra','pea','pepper','pumpkin','tomato','zucchini'],
  },
  human_body: {
    head_face: ['beard','brain','cheek','chin','ear','eye','eyebrow','eyelash','forehead','hair','head','iris','jaw','lip','moustache','mouth','nose','nostrils','pupil','temple','tongue','tooth'],
    arms_hands: ['arm','armpit','elbow','fingernail','forearm','hand','index finger','knuckle','little finger pinkie','middle finger','palm','ring finger','thumb','upper arm','wrist'],
    legs_feet: ['achilles tendon','ankle','big toe','calf muscle','foot','heel','hip','knee','leg','toes'],
    torso: ['abdomen','bellybutton','chest','collarbone','navel','neck','rib cage','shoulder','spine backbone','waist'],
    organs: ['heart','kidney','large intestine','liver','lungs','pancreas','small intestine','stomach','windpipe trachea','esophagus','vocal cords'],
    body_systems: ['arteries','bones','digestive system','human body','muscular system','nervous system','skeletal system','skeleton','skull','veins'],
  },
  musical_instruments: {
    string_instruments: ['baglama','bass guitar','cello','double bass','guitar','harp','kemancha','mandolin','qanun','ukulele','violin'],
    wind_instruments: ['clarinet','flute','french horn','harmonica','oboe','recorder','saxophone','trombone','trumpet','whistle','zurna'],
    percussion_instruments: ['conga','cymbals','drum','maracas','tambourine','triangle','xylophone'],
    keyboard_instruments: ['accordion','keyboard','organ','piano'],
  },
  professions: {
    health_science: ['dentist','doctor','nurse','paramedic','pharmacist','scientist','veterinarian'],
    education_law_public: ['judge','lawyer','librarian','politician','policeman','security guard','soldier','student','teacher','translator'],
    engineering_technology: ['architect','civil engineer','computer engineer','electronic engineer','engineer','web designer'],
    arts_entertainment_sports: ['artist','basketballer','clown','footballer','magician','musician','painter','photographer'],
    services_trades: ['baker','businessman','butcher','cashier','chef','construction worker','delivery man','driver','electrician','gardener','hairdresser','mail carrier','mechanic','plumber','postman','receptionist','repairman','taxi driver','tour guide','waiter'],
    nature_transport: ['astronaut','farmer','fireman','pilot','shepherd'],
  },
};

const continentSets = {
  europe: `Albania Andorra Austria Belarus Belgium Bosnia and Herzegovina Bulgaria Croatia Cyprus Czech Republic Denmark Estonia Finland France Germany Greece Hungary Iceland Ireland Italy Kosovo Latvia Liechtenstein Lithuania Luxembourg Macedonia Malta Moldova Monaco Montenegro Netherlands Norway Poland Portugal Romania Russia San Marino Serbia Slovakia Slovenia Spain Sweden Switzerland Turkey Ukraine United Kingdom Vatican City England Scotland Wales Northern Ireland`.split('|'),
  asia: `Afghanistan Armenia Azerbaijan Bahrain Bangladesh Bhutan Brunei Cambodia China Georgia India Indonesia Iran Iraq Israel Japan Jordan Kazakhstan Kuwait Kyrgyzstan Laos Lebanon Malaysia Maldives Mongolia Myanmar Nepal North Korea Oman Pakistan Palestine Philippines Qatar Saudi Arabia Singapore South Korea Sri Lanka Syria Taiwan Tajikistan Thailand Timor-Leste Turkmenistan United Arab Emirates Uzbekistan Vietnam Yemen Hong Kong`.split('|'),
};
// Pipe-delimited assignments are used below because many country names contain spaces.
continentSets.europe = `Albania|Andorra|Austria|Belarus|Belgium|Bosnia and Herzegovina|Bulgaria|Croatia|Cyprus|Czech Republic|Denmark|Estonia|Finland|France|Germany|Greece|Hungary|Iceland|Ireland|Italy|Kosovo|Latvia|Liechtenstein|Lithuania|Luxembourg|Macedonia|Malta|Moldova|Monaco|Montenegro|Netherlands|Norway|Poland|Portugal|Romania|Russia|San Marino|Serbia|Slovakia|Slovenia|Spain|Sweden|Switzerland|Turkey|Ukraine|United Kingdom|Vatican City|England|Scotland|Wales|Northern Ireland`.split('|');
continentSets.asia = `Afghanistan|Armenia|Azerbaijan|Bahrain|Bangladesh|Bhutan|Brunei|Cambodia|China|Georgia|India|Indonesia|Iran|Iraq|Israel|Japan|Jordan|Kazakhstan|Kuwait|Kyrgyzstan|Laos|Lebanon|Malaysia|Maldives|Mongolia|Myanmar|Nepal|North Korea|Oman|Pakistan|Palestine|Philippines|Qatar|Saudi Arabia|Singapore|South Korea|Sri Lanka|Syria|Taiwan|Tajikistan|Thailand|Timor-Leste|Turkmenistan|United Arab Emirates|Uzbekistan|Vietnam|Yemen|Hong Kong`.split('|');
continentSets.africa = `Algeria|Angola|Benin|Botswana|Burkina Faso|Burundi|Cabo Verde|Cameroon|Central African Republic|Chad|Comoros|Democratic Republic of the Congo|Republic of the Congo|Cote d'Ivoire|Djibouti|Egypt|Equatorial Guinea|Eritrea|Eswatini|Ethiopia|Gabon|Gambia|Ghana|Guinea|Guinea-Bissau|Kenya|Lesotho|Liberia|Libya|Madagascar|Malawi|Mali|Mauritania|Mauritius|Morocco|Mozambique|Namibia|Niger|Nigeria|Rwanda|Sao Tome and Principe|Senegal|Seychelles|Sierra Leone|Somalia|South Africa|South Sudan|Sudan|Tanzania|Togo|Tunisia|Uganda|Zambia|Zimbabwe`.split('|');
continentSets.north_america = `Antigua and Barbuda|Bahamas|Barbados|Belize|Canada|Costa Rica|Cuba|Dominica|Dominican Republic|El Salvador|Grenada|Guatemala|Haiti|Honduras|Jamaica|Mexico|Nicaragua|Panama|Saint Kitts and Nevis|Saint Lucia|Saint Vincent and the Grenadines|Trinidad and Tobago|United States of America|Puerto Rico|Greenland`.split('|');
continentSets.south_america = `Argentina|Bolivia|Brazil|Chile|Colombia|Ecuador|Guyana|Paraguay|Peru|Suriname|Uruguay|Venezuela`.split('|');
continentSets.oceania = `Australia|Fiji|Kiribati|Marshall Islands|Micronesia|Nauru|New Zealand|Palau|Papua New Guinea|Samoa|Solomon Islands|Tonga|Tuvalu|Vanuatu`.split('|');

function subcategory(category, title) {
  const n = normalized(title);
  if (category === 'countries' || category === 'flags') {
    for (const [id, names] of Object.entries(continentSets)) if (names.some(x => normalized(x) === n)) return id;
    return 'international_other';
  }
  if (groups[category]) {
    for (const [id, names] of Object.entries(groups[category])) if (names.map(normalized).includes(n)) return id;
    if (category === 'animals') return 'wild_animals';
    if (category === 'food') return 'ingredients_basics';
    if (category === 'fruits_vegetables') return 'wild_orchard_fruits';
  }
  if (category === 'clocks') {
    if (n.includes('o clock')) return 'full_hours'; if (n.includes('half past')) return 'half_hours';
    if (n.includes('quarter past')) return 'quarter_past'; if (n.includes('quarter to')) return 'quarter_to';
    if (n.includes('past')) return 'minutes_past'; if (n.includes('to')) return 'minutes_to'; return 'special_times';
  }
  if (category === 'space') {
    if (/astronaut|apollo|helmet|oxygen|space suit/.test(n)) return 'astronauts_equipment';
    if (/rocket|shuttle|station|satellite|capsule|vehicle|probe|module|launch pad|solar panel/.test(n)) return 'spacecraft_technology';
    if (/constellation|dipper|north star|ursa|scorpion|sagittarius|cygnus|lyra|aquila/.test(n)) return 'constellations';
    if (/telescope|observatory|binoculars/.test(n)) return 'observation'; return 'celestial_objects_space_science';
  }
  if (category === 'farm') {
    if (/cow|bull|calf|chick|hen|rooster|horse|donkey|goat|sheep|lamb|pig|turkey|duck|goose|cat|dog|ox/.test(n)) return 'farm_animals';
    if (/barn|coop|farmhouse|stable|well|mill|roof|floor|ceiling|stairs|fence|fountain|trough/.test(n)) return 'farm_buildings_areas';
    if (/tractor|harvester|trailer|carriage|plow|rake|shovel|fork|scythe|sickle|pickaxe|wheelbarrow|crane/.test(n)) return 'farm_tools_machines';
    if (/barley|bean|chickpea|clover|corn|rye|wheat|pea|seed|fruit|vegetable|milk/.test(n)) return 'crops_products'; return 'plants_nature';
  }
  if (category === 'buildings') {
    if (/tower|wall|mahal|sophia|pyramid|colossus|temple|gardens|zeus|artemis|halicarnassus|bridge/.test(n)) return 'landmarks_bridges';
    if (/school|library|bookshop|museum|kindergarten/.test(n)) return 'education_culture';
    if (/hospital|pharmacy|fire station|police station|post office|bank|office|workplace/.test(n)) return 'public_services_work';
    if (/restaurant|cafe|hotel|market|cinema|theatre|funfair|gym|stadium|court|pool|playground|zoo|aquarium|sports hall/.test(n)) return 'leisure_commercial';
    if (/church|mosque|synagogue/.test(n)) return 'religious_buildings';
    if (/forest|jungle|mountain|volcano|waterfall|beach|desert|cave|cliff|park/.test(n)) return 'natural_places';
    if (/airport|station|car park|street|pavement|traffic|petrol/.test(n)) return 'transport_city'; return 'homes_structures';
  }
  if (category === 'heavy_equipment') {
    if (/wrench|caliper|chisel|clamp|knife|cutter|drill|pliers|reel|stick|micrometer|nail|nut|rasp|sandpaper|saw|scales|screw|spring|tape measure|thermometer|toolbox|washer/.test(n)) return 'tools_measurement';
    if (/hard hat|gloves|goggles|clothes/.test(n)) return 'safety_equipment';
    if (/plane|helicopter|drone|balloon|parachute|paramotor/.test(n)) return 'air_vehicles';
    if (/boat|ship/.test(n)) return 'water_vehicles';
    if (/automobile|taxi|train|trailer|truck|tractor/.test(n)) return 'transport_vehicles'; return 'construction_machines';
  }
  if (category === 'home') {
    if (/shirt|shoe|sock|dress|coat|jacket|pants|shorts|skirt|sweater|suit|tie|hat|glove|boot|slipper|sandal|pajama|gown|vest|belt|scarf|blouse|tuxedo|boxer|brief|earmuff|mitten|flip flop|bathrobe/.test(n)) return 'clothes';
    if (/ring|diamond|ruby|emerald|bracelet|necklace|earring|jewelry|buckle|bow tie|purse|strap/.test(n)) return 'accessories_jewelry';
    if (/doll|ball|block|chess|dice|domino|console|pinball|plush|robot|horse|scooter|rope|top|toy|trampoline|tricycle|yoyo|bike|bicycle/.test(n)) return 'toys_games';
    if (/fork|knife|spoon|plate|cup|glass|bowl|pan|pot|cooker|oven|fridge|blender|grater|kettle|dishwasher|microwave|breadboard/.test(n)) return 'kitchen';
    if (/bath|shower|toilet|tooth|towel|soap|washcloth|tweezer|comb|brush|basin|sink|nail/.test(n)) return 'bathroom_personal_care';
    if (/bed|pillow|blanket|wardrobe|bedroom|night/.test(n)) return 'bedroom';
    if (/chair|table|armchair|carpet|television|lamp|sofa|living room/.test(n)) return 'living_room';
    if (/book|blackboard|calculator|chalk|clipboard|desk|computer|bulletin/.test(n)) return 'study_area'; return 'household_items';
  }
  if (category === 'school') {
    if (/^(zero|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety|hundred|thousand)$/.test(n)) return 'cardinal_numbers';
    if (/first|second|third|fourth|fifth|sixth|seventh|eighth|ninth|tenth|eleventh|twelfth|thirteenth|fourteenth|fifteenth|sixteenth|seventeenth|eighteenth|nineteenth|twentieth|zeroth/.test(n)) return 'ordinal_numbers';
    if (/plus|minus|times|equals|divide/.test(n)) return 'mathematics';
    if (/line|point|circle|square|triangle|rectangle|pentagon|hexagon|octagon|oval|cube|pyramid|sphere|diamond|cone|cylinder/.test(n)) return 'shapes_geometry';
    if (/comma|colon|semicolon|mark|quotation|parenthesis|dash/.test(n)) return 'punctuation';
    if (/red|blue|green|black|white|purple|brown|orange|yellow|gray|pink|maroon|turquoise/.test(n)) return 'colors';
    if (/^[a-z]$/.test(n)) return 'alphabet'; return 'classroom_learning';
  }
  return 'general';
}

function titleForSubcategory(id) {
  return id.split('_').map(x => x.charAt(0).toUpperCase() + x.slice(1)).join(' ');
}

function makeAddedRecord(category, title, sourceIndex) {
  const s = slug(title);
  return {
    OriginalId: null, Title: title, Category: category,
    Image: `slide_${category}_${s}.jpg`, WordSound: `slide_${category}_${s}_en.mp3`,
    ObjectSound: null, ExcludeItems: '', CreatedDate: null, OriginalRow: null,
    IsAddedInV2: true, AssetStatus: 'image_and_audio_required', AddedSourceIndex: sourceIndex,
  };
}

function buildCategory(id, title, filename, includeAdditions) {
  const raw = JSON.parse(fs.readFileSync(path.join(sourceDir, filename), 'utf8')).Slide;
  const records = raw.map(item => ({
    OriginalId: item.Id,
    Title: corrections[item.Title] || item.Title.trim(),
    OriginalTitle: corrections[item.Title] ? item.Title : undefined,
    Category: id,
    Image: item.Image,
    WordSound: item.WordSound,
    ObjectSound: item.ObjectSound || null,
    ExcludeItems: item.ExcludeItems || '',
    CreatedDate: item.CreatedDate || null,
    OriginalRow: item.Row ?? null,
    IsAddedInV2: false,
    AssetStatus: 'existing',
  }));
  let added = [];
  if (includeAdditions) {
    const needed = (10 - (records.length % 10)) % 10 || 10;
    const existing = new Set(records.map(x => normalized(x.Title)));
    added = additions[id].filter(x => !existing.has(normalized(x))).slice(0, needed)
      .map((x, i) => makeAddedRecord(id, x, i + 1));
  }
  const all = [...records, ...added].map((item, i) => ({...item, SortId: i + 1, Difficulty: difficulty(id, item.Title), Subcategory: subcategory(id, item.Title)}));
  const difficultyRank = {beginner: 0, intermediate: 1, advanced: 2};
  const ordered = [...all].sort((a,b) =>
    difficultyRank[a.Difficulty] - difficultyRank[b.Difficulty] ||
    (a.IsAddedInV2 - b.IsAddedInV2) || a.Title.localeCompare(b.Title));
  const categoryLessons = [];
  for (let i = 0; i < ordered.length; i += 10) {
    const words = ordered.slice(i, i + 10);
    const lessonDifficulty = words.reduce((max, word) =>
      difficultyRank[word.Difficulty] > difficultyRank[max] ? word.Difficulty : max, 'beginner');
    categoryLessons.push({ Lesson: i / 10 + 1, Difficulty: lessonDifficulty, WordCount: words.length, Words: words });
  }
  const levels = ['beginner', 'intermediate', 'advanced'].map(level => {
    const levelWords = all.filter(x => x.Difficulty === level)
      .sort((a,b) => (a.IsAddedInV2 - b.IsAddedInV2) || a.Title.localeCompare(b.Title));
    const lessons = [];
    for (let i = 0; i < levelWords.length; i += 10) {
      lessons.push({ Lesson: i / 10 + 1, WordCount: Math.min(10, levelWords.length - i), Words: levelWords.slice(i, i + 10) });
    }
    return { Level: level, WordCount: levelWords.length, Lessons: lessons };
  });
  const subcategories = [...new Set(all.map(x => x.Subcategory))].sort().map(subId => {
    const subWords = ordered.filter(x => x.Subcategory === subId);
    const lessons = [];
    for (let i = 0; i < subWords.length; i += 10) lessons.push({Lesson: i / 10 + 1, WordCount: Math.min(10, subWords.length - i), Words: subWords.slice(i, i + 10)});
    return {Id: subId, Title: titleForSubcategory(subId), WordCount: subWords.length, Lessons: lessons};
  });
  return { Id: id, Title: title, SourceFile: filename, OriginalWordCount: records.length, AddedWordCount: added.length, TotalWordCount: all.length, Lessons: categoryLessons, Subcategories: subcategories, DifficultyLevels: levels };
}

function build(version, includeAdditions) {
  const categories = sources.map(args => buildCategory(...args, includeAdditions));
  let globalId = 1;
  for (const category of categories) {
    const ids = new Map();
    for (const lesson of category.Lessons) lesson.Words = lesson.Words.map((word, index) => {
      const assigned = {...word, Id: globalId++, LessonPosition: index + 1};
      ids.set(`${word.OriginalId ?? 'added'}:${word.Title}`, assigned.Id);
      return assigned;
    });
    for (const level of category.DifficultyLevels) for (const lesson of level.Lessons) {
      lesson.Words = lesson.Words.map((word, index) => ({...word, Id: ids.get(`${word.OriginalId ?? 'added'}:${word.Title}`), LevelPosition: index + 1}));
    }
  }
  return {
    SchemaVersion: '2.0', DatabaseVersion: version, Language: 'en', LessonSize: 10,
    DifficultyOrder: ['beginner','intermediate','advanced'],
    Notes: includeAdditions
      ? 'Includes original records plus curated additions. Added records require image and audio assets.'
      : 'Contains only original records. Display-title spelling corrections do not rename existing asset files.',
    CategoryCount: categories.length,
    OriginalWordCount: categories.reduce((n,c) => n + c.OriginalWordCount, 0),
    AddedWordCount: categories.reduce((n,c) => n + c.AddedWordCount, 0),
    TotalWordCount: categories.reduce((n,c) => n + c.TotalWordCount, 0),
    Categories: categories,
  };
}

fs.mkdirSync(outputDir, {recursive: true});
const v1 = build('1.0-original-reorganized', false);
const v2 = build('2.0-curated-expanded', true);
fs.writeFileSync(path.join(outputDir, 'kidswordplus_unified_en_v1_subcategorized.json'), JSON.stringify(v1, null, 2) + '\n');
fs.writeFileSync(path.join(outputDir, 'kidswordplus_unified_en_v2_subcategorized.json'), JSON.stringify(v2, null, 2) + '\n');

const lines = [
  '# KidsWordPlus unified English database report', '',
  `- V1: ${v1.TotalWordCount} original words`,
  `- V2: ${v2.TotalWordCount} words (${v2.AddedWordCount} curated additions)`,
  '- Lesson size: maximum 10 words; V1 may have a shorter final lesson within a difficulty level.',
  '- V2 additions are marked with `IsAddedInV2: true` and `AssetStatus: image_and_audio_required`.', '',
  '| Category | V1 | Added | V2 |', '|---|---:|---:|---:|',
  ...v2.Categories.map(c => `| ${c.Title} | ${c.OriginalWordCount} | ${c.AddedWordCount} | ${c.TotalWordCount} |`),
  '', '## Corrected display titles', '',
  ...Object.entries(corrections).map(([a,b]) => `- ${a} → ${b}`),
];
lines.push('', '## Subcategories', '', ...v2.Categories.map(c => `- **${c.Title}:** ${c.Subcategories.map(s => `${s.Title} (${s.WordCount})`).join(', ')}`));
fs.writeFileSync(path.join(outputDir, 'README-subcategories.md'), lines.join('\n') + '\n');
console.log(JSON.stringify({v1:v1.TotalWordCount,v2:v2.TotalWordCount,added:v2.AddedWordCount,categories:v2.CategoryCount}, null, 2));
