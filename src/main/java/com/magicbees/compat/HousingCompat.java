package com.magicbees.compat;

import com.accbdd.complicated_bees.bees.BeeHousingModifier;
import com.magicbees.HousingBoosts;
import com.magicbees.MBDataMaps;
import com.magicbees.MagicBees;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Housing boosts from other mods' blocks and items, chosen by tag or data map entry so no compile dependency is
 * needed. The numbers come from the {@code magicbees:housing_accelerator} and {@code magicbees:mutation_catalyst} data
 * maps; a tagged block or item with no entry uses the defaults (productivity 1.5 for 30 power, mutation 1.5).
 *
 * <ul>
 *   <li>{@link #ACCELERATORS}: a powered block touching the housing (Technomancy's Crop Accelerator) raises
 *   productivity. It pays its cost in stored power for each work cycle, through its public {@code power()}
 *   and {@code add(int)} methods; with too little power there is no boost.</li>
 *   <li>{@link #CATALYSTS}: an item in an item frame on the housing (Magia Naturalis' Phenomorphous Stone) raises the
 *   mutation chance.</li>
 * </ul>
 */
public final class HousingCompat {
    public static final TagKey<Block> ACCELERATORS = TagKey.create(Registries.BLOCK, MagicBees.id("housing_accelerators"));
    public static final TagKey<Item> CATALYSTS = TagKey.create(Registries.ITEM, MagicBees.id("mutation_catalysts"));
    /** Complicated Bees asks for the modifiers several times per cycle; one payment covers this many ticks. */
    private static final int PAID_TICKS = 20;
    private static final int CATALYST_RECHECK = 100;

    private record Boost(long at, BeeHousingModifier modifier) {
    }

    private static final Map<Float, BeeHousingModifier> FASTER = new ConcurrentHashMap<>();
    private static final Map<Float, BeeHousingModifier> MUTAGENIC = new ConcurrentHashMap<>();
    private static final Map<BlockEntity, Boost> PAID = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<BlockEntity, Boost> CATALYST = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Class<?>, Method[]> POWER = new ConcurrentHashMap<>();
    private static final Method[] NONE = new Method[0];

    private HousingCompat() {
    }

    public static void register() {
        HousingBoosts.register(HousingCompat::accelerators);
        HousingBoosts.register(HousingCompat::catalysts);
    }

    private static void accelerators(BlockEntity housing, List<BeeHousingModifier> out) {
        Level level = housing.getLevel();
        long now = level.getGameTime();
        Boost last = PAID.get(housing);
        if (last != null && now - last.at() >= 0 && now - last.at() < PAID_TICKS) {
            out.add(last.modifier());
            return;
        }
        BlockPos pos = housing.getBlockPos();
        for (Direction d : Direction.values()) {
            BlockPos at = pos.relative(d);
            BlockState state = level.getBlockState(at);
            MBDataMaps.Accelerator acc = MBDataMaps.accelerator(state.getBlock());
            if (acc == null) {
                if (!state.is(ACCELERATORS)) continue;
                acc = MBDataMaps.Accelerator.DEFAULT;
            }
            BlockEntity be = level.getBlockEntity(at);
            if (be != null && pay(be, acc.cost())) {
                BeeHousingModifier mod = FASTER.computeIfAbsent(acc.productivity(),
                        f -> new BeeHousingModifier.Builder().productivity(f).build());
                PAID.put(housing, new Boost(now, mod));
                out.add(mod);
                return;
            }
        }
    }

    /** Takes {@code cost} power from the block entity if it has that much. */
    private static boolean pay(BlockEntity be, int cost) {
        Method[] m = POWER.computeIfAbsent(be.getClass(), c -> {
            try {
                return new Method[]{c.getMethod("power"), c.getMethod("add", int.class)};
            } catch (NoSuchMethodException e) {
                MagicBees.LOG.warn("{} is a housing accelerator but has no power()/add(int); it will not boost housings", c.getName());
                return NONE;
            }
        });
        if (m.length == 0) return false;
        if (cost == 0) return true;
        try {
            if ((int) m[0].invoke(be) < cost) return false;
            m[1].invoke(be, -cost);
            be.setChanged();
            return true;
        } catch (ReflectiveOperationException | ClassCastException e) {
            return false;
        }
    }

    /** The strongest catalyst among the item frames on the housing, or null. */
    private static BeeHousingModifier catalystIn(Level level, BlockEntity housing) {
        AABB box = new AABB(housing.getBlockPos()).inflate(1.0);
        float best = 0;
        for (ItemFrame f : level.getEntitiesOfClass(ItemFrame.class, box, f -> !f.getItem().isEmpty())) {
            ItemStack stack = f.getItem();
            MBDataMaps.Catalyst c = MBDataMaps.catalyst(stack.getItem());
            if (c == null && stack.is(CATALYSTS)) c = MBDataMaps.Catalyst.DEFAULT;
            if (c != null && c.mutation() > best) best = c.mutation();
        }
        return best == 0 ? null : MUTAGENIC.computeIfAbsent(best, m -> new BeeHousingModifier.Builder().mutation(m).build());
    }

    private static void catalysts(BlockEntity housing, List<BeeHousingModifier> out) {
        Level level = housing.getLevel();
        long now = level.getGameTime();
        Boost seen = CATALYST.get(housing);
        if (seen == null || now - seen.at() >= CATALYST_RECHECK || now < seen.at()) {
            seen = new Boost(now, catalystIn(level, housing));
            CATALYST.put(housing, seen);
        }
        if (seen.modifier() != null) out.add(seen.modifier());
    }
}
