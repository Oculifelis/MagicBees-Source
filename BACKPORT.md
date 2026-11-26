# NeoForge 1.21.1 build

This folder is a separate Gradle project that builds Magic Bees for Minecraft 1.21.1 / NeoForge 21.1.
The 26.1.2 build in the repo root is untouched.

## How it is put together

- Java and resources come from `src/main`. Gradle copies them into `build/shared/` before compiling.
- A file under `src/main/java` or `src/main/resources` here replaces the shared file at the same path.
  Only files whose vanilla or library APIs differ are kept here; everything else is shared.
- While copying, recipe JSON under `data/*/recipe/` is turned into the 1.21.1 ingredient format
  (`"minecraft:stick"` becomes `{"item": ...}`, `"#c:ingots/iron"` becomes `{"tag": ...}`,
  `neoforge:ingredient_type` becomes `type`). Complicated Bees mutation recipes are left alone.
- `assets/magicbees/items/**` (26.x client item definitions) and `pack.mcmeta` are not copied;
  1.21.1 reads `models/item` directly and has its own `pack.mcmeta` here (pack format 48).
- Dependency jars go in `libs/` (not committed); `libs/README.md` lists them.

Build: `.\gradlew.bat build` in this folder. Jar: `build/libs/magicbees-1.21.1-<version>.jar`.

## API differences handled by overrides

| 26.1.2 | 1.21.1 |
|---|---|
| `Identifier` | `ResourceLocation` |
| `ValueInput` / `ValueOutput` block entity saves | `CompoundTag` + `HolderLookup.Provider`, `ItemStack.save` / `parseOptional` |
| `getIntOr` and friends | `getInt` etc. with `contains` checks where the default isn't zero |
| `lookupOrThrow`, `getValue`, `listElementIds` | `registryOrThrow`, `get`, `keySet` |
| `BlockEntity#preRemoveSideEffects` | `Block#onRemove` in `DeviceBlock` calls `EffectJarBlockEntity#dropContents` |
| `useItemOn` returning `InteractionResult.TRY_WITH_EMPTY_HAND` | `ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION` |
| `Item#use` returning `InteractionResult` | `InteractionResultHolder<ItemStack>` |
| `inventoryTick(stack, ServerLevel, entity, EquipmentSlot)` | `inventoryTick(stack, Level, entity, int, boolean)` with a server check |
| `appendHoverText(..., TooltipDisplay, Consumer<Component>, ...)` | `appendHoverText(..., List<Component>, ...)` |
| `new BlockEntityType<>(factory, block)` | `BlockEntityType.Builder.of(factory, block).build(null)` |
| `registerBlock(name, fn, props -> ...)` | `registerBlock(name, fn, Properties)` |
| `EntityType#create(level, reason)`, `snapTo` | `create(level)`, `moveTo` |
| `sendOverlayMessage` | `displayClientMessage(msg, true)` |
| `net.minecraft.util.TriState` | `net.neoforged.neoforge.common.util.TriState` |
| `getMaxY`, `getOverworldClockTime` | `getMaxBuildHeight`, `getDayTime` |
| `MobEffects.SLOWNESS / SPEED / HASTE / NAUSEA` | `MOVEMENT_SLOWDOWN / MOVEMENT_SPEED / DIG_SPEED / CONFUSION` |
| JEI 2x `IRecipeType` | JEI 19 `RecipeType` |
| Thaumaturge node `kind / trait / capacity / reclassify / assignTrait / invalidateRefill` | `getNodeType / getNodeModifier / getAspectsBase / setNodeType / setNodeModifier / nodeChange` |
| `Unit.STREAM_CODEC` | `StreamCodec.unit(Unit.INSTANCE)` |
| `Item.Properties#enchantable / #repairable` (scoops) | `MagicScoopItem#getEnchantmentValue` / `#isValidRepairItem` |

## Data differences handled by overrides

- Copper bee: `minecraft:copper_nugget` doesn't exist in 1.21.1, so its specialty is raw copper at 0.025
  (about the same copper per cycle as 0.2 nuggets).
- Curios slots: Curios 9.5 has no `#curios:player_like` entity tag; the ring and necklace slots go to `minecraft:player`.

## Not in the 1.21.1 build

- Ars Magica Legacy bees: Ars Magica Legacy has no 1.21.1 release. Their data is conditioned on the mod
  and stays inactive.
