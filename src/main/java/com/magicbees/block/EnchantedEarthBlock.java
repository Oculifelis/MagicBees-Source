package com.magicbees.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.common.util.TriState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Enchanted Earth (BlockEnchantedEarth): sustains any plant, and each random tick hurries the first random-ticking
 * block above its column and, half the time, one beside it a level up.
 */
public class EnchantedEarthBlock extends Block {
    public EnchantedEarthBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos above = pos.above();
        while (above.getY() < level.getMaxBuildHeight() && level.getBlockState(above).is(this)) above = above.above();
        tickAt(level, above, random);
        if (random.nextInt(2) == 0) tickAt(level, pos.offset(random.nextInt(3) - 1, 1, random.nextInt(3) - 1), random);
    }

    private static void tickAt(ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState target = level.getBlockState(pos);
        if (target.isRandomlyTicking()) target.randomTick(level, pos, random);
    }

    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
        return TriState.TRUE;
    }
}
