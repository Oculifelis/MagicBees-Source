package com.magicbees.compat.thaumaturge;

import com.accbdd.complicated_bees.bees.effect.IBeeEffect;
import com.accbdd.complicated_bees.registry.BeeEffectRegistration;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.magicbees.MagicBees;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

/**
 * The node conversion chances of the Tainting, Purifying and Ravening effects as the effect data map
 * {@code data/magicbees/data_maps/complicated_bees/bee_effect/node_conversion.json}. {@code into} is the chance per
 * fire that a normal node turns into the effect's type, {@code back} the chance that a node of another special type
 * falls back to normal. Either may be left out; the defaults are NodeHelper's numbers.
 */
public final class NodeConversion {
    /** NodeHelper's chance to turn a node into each type. */
    static final Map<NodeType, Float> DEFAULTS = Map.of(NodeType.NORMAL, 0.075f, NodeType.TAINTED, 0.057f,
            NodeType.PURE, 0.035f, NodeType.HUNGRY, 0.0094f);

    public record Entry(Optional<Float> into, Optional<Float> back) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.floatRange(0, 1).optionalFieldOf("into").forGetter(Entry::into),
                Codec.floatRange(0, 1).optionalFieldOf("back").forGetter(Entry::back)
        ).apply(i, Entry::new));
    }

    public static final DataMapType<IBeeEffect, Entry> TYPE = DataMapType.builder(
            MagicBees.id("node_conversion"), BeeEffectRegistration.BEE_REGISTRY_KEY, Entry.CODEC).build();

    private NodeConversion() {
    }

    static void register(IEventBus bus) {
        bus.addListener((RegisterDataMapTypesEvent e) -> e.register(TYPE));
        if (Boolean.getBoolean("magicbees.selftest")) {
            NeoForge.EVENT_BUS.addListener((ServerStartedEvent e) -> selfTest(
                    e.getServer().getPackRepository().getSelectedIds().contains("file/qa_pack")));
        }
    }

    /** The chance that an effect converting into {@code target} moves a node to {@code next}. */
    static float chance(Holder<IBeeEffect> self, NodeType target, NodeType next) {
        Entry e = self == null ? null : self.getData(TYPE);
        Optional<Float> set = e == null ? Optional.empty() : next == target ? e.into() : e.back();
        return set.orElseGet(() -> DEFAULTS.getOrDefault(next, 0.05f));
    }

    private static Holder<IBeeEffect> holder(DeferredHolder<IBeeEffect, ?> h) {
        return h.getDelegate();
    }

    /** The world pack in tools/dev/qa_pack sets Ravening's {@code into} to 0.5; Purifying keeps the defaults. */
    private static void selfTest(boolean qaPack) {
        try {
            float pureInto = chance(holder(ThaumaturgeCompat.NODE_PURIFYING), NodeType.PURE, NodeType.PURE);
            float pureBack = chance(holder(ThaumaturgeCompat.NODE_PURIFYING), NodeType.PURE, NodeType.NORMAL);
            report("node conversion purifying defaults", pureInto == 0.035f && pureBack == 0.075f, pureInto + "/" + pureBack);
            float hungry = chance(holder(ThaumaturgeCompat.NODE_RAVENING), NodeType.HUNGRY, NodeType.HUNGRY);
            report("node conversion ravening " + (qaPack ? "pack" : "default"), hungry == (qaPack ? 0.5f : 0.0094f), String.valueOf(hungry));
        } catch (Exception ex) {
            MagicBees.LOG.error("SELFTEST FAIL node conversion crashed", ex);
        }
    }

    private static void report(String name, boolean ok, String got) {
        if (ok) MagicBees.LOG.info("SELFTEST PASS {}", name);
        else MagicBees.LOG.error("SELFTEST FAIL {} (got {})", name, got);
    }
}
