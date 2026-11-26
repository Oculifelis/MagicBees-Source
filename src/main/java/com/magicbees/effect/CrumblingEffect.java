package com.magicbees.effect;

import com.magicbees.MBDataMaps;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Magic Bees' Crumbling effect: weathers stone in the territory one step at a time. The table (AlleleEffectCrumbling's)
 * is the {@code magicbees:crumbling} block data map.
 */
public class CrumblingEffect extends ThrottledEffect {
    public CrumblingEffect() {
        super(200);
    }

    @Override
    protected void run(ServerLevel level, BlockEntity housing, ItemStack queen) {
        BlockPos pos = randomInTerritory(level, housing, queen);
        MBDataMaps.Crumble crumble = MBDataMaps.crumble(level.getBlockState(pos));
        if (crumble != null) crumble.block().ifPresent(b -> level.setBlock(pos, b.defaultBlockState(), Block.UPDATE_CLIENTS));
    }
}
