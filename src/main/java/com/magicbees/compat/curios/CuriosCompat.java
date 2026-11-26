package com.magicbees.compat.curios;

import com.magicbees.MBItems;
import com.magicbees.item.BeeRingItem;
import com.magicbees.item.MysteriousMagnetItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * The original's Baubles hooks: the Bee Ring is a ring, the Mysterious Magnet an amulet (Curios "necklace"). Slots and
 * item tags are datapack JSON (data/magicbees/curios, data/curios/tags). Only called when Curios is loaded.
 */
public final class CuriosCompat {
    private CuriosCompat() {
    }

    public static void register(IEventBus bus) {
        bus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(() -> {
            CuriosApi.registerCurio(MBItems.BEE_RING.get(), new ICurioItem() {
                @Override
                public void curioTick(SlotContext slot, ItemStack stack) {
                    if (slot.entity() instanceof Player player && player.level() instanceof ServerLevel level) BeeRingItem.wornTick(stack, level, player);
                }
            });
            CuriosApi.registerCurio(MBItems.MYSTERIOUS_MAGNET.get(), new ICurioItem() {
                @Override
                public void curioTick(SlotContext slot, ItemStack stack) {
                    if (slot.entity() instanceof Player player && player.level() instanceof ServerLevel level && MysteriousMagnetItem.active(stack)) {
                        MysteriousMagnetItem.pull(stack, level, player);
                    }
                }
            });
        }));
    }
}
