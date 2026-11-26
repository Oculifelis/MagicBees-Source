package com.magicbees.block;

import com.accbdd.complicated_bees.bees.GeneticHelper;
import com.accbdd.complicated_bees.bees.effect.IBeeEffect;
import com.accbdd.complicated_bees.bees.gene.GeneEffect;
import com.accbdd.complicated_bees.item.DroneItem;
import com.magicbees.MBBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Effect Jar (TileEntityEffectJar): drones go in; one at a time is taken as the jar's "queen", whose species effect
 * runs around the jar as if from a hive. Every 550 ticks the bee ages; after {@link #LIFE} such steps it dies and the
 * next drone takes its place.
 */
public class EffectJarBlockEntity extends BlockEntity {
    public static final int AGE_INTERVAL = 550;
    /** Forestry bees lost 0.26 of a health point per step; a 20-health drone lasted about 77 steps. */
    public static final int LIFE = 77;

    private ItemStack drones = ItemStack.EMPTY;
    private ItemStack active = ItemStack.EMPTY;
    private int life;
    private int throttle;

    public EffectJarBlockEntity(BlockPos pos, BlockState state) {
        super(MBBlocks.EFFECT_JAR_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EffectJarBlockEntity jar) {
        if (jar.active.isEmpty()) {
            if (jar.drones.isEmpty()) return;
            jar.active = jar.drones.split(1);
            jar.life = LIFE;
            jar.setChanged();
        }
        if (GeneticHelper.getGeneValue(jar.active, GeneEffect.ID, true) instanceof IBeeEffect effect) {
            effect.runEffect(jar, jar.active, jar.throttle);
        }
        if (++jar.throttle > AGE_INTERVAL) {
            jar.throttle = 0;
            if (--jar.life <= 0) jar.active = ItemStack.EMPTY;
            jar.setChanged();
        }
    }

    /** Right-click with drones to add them; with an empty hand, take the waiting drones back out. */
    public boolean use(Player player, ItemStack held) {
        if (held.getItem() instanceof DroneItem) {
            if (drones.isEmpty()) {
                drones = held.split(held.getCount());
            } else if (ItemStack.isSameItemSameComponents(drones, held)) {
                int moved = Math.min(held.getCount(), drones.getMaxStackSize() - drones.getCount());
                drones.grow(moved);
                held.shrink(moved);
            } else {
                return false;
            }
            setChanged();
            return true;
        }
        if (!held.isEmpty()) return false;
        if (!drones.isEmpty()) {
            player.getInventory().placeItemBackInInventory(drones);
            drones = ItemStack.EMPTY;
            setChanged();
        } else {
            player.displayClientMessage(active.isEmpty() ? Component.translatable("message.magicbees.effect_jar.empty")
                    : Component.translatable("message.magicbees.effect_jar.active", active.getHoverName(), life * 100 / LIFE), true);
        }
        return true;
    }

    /** Called by DeviceBlock#onRemove when the jar is broken (1.21.1 has no preRemoveSideEffects). */
    public void dropContents() {
        if (level instanceof ServerLevel) {
            BlockPos pos = worldPosition;
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drones);
            drones = ItemStack.EMPTY;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag out, HolderLookup.Provider registries) {
        super.saveAdditional(out, registries);
        if (!drones.isEmpty()) out.put("Drones", drones.save(registries));
        if (!active.isEmpty()) out.put("Active", active.save(registries));
        out.putInt("Life", life);
        out.putInt("Throttle", throttle);
    }

    @Override
    protected void loadAdditional(CompoundTag in, HolderLookup.Provider registries) {
        super.loadAdditional(in, registries);
        drones = ItemStack.parseOptional(registries, in.getCompound("Drones"));
        active = ItemStack.parseOptional(registries, in.getCompound("Active"));
        life = in.getInt("Life");
        throttle = in.getInt("Throttle");
    }
}
