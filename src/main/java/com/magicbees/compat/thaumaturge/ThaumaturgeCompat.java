package com.magicbees.compat.thaumaturge;

import com.accbdd.complicated_bees.bees.effect.IBeeEffect;
import com.accbdd.complicated_bees.recipe.mutation.condition.IMutationCondition;
import com.accbdd.complicated_bees.registry.BeeEffectRegistration;
import com.accbdd.complicated_bees.registry.MutationRegistration;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.magicbees.HousingBoosts;
import com.magicbees.MBItems;
import com.magicbees.MagicBees;
import com.magicbees.block.DeviceBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * ThaumcraftHelper's code half: the node bee effects, the near-node mutation condition, the Vis Aura Provider and the
 * Apimancer's Drainer. Called only when Thaumaturge is loaded; the rest of the Thaumaturge module is datapack JSON
 * behind neoforge:mod_loaded.
 */
public final class ThaumaturgeCompat {
    private static final DeferredRegister<IBeeEffect> EFFECTS = DeferredRegister.create(BeeEffectRegistration.BEE_REGISTRY_KEY, MagicBees.MODID);
    private static final DeferredRegister<IMutationCondition> CONDITIONS = DeferredRegister.create(MutationRegistration.MUTATION_CONDITION_KEY, MagicBees.MODID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MagicBees.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MagicBees.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MagicBees.MODID);

    public static final DeferredBlock<DeviceBlock<VisAuraProviderBlockEntity>> VIS_AURA_PROVIDER = BLOCKS.registerBlock("vis_aura_provider",
            p -> new DeviceBlock<>(p, ThaumaturgeCompat.VIS_AURA_PROVIDER_BE, VisAuraProviderBlockEntity::serverTick,
                    (be, player, hand, held) -> be.cycle(player)),
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).sound(SoundType.WOOD));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VisAuraProviderBlockEntity>> VIS_AURA_PROVIDER_BE =
            BLOCK_ENTITIES.register("vis_aura_provider", () -> BlockEntityType.Builder.of(VisAuraProviderBlockEntity::new, VIS_AURA_PROVIDER.get()).build(null));
    public static final DeferredItem<BlockItem> VIS_AURA_PROVIDER_ITEM = ITEMS.registerSimpleBlockItem(VIS_AURA_PROVIDER);

    public static final DeferredBlock<DeviceBlock<ApimancersDrainerBlockEntity>> APIMANCERS_DRAINER = BLOCKS.registerBlock("apimancers_drainer",
            p -> new DeviceBlock<>(p, ThaumaturgeCompat.APIMANCERS_DRAINER_BE, ApimancersDrainerBlockEntity::serverTick,
                    (be, player, hand, held) -> be.use(player, held)),
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f).sound(SoundType.METAL).requiresCorrectToolForDrops());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ApimancersDrainerBlockEntity>> APIMANCERS_DRAINER_BE =
            BLOCK_ENTITIES.register("apimancers_drainer", () -> BlockEntityType.Builder.of(ApimancersDrainerBlockEntity::new, APIMANCERS_DRAINER.get()).build(null));
    public static final DeferredItem<BlockItem> APIMANCERS_DRAINER_ITEM = ITEMS.registerSimpleBlockItem(APIMANCERS_DRAINER);

    public static final DeferredHolder<IBeeEffect, NodeEffect.Convert> NODE_TAINTING = EFFECTS.register("node_tainting",
            () -> new NodeEffect.Convert(NodeType.TAINTED, 250, () -> ThaumaturgeCompat.NODE_TAINTING));
    public static final DeferredHolder<IBeeEffect, NodeEffect.Convert> NODE_PURIFYING = EFFECTS.register("node_purifying",
            () -> new NodeEffect.Convert(NodeType.PURE, 250, () -> ThaumaturgeCompat.NODE_PURIFYING));
    public static final DeferredHolder<IBeeEffect, NodeEffect.Convert> NODE_RAVENING = EFFECTS.register("node_ravening",
            () -> new NodeEffect.Convert(NodeType.HUNGRY, 2, () -> ThaumaturgeCompat.NODE_RAVENING));

    static {
        EFFECTS.register("node_recharge", NodeEffect.Recharge::new);
        EFFECTS.register("node_empower", NodeEffect.Empower::new);
        EFFECTS.register("node_repair", NodeEffect.Repair::new);
        CONDITIONS.register(NearNodeCondition.ID, () -> new NearNodeCondition((int) NodeEffect.RANGE));
    }

    private ThaumaturgeCompat() {
    }

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
        NodeConversion.register(bus);
        SelfTest.register();
        CONDITIONS.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        MBItems.addToTab(VIS_AURA_PROVIDER_ITEM);
        MBItems.addToTab(APIMANCERS_DRAINER_ITEM);
        bus.addListener(ThaumaturgeCompat::registerCapabilities);
        // a provider touching a housing on any side boosts it
        HousingBoosts.register((housing, out) -> {
            for (Direction d : Direction.values()) {
                if (housing.getLevel().getBlockEntity(housing.getBlockPos().relative(d)) instanceof VisAuraProviderBlockEntity provider) {
                    provider.addModifiers(out);
                }
            }
        });
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, APIMANCERS_DRAINER_BE.get(), (be, side) -> be);
    }
}
