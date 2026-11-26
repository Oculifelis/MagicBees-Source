package com.magicbees.compat.thaumaturge;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeData;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeLocationIndex;
import com.accbdd.complicated_bees.bees.effect.IBeeEffect;
import com.magicbees.effect.ThrottledEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * The Thaumic node bees (Magic Bees' NodeHelper): each acts on the nearest aura node within {@link #RANGE} blocks of the
 * hive. Only loaded when Thaumaturge is installed.
 */
public abstract class NodeEffect extends ThrottledEffect {
    /** Magic Bees searched the queen's territory; the largest territory reaches about this far. */
    public static final double RANGE = 12;
    /** Config.thaumcraftNodeMaxSize */
    private static final int NODE_MAX_SIZE = 255;
    // NodeHelper.conversionChances

    protected NodeEffect(int throttle) {
        super(throttle);
    }

    @Override
    protected final void run(ServerLevel level, BlockEntity housing, ItemStack queen) {
        Optional<BlockPos> at = NodeLocationIndex.get(level).findNearestAny(housing.getBlockPos(), RANGE);
        if (at.isPresent() && level.isLoaded(at.get()) && level.getBlockEntity(at.get()) instanceof BlockEntityNode node) {
            if (act(level.getRandom(), node, level)) node.invalidateRefill();
        }
    }

    /** Changes the node; true when something changed and clients need the update. */
    protected abstract boolean act(RandomSource random, BlockEntityNode node, ServerLevel level);


    /** Rejuvenating (effectVisRecharge): refills the node's aspects toward their base amounts. */
    public static class Recharge extends NodeEffect {
        public Recharge() {
            super(20);
        }

        @Override
        protected boolean act(RandomSource random, BlockEntityNode node, ServerLevel level) {
            AspectList base = node.capacity();
            AspectList now = node.getAspects();
            boolean changed = false;
            for (AspectInstance a : base.entries()) {
                int diff = a.amount() - now.amountOf(a.aspect());
                if (diff > 0) {
                    now = now.add(a.aspect(), diff > 1 ? random.nextInt(diff) + 1 : 1);
                    changed = true;
                }
            }
            if (changed) node.setAspects(now);
            return changed;
        }
    }

    /** Empowering (effectNodeEmpower): grows a random primal in the node's base, as NodeHelper.growNodeInRange. */
    public static class Empower extends NodeEffect {
        public Empower() {
            super(200);
        }

        @Override
        protected boolean act(RandomSource random, BlockEntityNode node, ServerLevel level) {
            AspectList base = node.capacity();
            if (random.nextInt(Math.max(base.totalAmount(), 1)) >= 120) return false;
            List<Holder<IAspect>> primals = new ArrayList<>();
            ResourceKey<Registry<IAspect>> aspects = ResourceKey.createRegistryKey(TTAspects.AER.registry());
            var registry = level.registryAccess().lookupOrThrow(aspects);
            for (ResourceKey<IAspect> key : TTAspects.PRIMALS) registry.get(key).ifPresent(primals::add);
            if (primals.isEmpty()) return false;
            for (int roll = 0; roll < 20; roll++) {
                Holder<IAspect> aspect = primals.get(random.nextInt(primals.size()));
                if (base.amountOf(aspect) >= NODE_MAX_SIZE) continue;
                int amount = 1 + random.nextInt(2);
                node.applyNodeData(new NodeData(node.kind(), Optional.ofNullable(node.trait()),
                        node.getAspects().add(aspect, amount), base.add(aspect, amount)));
                return true;
            }
            return false;
        }
    }

    /** Nexus (effectNodeRepair): steadies unstable nodes and brightens fading ones. */
    public static class Repair extends NodeEffect {
        public Repair() {
            super(2);
        }

        @Override
        protected boolean act(RandomSource random, BlockEntityNode node, ServerLevel level) {
            if (node.kind() == NodeType.UNSTABLE) {
                node.reclassify(NodeType.NORMAL);
                return true;
            }
            NodeModifier modifier = node.trait();
            NodeModifier next = modifier;
            if (modifier == NodeModifier.FADING) next = NodeModifier.PALE;
            else if (modifier == NodeModifier.PALE && random.nextFloat() < 0.65f) next = null;
            else if (modifier == null && random.nextFloat() < 0.15f) next = NodeModifier.BRIGHT;
            if (next == modifier) return false;
            node.assignTrait(next);
            return true;
        }
    }

    /**
     * Flux, Pure and Hungry (effectNodeConversion*): a node of another special type first falls back to normal, then
     * turns into the target type, each step with NodeHelper's chance
     * unless the {@link NodeConversion} data map sets another.
     */
    public static class Convert extends NodeEffect {
        private final NodeType target;
        private final java.util.function.Supplier<DeferredHolder<IBeeEffect, ?>> self;

        public Convert(NodeType target, int throttle, java.util.function.Supplier<DeferredHolder<IBeeEffect, ?>> self) {
            super(throttle);
            this.target = target;
            this.self = self;
        }

        @Override
        protected boolean act(RandomSource random, BlockEntityNode node, ServerLevel level) {
            NodeType type = node.kind();
            NodeType next = type == NodeType.NORMAL ? target : type != target ? NodeType.NORMAL : null;
            if (next == null || random.nextFloat() >= NodeConversion.chance(self.get().getDelegate(), target, next)) return false;
            node.reclassify(next);
            return true;
        }
    }
}
