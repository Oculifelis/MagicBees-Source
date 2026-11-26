package com.magicbees;

import com.accbdd.complicated_bees.bees.effect.IBeeEffect;
import com.accbdd.complicated_bees.bees.effect.PotionEffect;
import com.accbdd.complicated_bees.registry.BeeEffectRegistration;
import com.magicbees.effect.CleansingEffect;
import com.magicbees.effect.CrumblingEffect;
import com.magicbees.effect.GlacialEffect;
import com.magicbees.effect.LightningEffect;
import com.magicbees.effect.RepulsionEffect;
import com.magicbees.effect.SpawnMobEffect;
import com.magicbees.effect.TransmutingEffect;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Bee effects go into Complicated Bees' code registry. Effects Complicated Bees already has (flaming, aggressive,
 * hostile, champion) are reused by id from the species JSON; these are the ones Magic Bees and Forestry added.
 */
public final class MBEffects {
    public static final DeferredRegister<IBeeEffect> EFFECTS = DeferredRegister.create(BeeEffectRegistration.BEE_REGISTRY_KEY, MagicBees.MODID);

    // Magic Bees' potion alleles (duration in seconds as in AlleleEffectPotion, applied every 80 ticks)
    public static final Supplier<IBeeEffect> SLOWNESS = potion("slowness", MobEffects.MOVEMENT_SLOWDOWN, 3, 0);
    public static final Supplier<IBeeEffect> SWIFTNESS = potion("swiftness", MobEffects.MOVEMENT_SPEED, 10, 0);
    public static final Supplier<IBeeEffect> HASTE = potion("haste", MobEffects.DIG_SPEED, 15, 0);
    public static final Supplier<IBeeEffect> WITHERING = potion("withering", MobEffects.WITHER, 10, 0);
    public static final Supplier<IBeeEffect> INVISIBILITY = potion("invisibility", MobEffects.INVISIBILITY, 10, 0);
    // Forestry effects that Complicated Bees lacks
    public static final Supplier<IBeeEffect> DRUNKARD = potion("drunkard", MobEffects.CONFUSION, 10, 0);
    public static final Supplier<IBeeEffect> GLACIAL = EFFECTS.register("glacial", GlacialEffect::new);
    public static final Supplier<IBeeEffect> REPULSION = EFFECTS.register("repulsion", RepulsionEffect::new);
    public static final Supplier<IBeeEffect> CLEANSING = EFFECTS.register("cleansing", CleansingEffect::new);
    public static final Supplier<IBeeEffect> TRANSMUTING = EFFECTS.register("transmuting", () -> new TransmutingEffect(200));
    public static final Supplier<IBeeEffect> CRUMBLING = EFFECTS.register("crumbling", CrumblingEffect::new);

    // spawn alleles: throttle (ticks), chance in percent, max mobs of that kind in the territory (Allele.java values)
    public static final Supplier<IBeeEffect> SPAWN_GHAST = spawn("spawn_ghast", EntityType.GHAST, 2060, 10, 1);
    public static final Supplier<IBeeEffect> SPAWN_SPIDER = spawn("spawn_spider", EntityType.SPIDER, 400, 70, 4);
    public static final Supplier<IBeeEffect> SPAWN_BLAZE = spawn("spawn_blaze", EntityType.BLAZE, 800, 60, 2);
    public static final Supplier<IBeeEffect> SPAWN_WOLF = spawn("spawn_wolf", EntityType.WOLF, 650, 40, 2);
    public static final Supplier<IBeeEffect> SPAWN_PIG = spawn("spawn_pig", EntityType.PIG, 350, 100, 4);
    public static final Supplier<IBeeEffect> SPAWN_COW = spawn("spawn_cow", EntityType.COW, 640, 100, 3);
    public static final Supplier<IBeeEffect> SPAWN_CHICKEN = spawn("spawn_chicken", EntityType.CHICKEN, 20, 100, 20);
    public static final Supplier<IBeeEffect> SPAWN_SHEEP = spawn("spawn_sheep", EntityType.SHEEP, 450, 100, 5);
    public static final Supplier<IBeeEffect> SPAWN_CAT = spawn("spawn_cat", EntityType.OCELOT, 702, 60, 2);
    public static final Supplier<IBeeEffect> SPAWN_HORSE = spawn("spawn_horse", EntityType.HORSE, 450, 59, 2);
    // with Thaumaturge the bats are its firebats and the zombies its brainy zombies (Allele.java's Thaumcraft branch)
    public static final Supplier<IBeeEffect> SPAWN_BATS = firstOf("spawn_bats", 150, 100, 6, "thaumaturge:firebat", "minecraft:bat");
    public static final Supplier<IBeeEffect> SPAWN_BRAINY_ZOMBIE = firstOf("spawn_brainy_zombie", 800, 100, 2,
            "thaumaturge:brainy_zombie", "minecraft:zombie");
    public static final Supplier<IBeeEffect> SPAWN_WISP = firstOf("spawn_wisp", 1800, 79, 2, "thaumaturge:wisp");

    // Ars Magica bees (Ars Magica Legacy mobs by id; they never spawn without that mod)
    public static final Supplier<IBeeEffect> LIGHTNING = EFFECTS.register("lightning", LightningEffect::new);
    public static final Supplier<IBeeEffect> SPAWN_MANA_DRAINER = EFFECTS.register("spawn_mana_drainer", () -> new SpawnMobEffect(
            List.of(ResourceLocation.parse("arsmagicalegacy:mana_creeper"), ResourceLocation.parse("arsmagicalegacy:mana_vortex")), 400, 20, 2));
    // the Wight bee spawned Thaumcraft wisps or Ars Magica hecates: Thaumaturge's wisps when present, else vexes
    public static final Supplier<IBeeEffect> SPAWN_WIGHT = firstOf("spawn_wight", 600, 30, 2, "thaumaturge:wisp", "minecraft:vex");

    private MBEffects() {
    }

    private static Supplier<IBeeEffect> potion(String name, Holder<MobEffect> effect, int seconds, int amplifier) {
        return EFFECTS.register(name, () -> new PotionEffect(new MobEffectInstance(effect, seconds * 20, amplifier, true, true), 80));
    }

    private static Supplier<IBeeEffect> spawn(String name, EntityType<?> type, int throttle, int chance, int max) {
        return EFFECTS.register(name, () -> new SpawnMobEffect(type, throttle, chance, max));
    }

    private static Supplier<IBeeEffect> firstOf(String name, int throttle, int chance, int max, String... mobs) {
        List<ResourceLocation> ids = java.util.Arrays.stream(mobs).map(ResourceLocation::parse).toList();
        return EFFECTS.register(name, () -> new SpawnMobEffect(ids, throttle, chance, max, true));
    }

    static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}
