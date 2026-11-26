package com.magicbees.effect;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Magic Bees' AlleleEffectSpawnMob (and the weighted variant: one of several mobs, picked at random). Mobs are named by
 * registry id and looked up when the effect fires, so a mob from a mod that is not installed simply never spawns.
 */
public class SpawnMobEffect extends ThrottledEffect {
    private final List<ResourceLocation> types;
    private final int chance;
    private final int max;
    private final boolean firstAvailable;

    public SpawnMobEffect(List<ResourceLocation> types, int throttle, int chance, int max) {
        this(types, throttle, chance, max, false);
    }

    /** With {@code firstAvailable}, the first installed mob of the list is used (a modded mob with a vanilla fallback). */
    public SpawnMobEffect(List<ResourceLocation> types, int throttle, int chance, int max, boolean firstAvailable) {
        super(throttle);
        this.types = types;
        this.chance = chance;
        this.max = max;
        this.firstAvailable = firstAvailable;
    }

    public SpawnMobEffect(EntityType<?> type, int throttle, int chance, int max) {
        this(List.of(BuiltInRegistries.ENTITY_TYPE.getKey(type)), throttle, chance, max);
    }

    @Override
    protected void run(ServerLevel level, BlockEntity housing, ItemStack queen) {
        if (level.getRandom().nextInt(100) >= chance) return;
        Optional<EntityType<?>> found = firstAvailable
                ? types.stream().flatMap(id -> BuiltInRegistries.ENTITY_TYPE.getOptional(id).stream()).findFirst()
                : BuiltInRegistries.ENTITY_TYPE.getOptional(types.get(level.getRandom().nextInt(types.size())));
        if (found.isEmpty()) return;
        EntityType<?> type = found.get();
        long present = getTerritoryEntities(housing, queen).stream().filter(e -> e.getType() == type).count();
        if (present >= max) return;
        BlockPos pos = housing.getBlockPos();
        for (int attempt = 0; attempt < 8; attempt++) {
            BlockPos at = pos.offset(level.getRandom().nextInt(9) - 4, level.getRandom().nextInt(3) - 1, level.getRandom().nextInt(9) - 4);
            if (!level.getBlockState(at).isAir() || !level.getBlockState(at.above()).isAir()) continue;
            Entity entity = type.create(level);
            if (entity == null) return;
            entity.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.getRandom().nextFloat() * 360f, 0f);
            level.addFreshEntity(entity);
            return;
        }
    }
}
