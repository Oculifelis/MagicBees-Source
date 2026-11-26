package com.magicbees.compat.thaumaturge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import com.accbdd.complicated_bees.bees.BeeHousingModifier;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.aura.VisRelayHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Vis Aura Provider (TileEntityVisAuraProvider) beside any bee housing: draws vis from the chunk's aura into charges
 * and spends one charge per boost window. It drinks from a vis relay network first, as the original drew from TC4's vis
 * net (Aqua for mutation, Perditio for death, Aer for production; "all" picks one of the three), and falls back to
 * the chunk's aura. Magic Bees fed it to its own Magic Apiary; here it boosts every Complicated
 * Bees housing it touches, since that apiary's menu cannot be reused for a new block (see COVERAGE.md).
 */
public class VisAuraProviderBlockEntity extends BlockEntity {
    /**
     * What the provider boosts; right-click cycles. Each mode carries its own aspect, boost window and effects, so
     * nothing is looked up by index. Saved by name. ALL draws one of the three fixed modes' aspects at random.
     */
    public enum Mode {
        ALL(null, 850, true, true, true),
        MUTATION("aqua", 400, true, false, false),
        DEATH("perditio", 400, false, true, false),
        PRODUCTION("aer", 300, false, false, true);

        private final ResourceKey<IAspect> aspect;
        final int duration;
        final boolean mutation, lifespan, productivity;

        Mode(String aspect, int duration, boolean mutation, boolean lifespan, boolean productivity) {
            this.aspect = aspect == null ? null : aspect(aspect);
            this.duration = duration;
            this.mutation = mutation;
            this.lifespan = lifespan;
            this.productivity = productivity;
        }

        public String id() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        static Mode of(String id) {
            for (Mode m : values()) {
                if (m.id().equals(id)) return m;
            }
            return ALL;
        }

        Mode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        ResourceKey<IAspect> drawAspect(net.minecraft.util.RandomSource random) {
            return aspect != null ? aspect : FIXED.get(random.nextInt(FIXED.size())).aspect;
        }
    }

    private static final List<Mode> FIXED = List.of(Mode.MUTATION, Mode.DEATH, Mode.PRODUCTION);
    private static final int MAX_CHARGES = 10;
    private static final float VIS_PER_CHARGE = 0.4f;
    private static final float VIS_PER_DRAW = 0.05f;
    // TileEntityMagicApiary: the vis upkeep interval (boost lengths are on Mode)
    private static final int DRAW_INTERVAL = 11;

    private Mode mode = Mode.ALL;
    private int charges;
    private float stored;
    private long activeUntil;

    public VisAuraProviderBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumaturgeCompat.VIS_AURA_PROVIDER_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, VisAuraProviderBlockEntity be) {
        if (be.charges >= MAX_CHARGES || level.getGameTime() % DRAW_INTERVAL != 0) return;
        ResourceKey<IAspect> aspect = be.mode.drawAspect(level.getRandom());
        int centivis = level instanceof ServerLevel server
                ? VisRelayHelper.drainCentivis(server, pos, aspect, Math.round(VIS_PER_DRAW * 100), false) : 0;
        be.stored += centivis > 0 ? centivis / 100f : AuraHelper.drainVis(level, pos, VIS_PER_DRAW, false);
        if (be.stored >= VIS_PER_CHARGE) {
            be.stored -= VIS_PER_CHARGE;
            be.charges++;
        }
        be.setChanged();
    }

    private static ResourceKey<IAspect> aspect(String name) {
        return ResourceKey.create(IAspect.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("thaumaturge", name));
    }

    public boolean cycle(Player player) {
        mode = mode.next();
        setChanged();
        player.displayClientMessage(Component.translatable("message.magicbees.vis_aura_provider." + mode.id(), charges, MAX_CHARGES), true);
        return true;
    }

    /** Adds this provider's boost to a housing's modifiers, spending a charge when the last window has run out. */
    public void addModifiers(List<BeeHousingModifier> out) {
        if (level == null) return;
        long now = level.getGameTime();
        if (now >= activeUntil) {
            if (charges <= 0) return;
            charges--;
            activeUntil = now + mode.duration;
            setChanged();
        }
        BeeHousingModifier.Builder boost = new BeeHousingModifier.Builder();
        if (mode.mutation) boost.mutation(2f);
        if (mode.lifespan) boost.lifespan(0.5f);
        if (mode.productivity) boost.productivity(1.4f);
        out.add(boost.build());
    }

    @Override
    protected void saveAdditional(CompoundTag out, HolderLookup.Provider registries) {
        super.saveAdditional(out, registries);
        out.putString("Mode", mode.id());
        out.putInt("Charges", charges);
        out.putFloat("Stored", stored);
        out.putLong("ActiveUntil", activeUntil);
    }

    @Override
    protected void loadAdditional(CompoundTag in, HolderLookup.Provider registries) {
        super.loadAdditional(in, registries);
        mode = Mode.of(in.getString("Mode"));
        charges = in.getInt("Charges");
        stored = in.getFloat("Stored");
        activeUntil = in.getLong("ActiveUntil");
    }
}
