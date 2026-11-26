package com.magicbees.effect;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Magic Bees' Curative effect: clears harmful potion effects from players in the territory. */
public class CleansingEffect extends ThrottledEffect {
    public CleansingEffect() {
        super(200);
    }

    @Override
    protected void run(ServerLevel level, BlockEntity housing, ItemStack queen) {
        for (Entity entity : getTerritoryEntities(housing, queen)) {
            if (!(entity instanceof Player player)) continue;
            for (MobEffectInstance instance : player.getActiveEffects().toArray(MobEffectInstance[]::new)) {
                Holder<MobEffect> effect = instance.getEffect();
                if (effect.value().getCategory() == MobEffectCategory.HARMFUL) player.removeEffect(effect);
            }
        }
    }
}
