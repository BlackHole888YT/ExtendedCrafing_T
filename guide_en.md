# Extended Crafting Table Recipe Guide (1.21.1) by BlackHole888YT

For bug reports, contact:<br>
📞 Discord: **blackhole888yt** (Calls are available for Russian speakers only.)

---

This guide describes the complete JSON structure for the custom recipe type `extended_crafting_t:extended_shaped`.

These recipes support advanced RPG-style conditions, including time of day, dimensions, player health requirements, experience consumption, tool durability usage, and custom enchanted books.

---

# 1. Recipe Structure (Root Level)

Recipe files must be placed in:

📂 `[DATAPACK_NAME]/data/extended_crafting_t/recipe/[recipe_name].json`

### Full Example

```json
{
  "type": "extended_crafting_t:extended_shaped",
  "category": "misc",
  "key": { ... },
  "additional_conditions": { ... },
  "pattern": [ ... ],
  "result": { ... }
}
```

## Root Parameters

| Parameter | Type | Required | Description |
|-----------|------|:--------:|-------------|
| `type` | String | ✅ | Must always be `"extended_crafting_t:extended_shaped"`. |
| `category` | String | ✅ | Vanilla recipe category (`"misc"`, `"equipment"`, `"combat"`, etc.). |
| `pattern` | List of Strings | ✅ | Standard 3×3 crafting pattern. |
| `key` | Object | ✅ | Maps characters from `pattern` to ingredient definitions. |
| `additional_conditions` | Object | ❌ | Environmental and player requirements. If omitted, the recipe is always available. |
| `result` | Object | ✅ | The resulting item. |

---

# 2. Ingredient Block (`key`)

Each character used in `pattern` has its own ingredient definition.

## Available Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|:--------:|:-------:|-------------|
| `ingredient` | Object | ✅ | — | Standard vanilla ingredient. Supports either `"item"` or `"tag"`. |
| `count` | Integer | ❌ | `1` | Minimum number of items required in the slot and consumed during crafting. |
| `consumable` | Boolean | ❌ | `true` | If `false`, the item remains in the crafting grid after crafting (useful for tools). |
| `deal_damage` | Integer | ❌ | `0` | Durability damage dealt to the item. Must not exceed the item's maximum durability, otherwise the recipe will fail. Works only when `consumable` is `false`. |
| `work_enchantments` | Boolean | ❌ | `false` | If enabled, vanilla **Unbreaking** will reduce durability loss. |
| `book_id` | String | ❌ | — | Used **only** for enchanted books. Requires the exact enchantment ID (e.g. `"minecraft:sharpness"`). |
| `book_lvl` | Integer | ❌ | — | Used together with `book_id`. The enchantment level must match exactly. Additional enchantments will invalidate the recipe. |
| `item_after_consume` | Object | ❌ | — | Item that replaces the ingredient if it breaks or is fully consumed. Useful for returning empty buckets or broken tool fragments. |

### Example

```json
"S": {
  "ingredient": {
    "item": "minecraft:iron_sword"
  },
  "consumable": false,
  "deal_damage": 150,
  "work_enchantments": true,
  "item_after_consume": {
    "id": "minecraft:enchanted_book",
    "count": 1,
    "components": {
      "minecraft:custom_data": {
        "book_id": "minecraft:unbreaking",
        "book_lvl": "2"
      }
    }
  }
}
```

---

# 3. Additional Conditions (`additional_conditions`)

Every field inside this object is **optional**.

If a field is omitted, that condition is ignored.

## Available Conditions

| Parameter | Type | Description |
|-----------|------|-------------|
| `required_dimension` | String | The dimension where the crafting table must be placed (`"overworld"`, `"the_nether"`, `"the_end"`). |
| `required_advancement` | String | Full advancement ID the player must have completed (e.g. `"minecraft:story/mine_diamond"`). |
| `required_hp` | String | Player health requirement. Supports:<br>• `"max"` (full health)<br>• `"min"` (≤1 HP / half a heart)<br>• `"half"` (approximately half health ±1.5 HP)<br>• Percentages (`"50%"` = at least half health)<br>• Numeric values (`"15.0"` = at least 15 HP). |
| `xpCost` | Object | Experience **points** requirement.<br>• `value` (Integer): required XP points.<br>• `consumable` (Boolean): whether XP points are consumed. |
| `xpLvl` | Object | Experience **levels** requirement.<br>• `value` (Integer): required levels.<br>• `consumable` (Boolean): whether levels are consumed. |
| `required_time` | Object | Time-of-day requirement.<br>• `value`: preset (`"sunrise"`, `"day"`, `"noon"`, `"sunset"`, `"night"`, `"midnight"`) **or** an exact tick value (`"12500"`).<br>• `interval`: allowed tolerance in ticks.<br>• `interval_value`: always `"tick"`. |

### Example

```json
"additional_conditions": {
  "required_dimension": "overworld",
  "required_advancement": "minecraft:story/mine_diamond",
  "required_hp": "max",
  "xpCost": {
    "value": 150,
    "consumable": true
  },
  "xpLvl": {
    "value": 15,
    "consumable": false
  },
  "required_time": {
    "value": "12500",
    "interval": 500,
    "interval_value": "tick"
  }
}
```

---

# 4. Result Block (`result`)

Defines the crafted item.

Fully supports Minecraft 1.21.1 Data Components.

## Parameters

| Parameter | Type | Required | Description |
|-----------|------|:--------:|-------------|
| `id` | String | ✅ | Item ID (e.g. `"minecraft:enchanted_book"`). |
| `count` | Integer | ❌ | Number of items produced (default is `1`). |
| `components` | Object | ❌ | Standard 1.21.1 Data Components. Used primarily for creating custom enchanted books through `"minecraft:custom_data"`. |
| `book_id` | String | ❌ | Enchantment ID for the resulting enchanted book. |
| `book_lvl` | String | ❌ | Enchantment level for the resulting book. It is recommended not to exceed vanilla maximum levels, otherwise anvils may downgrade enchantments (e.g. **Sharpness VII** becomes **Sharpness V**). |

### Example (Mending I Enchanted Book)

```json
"result": {
  "count": 1, // You can even produce multiple books.
  "id": "minecraft:enchanted_book",
  "components": {
    "minecraft:custom_data": {
      "book_id": "minecraft:mending",
      "book_lvl": "1" // Any level can be specified.
    }
  }
}
```