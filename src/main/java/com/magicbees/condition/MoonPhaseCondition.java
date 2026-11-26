package com.magicbees.condition;

import com.accbdd.complicated_bees.recipe.mutation.condition.IMutationCondition;
import com.accbdd.complicated_bees.recipe.mutation.condition.MutationCondition;
import com.magicbees.MagicBees;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Magic Bees' MoonPhaseMutationRestriction: the mutation only happens while the moon phase is between {@code from}
 * and {@code to} (0 = full moon .. 7 = waxing gibbous, wrapping). JSON: {"magicbees:moon_phase": {"from": 0, "to": 0}}.
 */
public class MoonPhaseCondition extends MutationCondition {
    public static final String ID = "moon_phase";
    private static final String[] NAMES = {"full", "waning_gibbous", "last_quarter", "waning_crescent", "new",
            "waxing_crescent", "first_quarter", "waxing_gibbous"};
    private final int from;
    private final int to;

    public MoonPhaseCondition(int from, int to) {
        this.from = Math.floorMod(from, 8);
        this.to = Math.floorMod(to, 8);
    }

    /** Vanilla's moon timeline runs on the overworld clock: one phase per 24000-tick day, eight phases. */
    public static int phase(Level level) {
        return (int) Math.floorMod(level.getDayTime() / 24000L, 8L);
    }

    @Override
    public ResourceLocation getID() {
        return MagicBees.id(ID);
    }

    @Override
    public boolean check(Level level, BlockPos pos) {
        int span = Math.floorMod(to - from, 8);
        return Math.floorMod(phase(level) - from, 8) <= span;
    }

    @Override
    public Component getDescription() {
        Component a = Component.translatable("moon_phase.magicbees." + NAMES[from]).withStyle(ChatFormatting.GOLD);
        if (from == to) return Component.translatable("gui.magicbees.mutations.moon_phase", a);
        Component b = Component.translatable("moon_phase.magicbees." + NAMES[to]).withStyle(ChatFormatting.GOLD);
        return Component.translatable("gui.magicbees.mutations.moon_phase_range", a, b);
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("from", from);
        tag.putInt("to", to);
        return tag;
    }

    @Override
    public IMutationCondition deserialize(CompoundTag tag) {
        return new MoonPhaseCondition(tag.getInt("from"), tag.contains("to") ? tag.getInt("to") : tag.getInt("from"));
    }
}
