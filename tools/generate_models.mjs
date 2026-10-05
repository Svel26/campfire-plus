import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const assets = path.join(root, 'src/main/resources/assets/campfires');
const vanilla = path.join(root, 'src/main/resources/assets/minecraft');

// Burnt Basic (MIT) log geometry, used for the ember and burnt-out log states.
const emberLogs = JSON.parse(fs.readFileSync(path.join(root, 'tools/templates/ember_campfire.json'), 'utf8')).elements;
const burntLogs = JSON.parse(fs.readFileSync(path.join(root, 'tools/templates/burnt_campfire.json'), 'utf8')).elements;

// Fresh logs copied from the vanilla campfire_off template.
const freshLogs = [
  {
    from: [1, 0, 0], to: [5, 4, 16],
    faces: {
      north: { uv: [0, 4, 4, 8], texture: '#log', cullface: 'north' },
      east: { uv: [0, 1, 16, 5], texture: '#log' },
      south: { uv: [0, 4, 4, 8], texture: '#log', cullface: 'south' },
      west: { uv: [16, 0, 0, 4], texture: '#log' },
      up: { uv: [0, 0, 16, 4], rotation: 90, texture: '#log' },
      down: { uv: [0, 0, 16, 4], rotation: 90, texture: '#log', cullface: 'down' }
    }
  },
  {
    from: [11, 0, 0], to: [15, 4, 16],
    faces: {
      north: { uv: [0, 4, 4, 8], texture: '#log', cullface: 'north' },
      east: { uv: [0, 0, 16, 4], texture: '#log' },
      south: { uv: [0, 4, 4, 8], texture: '#log', cullface: 'south' },
      west: { uv: [16, 1, 0, 5], texture: '#log' },
      up: { uv: [0, 0, 16, 4], rotation: 90, texture: '#log' },
      down: { uv: [0, 0, 16, 4], rotation: 90, texture: '#log', cullface: 'down' }
    }
  },
  {
    from: [0, 3, 1], to: [16, 7, 5],
    faces: {
      north: { uv: [0, 0, 16, 4], texture: '#log' },
      east: { uv: [0, 4, 4, 8], texture: '#log', cullface: 'east' },
      south: { uv: [16, 0, 0, 4], texture: '#log' },
      west: { uv: [0, 4, 4, 8], texture: '#log', cullface: 'west' },
      up: { uv: [0, 0, 16, 4], rotation: 180, texture: '#log' },
      down: { uv: [0, 0, 16, 4], texture: '#log' }
    }
  },
  {
    from: [0, 3, 11], to: [16, 7, 15],
    faces: {
      north: { uv: [16, 0, 0, 4], texture: '#log' },
      east: { uv: [0, 4, 4, 8], texture: '#log', cullface: 'east' },
      south: { uv: [0, 0, 16, 4], texture: '#log' },
      west: { uv: [0, 4, 4, 8], texture: '#log', cullface: 'west' },
      up: { uv: [0, 0, 16, 4], rotation: 180, texture: '#log' },
      down: { uv: [0, 0, 16, 4], texture: '#log' }
    }
  }
];

const bed = (topTexture) => ({
  from: [5, 0, 0], to: [11, 1, 16],
  faces: {
    north: { uv: [0, 15, 6, 16], texture: '#log', cullface: 'north' },
    south: { uv: [10, 15, 16, 16], texture: '#log', cullface: 'south' },
    up: { uv: [0, 8, 16, 14], rotation: 90, texture: topTexture },
    down: { uv: [0, 8, 16, 14], rotation: 90, texture: '#log', cullface: 'down' }
  }
});

const fire = [
  {
    from: [0.8, 1, 8], to: [15.2, 17, 8],
    rotation: { origin: [8, 8, 8], axis: 'y', angle: 45, rescale: true },
    shade: false,
    faces: {
      north: { uv: [0, 0, 16, 16], texture: '#fire' },
      south: { uv: [0, 0, 16, 16], texture: '#fire' }
    }
  },
  {
    from: [8, 1, 0.8], to: [8, 17, 15.2],
    rotation: { origin: [8, 8, 8], axis: 'y', angle: 45, rescale: true },
    shade: false,
    faces: {
      west: { uv: [0, 0, 16, 16], texture: '#fire' },
      east: { uv: [0, 0, 16, 16], texture: '#fire' }
    }
  }
];

function writeJson(file, data) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, JSON.stringify(data, null, 2) + '\n');
}

function logElement(state, index) {
  if (state === 'fresh') return freshLogs[index];
  if (state === 'ember') return emberLogs[index];
  return burntLogs[index];
}

function makeModel(prefix, soul, lit, fresh, ember) {
  const elements = [];
  for (let i = 0; i < 4; i++) {
    let state;
    if (i < fresh) state = 'fresh';
    else if (i < fresh + ember) state = 'ember';
    else state = 'burnt';
    elements.push(logElement(state, i));
  }

  const textures = {
    particle: 'minecraft:block/campfire_log',
    log: 'minecraft:block/campfire_log',
    'ember_log.png': 'campfires:block/ember_log',
    'burnt_log.png': 'campfires:block/burnt_log',
    'burnt_log_top.png': 'campfires:block/burnt_log_top'
  };

  if (lit) {
    textures.bed = soul ? 'minecraft:block/soul_campfire_log_lit' : 'minecraft:block/campfire_log_lit';
    textures.fire = soul ? 'minecraft:block/soul_campfire_fire' : 'minecraft:block/campfire_fire';
    elements.push(bed('#bed'), ...fire);
  } else {
    textures.bed = 'minecraft:block/campfire_log';
    elements.push(bed('#bed'));
  }

  writeJson(path.join(assets, `models/block/${prefix}_${lit ? 'lit' : 'unlit'}_f${fresh}_e${ember}.json`), {
    parent: 'minecraft:block/block',
    render_type: 'minecraft:cutout',
    textures,
    elements
  });
}

function makeModels(prefix, soul) {
  for (let fresh = 0; fresh <= 4; fresh++) {
    for (let ember = 0; ember <= 4 - fresh; ember++) {
      makeModel(prefix, soul, false, fresh, ember);
      if (fresh > 0) {
        makeModel(prefix, soul, true, fresh, ember);
      }
    }
  }
}

function blockstate(prefix) {
  const variants = {};
  const rotations = { north: 180, south: 0, east: 270, west: 90 };
  for (const facing of ['north', 'south', 'east', 'west']) {
    const rot = rotations[facing] ? { y: rotations[facing] } : {};
    for (const lit of [true, false]) {
      for (let fresh = 0; fresh <= 4; fresh++) {
        for (let ember = 0; ember <= 4; ember++) {
          const clamped = Math.min(ember, 4 - fresh);
          const state = lit && fresh > 0 ? 'lit' : 'unlit';
          variants[`facing=${facing},lit=${lit},fresh=${fresh},ember=${ember}`] = {
            model: `campfires:block/${prefix}_${state}_f${fresh}_e${clamped}`,
            ...rot
          };
        }
      }
    }
  }
  return { variants };
}

fs.rmSync(path.join(assets, 'models/block'), { recursive: true, force: true });
fs.rmSync(path.join(assets, 'blockstates'), { recursive: true, force: true });
fs.rmSync(path.join(root, 'src/main/resources/resourcepacks'), { recursive: true, force: true });

makeModels('campfire', false);
makeModels('soul_campfire', true);

writeJson(path.join(vanilla, 'blockstates/campfire.json'), blockstate('campfire'));
writeJson(path.join(vanilla, 'blockstates/soul_campfire.json'), blockstate('soul_campfire'));

console.log('Generated campfire models and blockstates.');
