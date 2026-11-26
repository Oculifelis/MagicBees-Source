package com.magicbees;

import com.accbdd.complicated_bees.bees.BeeHousingModifier;
import com.accbdd.complicated_bees.item.FrameItem;
import com.magicbees.item.BeeRingItem;
import com.magicbees.item.MagicScoopItem;
import com.magicbees.item.MysteriousMagnetItem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Magic Bees' resources and frames. Combs are Complicated Bees comb types (data), not items. */
public final class MBItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MagicBees.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MagicBees.MODID);

    private static final List<Supplier<? extends Item>> TAB_ITEMS = new ArrayList<>();
    public static final String[] PRIMALS = {"air", "fire", "water", "earth", "order", "chaos"};
    private static final TagKey<Item> REPAIRS_THAUMIUM = TagKey.create(Registries.ITEM, MagicBees.id("repairs_thaumium_tools"));
    private static final TagKey<Item> REPAIRS_VOID = TagKey.create(Registries.ITEM, MagicBees.id("repairs_void_tools"));

    // waxes, drops, pollen, propolis (Magic Bees' tinted Forestry-style resources)
    public static final DeferredItem<Item> MAGIC_WAX = simple("magic_wax");
    public static final DeferredItem<Item> SOUL_WAX = simple("soul_wax");
    public static final DeferredItem<Item> AMNESIC_WAX = simple("amnesic_wax");
    public static final DeferredItem<Item> ENCHANTING_DROP = simple("enchanting_drop");
    public static final DeferredItem<Item> INTELLECT_DROP = simple("intellect_drop");
    public static final DeferredItem<Item> UNUSUAL_POLLEN = simple("unusual_pollen");
    public static final DeferredItem<Item> PHASED_POLLEN = simple("phased_pollen");
    public static final DeferredItem<Item> UNSTABLE_PROPOLIS = simple("unstable_propolis");

    // miscellaneous resources
    public static final DeferredItem<Item> SKULL_CHIP = simple("skull_chip");
    public static final DeferredItem<Item> SKULL_FRAGMENT = simple("skull_fragment");
    public static final DeferredItem<Item> DRAGON_DUST = simple("dragon_dust");
    public static final DeferredItem<Item> DRAGON_CHUNK = simple("dragon_chunk");
    public static final DeferredItem<Item> AROMATIC_LUMP = simple("aromatic_lump");
    public static final DeferredItem<Item> EXTENDED_FERTILIZER = simple("extended_fertilizer");
    public static final DeferredItem<Item> ESSENCE_FALSE_LIFE = rare("essence_false_life");
    public static final DeferredItem<Item> ESSENCE_SHALLOW_GRAVE = rare("essence_shallow_grave");
    public static final DeferredItem<Item> ESSENCE_LOST_TIME = rare("essence_lost_time");
    public static final DeferredItem<Item> ESSENCE_EVERLASTING_DURABILITY = rare("essence_everlasting_durability");
    public static final DeferredItem<Item> ESSENCE_SCORNFUL_OBLIVION = rare("essence_scornful_oblivion");
    public static final DeferredItem<Item> ESSENCE_FICKLE_PERMANENCE = rare("essence_fickle_permanence");
    public static final DeferredItem<Item> DIMENSIONAL_SINGULARITY = rare("dimensional_singularity");
    public static final DeferredItem<Item> LORE_FRAGMENT = simple("lore_fragment");
    public static final DeferredItem<Item> DIAMOND_SHARD = simple("diamond_shard");
    public static final DeferredItem<Item> EMERALD_SHARD = simple("emerald_shard");

    // tools
    public static final DeferredItem<MysteriousMagnetItem> MYSTERIOUS_MAGNET = item("mysterious_magnet",
            p -> new MysteriousMagnetItem(p.stacksTo(1).component(MBComponents.MAGNET_LEVEL.get(), 0)));
    public static final DeferredItem<Item> MOON_DIAL = item("moon_dial", p -> new Item(p.stacksTo(1)));
    public static final DeferredItem<BeeRingItem> BEE_RING = item("bee_ring", p -> new BeeRingItem(p.stacksTo(1)));

    // Thaumaturge module: primal propolis from the glittering combs, centrifuged into primal dusts (recipes are gated)
    public static final List<DeferredItem<Item>> PRIMAL_PROPOLIS = primals("propolis");
    public static final List<DeferredItem<Item>> PRIMAL_DUSTS = primals("dust");
    public static final DeferredItem<MagicScoopItem> THAUMIUM_SCOOP = item("thaumium_scoop",
            p -> new MagicScoopItem(p.durability(30), false, 14, REPAIRS_THAUMIUM));
    public static final DeferredItem<MagicScoopItem> VOID_SCOOP = item("void_scoop",
            p -> new MagicScoopItem(p.durability(30), true, 10, REPAIRS_VOID));

    public static final DeferredItem<Item> JELLY_BABIES = item("jelly_babies", p -> new Item(p.food(
            new FoodProperties.Builder().nutrition(1).saturationModifier(0.1f).alwaysEdible().build())));

    // frames: Magic Bees' additive production bonus x becomes a 1 + x/2 multiplier to sit beside Complicated Bees' frames
    public static final DeferredItem<FrameItem> MAGIC_FRAME = frame("magic_frame", 240, MBConfig.MAGIC_FRAME,
            new BeeHousingModifier.Builder().productivity(2f).build());
    public static final DeferredItem<FrameItem> RESILIENT_FRAME = frame("resilient_frame", 800, MBConfig.RESILIENT_FRAME,
            new BeeHousingModifier.Builder().productivity(2f).build());
    public static final DeferredItem<FrameItem> GENTLE_FRAME = frame("gentle_frame", 200, MBConfig.GENTLE_FRAME,
            new BeeHousingModifier.Builder().mutation(0.7f).lifespan(1.5f).productivity(1.2f).build());
    public static final DeferredItem<FrameItem> METABOLIC_FRAME = frame("metabolic_frame", 130, MBConfig.METABOLIC_FRAME,
            new BeeHousingModifier.Builder().mutation(1.8f).productivity(1.1f).build());
    public static final DeferredItem<FrameItem> NECROTIC_FRAME = frame("necrotic_frame", 280, MBConfig.NECROTIC_FRAME,
            new BeeHousingModifier.Builder().lifespan(0.3f).productivity(0.875f).build());
    public static final DeferredItem<FrameItem> TEMPORAL_FRAME = frame("temporal_frame", 300, MBConfig.TEMPORAL_FRAME,
            new BeeHousingModifier.Builder().lifespan(2.5f).build());
    public static final DeferredItem<FrameItem> OBLIVION_FRAME = frame("oblivion_frame", 50, MBConfig.OBLIVION_FRAME,
            new BeeHousingModifier.Builder().lifespan(0.0001f).productivity(0f).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.magicbees"))
            .icon(() -> new ItemStack(MAGIC_FRAME.get()))
            .displayItems((params, out) -> TAB_ITEMS.forEach(i -> out.accept(i.get())))
            .build());

    private MBItems() {
    }

    /** For the compat modules' own registers: show their items in the Magic Bees tab. */
    public static void addToTab(Supplier<? extends Item> item) {
        TAB_ITEMS.add(item);
    }

    private static <I extends Item> DeferredItem<I> item(String name, Function<Item.Properties, I> factory) {
        DeferredItem<I> item = ITEMS.registerItem(name, factory);
        TAB_ITEMS.add(item);
        return item;
    }

    private static DeferredItem<Item> simple(String name) {
        return item(name, Item::new);
    }

    private static List<DeferredItem<Item>> primals(String kind) {
        List<DeferredItem<Item>> out = new ArrayList<>();
        for (String primal : PRIMALS) out.add(simple(primal + "_" + kind));
        return List.copyOf(out);
    }

    private static DeferredItem<Item> rare(String name) {
        return item(name, p -> new Item(p.rarity(Rarity.UNCOMMON)));
    }

    private static DeferredItem<FrameItem> frame(String name, int durability, ModConfigSpec.ConfigValue<Boolean> enabled,
                                                 BeeHousingModifier modifier) {
        return item(name, p -> new FrameItem(p.durability(durability), modifier, enabled));
    }

    static void register(IEventBus bus) {
        ITEMS.register(bus);
        TABS.register(bus);
    }
}
