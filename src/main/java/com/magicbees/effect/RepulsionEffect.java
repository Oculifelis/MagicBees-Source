package com.magicbees.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/** Forestry's Repulsion effect: pushes hostile mobs out of the territory. */
public class RepulsionEffect extends ThrottledEffect {
    public RepulsionEffect() {
        super(10);
    }

    @Override
    protected void run(ServerLevel level, BlockEntity housing, ItemStack queen) {
        Vec3 centre = Vec3.atCenterOf(housing.getBlockPos());
        for (Entity entity : getTerritoryEntities(housing, queen)) {
            if (!(entity instanceof Enemy)) continue;
            Vec3 away = entity.position().subtract(centre).multiply(1, 0, 1);
            if (away.lengthSqr() < 1.0e-4) continue;
            entity.push(away.normalize().scale(0.6).add(0, 0.2, 0));
            entity.hurtMarked = true;
        }
    }
}
