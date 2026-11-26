package com.magicbees.compat.thaumaturge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import com.accbdd.complicated_bees.bees.BeeHousingModifier;
import com.accbdd.complicated_bees.block.entity.BaseBeeHousing;
import com.accbdd.complicated_bees.bees.GeneticHelper;
import com.accbdd.complicated_bees.item.BeeItem;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.api.essentia.IItemEssentia;
import com.magicbees.MagicBees;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * The Apimancer's Drainer under a housing whose queen is an Essentia bee: every cycle it draws essentia of the aspect
 * it is attuned to, scaled by the housing's production, at the cost of extra queen age. Tubes pull it from any side.
 */
public class ApimancersDrainerBlockEntity extends BlockEntity implements IEssentiaTransport {
    private static final ResourceLocation ESSENTIA_BEE = MagicBees.id("essentia");
    private static final int INTERVAL = 200;
    private static final int CAPACITY = 512;

    private @Nullable ResourceKey<IAspect> attuned;
    private int amount;
    private float partial;

    public ApimancersDrainerBlockEntity(BlockPos pos, BlockState state) {
        super(ThaumaturgeCompat.APIMANCERS_DRAINER_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ApimancersDrainerBlockEntity be) {
        if (be.attuned == null || be.amount >= CAPACITY || (level.getGameTime() + pos.asLong()) % INTERVAL != 0) return;
        if (!(level.getBlockEntity(pos.above()) instanceof BaseBeeHousing housing)) return;
        ItemStack queen = housing.getLogic().getQueen();
        if (queen.isEmpty() || !ESSENTIA_BEE.equals(GeneticHelper.getSpeciesLoc(queen))) return;
        float production = 1f, lifespan = 1f;
        for (BeeHousingModifier m : housing.getHousingModifiers()) {
            production *= m.getProductivityMod();
            lifespan *= m.getLifespanMod();
        }
        be.partial += production;
        int whole = (int) be.partial;
        if (whole <= 0) return;
        be.partial -= whole;
        be.amount = Math.min(CAPACITY, be.amount + whole);
        // draining costs the queen a second tick of age, as the drainer's essence pull did
        BeeItem.setAge(queen, BeeItem.getAge(queen) + 1f / Math.max(lifespan, 0.0001f));
        housing.setChanged();
        be.setChanged();
    }

    /** Right-click with an essentia container attunes to its first aspect; an empty hand shows the contents. */
    public boolean use(Player player, ItemStack held) {
        IItemEssentia container = held.isEmpty() ? null : held.getCapability(EssentiaCapabilities.CONTAINER);
        if (container != null) {
            for (AspectInstance a : container.getAspects().entries()) {
                ResourceKey<IAspect> key = a.aspect().unwrapKey().orElse(null);
                if (key == null) continue;
                if (!key.equals(attuned)) amount = 0;
                attuned = key;
                setChanged();
                break;
            }
        }
        player.displayClientMessage(attuned == null ? Component.translatable("message.magicbees.apimancers_drainer.unattuned")
                : Component.translatable("message.magicbees.apimancers_drainer.contents", amount, attuned.location().toString()), true);
        return true;
    }

    private @Nullable Holder<IAspect> attunedHolder() {
        if (attuned == null || level == null) return null;
        return level.registryAccess().registryOrThrow(IAspect.REGISTRY_KEY).getHolder(attuned).orElse(null);
    }

    @Override
    public boolean isConnectable(Direction side) {
        return true;
    }

    @Override
    public boolean canInputFrom(Direction side) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction side) {
        return true;
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> type, int count) {
    }

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable Direction side) {
        return null;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction side) {
        return 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> type, int count, Direction side) {
        return takeEssentia(type, count, side, false);
    }

    @Override
    public int takeEssentia(Holder<IAspect> type, int count, Direction side, boolean simulate) {
        Holder<IAspect> held = attunedHolder();
        if (held == null || amount <= 0 || !held.equals(type)) return 0;
        int taken = Math.min(count, amount);
        if (!simulate) {
            amount -= taken;
            setChanged();
        }
        return taken;
    }

    @Override
    public int addEssentia(Holder<IAspect> type, int count, Direction side) {
        return 0;
    }

    @Override
    public int addEssentia(Holder<IAspect> type, int count, Direction side, boolean simulate) {
        return 0;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(@Nullable Direction side) {
        return amount > 0 ? attunedHolder() : null;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction side) {
        return amount;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    protected void saveAdditional(CompoundTag out, HolderLookup.Provider registries) {
        super.saveAdditional(out, registries);
        if (attuned != null) out.putString("Aspect", attuned.location().toString());
        out.putInt("Amount", amount);
        out.putFloat("Partial", partial);
    }

    @Override
    protected void loadAdditional(CompoundTag in, HolderLookup.Provider registries) {
        super.loadAdditional(in, registries);
        ResourceLocation id = in.getString("Aspect").isEmpty() ? null : ResourceLocation.tryParse(in.getString("Aspect"));
        attuned = id == null ? null : ResourceKey.create(IAspect.REGISTRY_KEY, id);
        amount = in.getInt("Amount");
        partial = in.getFloat("Partial");
    }
}
