package com.magicbees.effect;

import com.accbdd.complicated_bees.bees.effect.BeeEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Runs on the server every {@code throttle} game ticks, like Magic Bees' AlleleEffectThrottled. The housing position
 * offsets the phase so neighbouring apiaries do not all fire on the same tick.
 */
public abstract class ThrottledEffect extends BeeEffect {
    private final int throttle;

    protected ThrottledEffect(int throttle) {
        this.throttle = Math.max(1, throttle);
    }

    @Override
    public final void runEffect(BlockEntity housing, ItemStack queen, int cycleProgress) {
        if (!(housing.getLevel() instanceof ServerLevel level)) return;
        if (Math.floorMod(level.getGameTime() + housing.getBlockPos().asLong(), (long) throttle) != 0) return;
        run(level, housing, queen);
    }

    protected abstract void run(ServerLevel level, BlockEntity housing, ItemStack queen);

    /** A random position inside the queen's territory box (reservoir sampling over the territory iterator). */
    protected BlockPos randomInTerritory(ServerLevel level, BlockEntity housing, ItemStack queen) {
        RandomSource random = level.getRandom();
        BlockPos chosen = housing.getBlockPos();
        int seen = 0;
        var it = getBlockIterator(housing, queen);
        while (it.hasNext()) {
            BlockPos pos = it.next();
            if (random.nextInt(++seen) == 0) chosen = pos.immutable();
        }
        return chosen;
    }
}
