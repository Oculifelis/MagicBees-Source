package com.magicbees.compat.neovitae;

import com.accbdd.complicated_bees.bees.BeeHousingModifier;
import com.accbdd.complicated_bees.bees.IBeeHousing;
import com.accbdd.complicated_bees.item.FrameItem;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.magicbees.HousingBoosts;
import com.magicbees.MBConfig;
import com.magicbees.MBItems;
import com.magicbees.MagicBees;
import java.util.UUID;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * BloodHelper's frames for Neo Vitae (Blood Magic's successor). Each frame wears like any Complicated Bees frame, and
 * while it sits in a housing, every point of wear is mended with {@link #LP_PER_REPAIR} LP from the housing owner's
 * soul network (the original's EnergyItems.syphonBatteries on every use). Only called when Neo Vitae is loaded.
 */
public final class NeoVitaeCompat {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MagicBees.MODID);
    /** ItemBloodBaseFrame/ItemBloodFrame/ItemFrenziedFrame: energyUsed = 1000 */
    private static final int LP_PER_REPAIR = 1000;
    private static final int DURABILITY = 10;

    public static final DeferredItem<FrameItem> BLOOD_SOAKED_FRAME = frame("blood_soaked_frame",
            new BeeHousingModifier.Builder().productivity(2f).build());
    public static final DeferredItem<FrameItem> BLOOD_FRAME = frame("blood_frame",
            new BeeHousingModifier.Builder().lifespan(0.0001f).productivity(0f).build());
    public static final DeferredItem<FrameItem> FRENZIED_FRAME = frame("frenzied_frame",
            new BeeHousingModifier.Builder().mutation(10f).lifespan(0.0001f).productivity(0f).build());

    private NeoVitaeCompat() {
    }

    private static DeferredItem<FrameItem> frame(String name, BeeHousingModifier modifier) {
        return ITEMS.registerItem(name, p -> new FrameItem(p.durability(DURABILITY), modifier, MBConfig.BLOOD_FRAMES));
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        MBItems.addToTab(BLOOD_SOAKED_FRAME);
        MBItems.addToTab(BLOOD_FRAME);
        MBItems.addToTab(FRENZIED_FRAME);
        HousingBoosts.register((housing, out) -> {
            if (housing instanceof IBeeHousing beeHousing) mend(beeHousing);
        });
    }

    private static void mend(IBeeHousing housing) {
        UUID owner = housing.getOwner();
        if (owner == null) return;
        var frames = housing.getFrameItemHandler();
        for (int slot = 0; slot < frames.getSlots(); slot++) {
            ItemStack frame = frames.getStackInSlot(slot);
            if (!frame.isDamaged() || !isBloodFrame(frame)) continue;
            IAnima anima = NeoVitaeAPI.getInstance().getAnima(owner);
            while (frame.isDamaged() && anima.getCurrentEV() >= LP_PER_REPAIR) {
                anima.syphon(AnimaTicket.create(LP_PER_REPAIR));
                frame.setDamageValue(frame.getDamageValue() - 1);
            }
        }
    }

    private static boolean isBloodFrame(ItemStack stack) {
        return stack.is(BLOOD_SOAKED_FRAME.get()) || stack.is(BLOOD_FRAME.get()) || stack.is(FRENZIED_FRAME.get());
    }
}
