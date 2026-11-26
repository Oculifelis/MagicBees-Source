package com.magicbees.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;

/** Forestry's Glacial effect: freezes water sources in the territory and lays snow on open ground. */
public class GlacialEffect extends ThrottledEffect {
    public GlacialEffect() {
        super(20);
    }

    @Override
    protected void run(ServerLevel level, BlockEntity housing, ItemStack queen) {
        BlockPos pos = randomInTerritory(level, housing, queen);
        var fluid = level.getFluidState(pos);
        if (fluid.is(Fluids.WATER) && fluid.isSource() && level.getBlockState(pos).is(Blocks.WATER)) {
            level.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
        } else if (level.getBlockState(pos).isAir() && Blocks.SNOW.defaultBlockState().canSurvive(level, pos)) {
            level.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState());
        }
    }
}
