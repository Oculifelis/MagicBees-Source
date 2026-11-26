package com.magicbees.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;

/** Extra Bees' Lightning effect, which Magic Bees' Lightning bee used: now and then strikes inside the territory. */
public class LightningEffect extends ThrottledEffect {
    public LightningEffect() {
        super(200);
    }

    @Override
    protected void run(ServerLevel level, BlockEntity housing, ItemStack queen) {
        if (level.getRandom().nextInt(4) != 0) return;
        BlockPos target = randomInTerritory(level, housing, queen);
        BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, target);
        if (ground.closerThan(housing.getBlockPos(), 2.5)) return; // never strike the hive itself
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt == null) return;
        bolt.snapTo(ground.getX() + 0.5, ground.getY(), ground.getZ() + 0.5);
        level.addFreshEntity(bolt);
    }
}
