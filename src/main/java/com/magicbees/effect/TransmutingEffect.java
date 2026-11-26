package com.magicbees.effect;

import com.magicbees.MBDataMaps;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Magic Bees' Transmuting effect. Its rules are the {@code magicbees:transmuting} block data map, shipped with sand to
 * sandstone in sandy biomes, red sand to red sandstone in badlands and, with Railcraft Reborn, stone or cobblestone to
 * quarried stone in forests that are not snowy and abyssal stone in oceans. The first rule whose biome matches and
 * whose target block exists wins.
 */
public class TransmutingEffect extends ThrottledEffect {
    public TransmutingEffect(int throttle) {
        super(throttle);
    }

    @Override
    protected void run(ServerLevel level, BlockEntity housing, ItemStack queen) {
        BlockPos pos = randomInTerritory(level, housing, queen);
        var biome = level.getBiome(pos);
        for (MBDataMaps.Transmute rule : MBDataMaps.transmute(level.getBlockState(pos))) {
            if (!rule.matches(biome)) continue;
            Optional<Block> into = rule.block();
            if (into.isPresent()) {
                level.setBlock(pos, into.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                return;
            }
        }
    }
}
