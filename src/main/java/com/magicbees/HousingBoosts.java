package com.magicbees;

import com.accbdd.complicated_bees.bees.BeeHousingModifier;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Extra housing modifiers from blocks next to a Complicated Bees housing (Magic Bees' Magic Apiary charges). The
 * BaseBeeHousing mixin appends whatever the registered sources return to the housing's frame modifiers.
 */
public final class HousingBoosts {
    public interface Source {
        void addModifiers(BlockEntity housing, List<BeeHousingModifier> out);
    }

    private static final List<Source> SOURCES = new CopyOnWriteArrayList<>();

    private HousingBoosts() {
    }

    public static void register(Source source) {
        SOURCES.add(source);
    }

    public static void apply(BlockEntity housing, List<BeeHousingModifier> out) {
        if (housing.getLevel() == null || housing.getLevel().isClientSide()) return;
        for (Source source : SOURCES) source.addModifiers(housing, out);
    }
}
