package com.magicbees;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Item data: the Bee Ring's bees, and the Mysterious Magnet's level (its old damage value >> 1) and on/off flag (damage & 1). */
public final class MBComponents {
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MagicBees.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MAGNET_LEVEL = COMPONENTS.registerComponentType(
            "magnet_level", b -> b.persistent(Codec.intRange(0, 8)).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> MAGNET_ACTIVE = COMPONENTS.registerComponentType(
            "magnet_active", b -> b.persistent(Unit.CODEC).networkSynchronized(Unit.STREAM_CODEC));

    /** Bee Ring (InventoryBeeRing): slot 0 the waiting drones, slot 1 the bee currently at work. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> RING_BEES = COMPONENTS.registerComponentType(
            "ring_bees", b -> b.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC));
    /** Ageing steps the working bee has left. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RING_LIFE = COMPONENTS.registerComponentType(
            "ring_life", b -> b.persistent(Codec.intRange(0, 1000)).networkSynchronized(ByteBufCodecs.VAR_INT));

    private MBComponents() {
    }

    static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}
