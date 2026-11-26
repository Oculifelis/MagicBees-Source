package com.magicbees;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

/**
 * The Crumbling and Transmuting block tables as block data maps, so datapacks can change them
 * ({@code data/magicbees/data_maps/block/crumbling.json} and {@code transmuting.json}). Target blocks are ids looked
 * up when used, so a rule for a mod that is not installed does nothing. Housing boosts from other mods' blocks and
 * items read their numbers from {@code housing_accelerator.json} (block) and {@code mutation_catalyst.json} (item).
 */
public final class MBDataMaps {
    /** What a block crumbles into. */
    public record Crumble(String result) {
        public static final Codec<Crumble> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("result").forGetter(Crumble::result)
        ).apply(i, Crumble::new));

        public Optional<Block> block() {
            return MagicBees.block(result);
        }
    }

    /**
     * One transmute rule: the block becomes {@code into} in a biome in any of {@code anyBiome} (empty means every
     * biome) and in none of {@code notBiome}. {@code where} names the biome group on the JEI page.
     */
    public record Transmute(String into, List<TagKey<Biome>> anyBiome, List<TagKey<Biome>> notBiome, String where) {
        public static final Codec<Transmute> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("into").forGetter(Transmute::into),
                TagKey.codec(Registries.BIOME).listOf().optionalFieldOf("any_biome", List.of()).forGetter(Transmute::anyBiome),
                TagKey.codec(Registries.BIOME).listOf().optionalFieldOf("not_biome", List.of()).forGetter(Transmute::notBiome),
                Codec.STRING.optionalFieldOf("where", "any").forGetter(Transmute::where)
        ).apply(i, Transmute::new));

        public boolean matches(Holder<Biome> biome) {
            return (anyBiome.isEmpty() || anyBiome.stream().anyMatch(biome::is)) && notBiome.stream().noneMatch(biome::is);
        }

        public Optional<Block> block() {
            return MagicBees.block(into);
        }
    }

    /**
     * A powered block that speeds up a housing it touches: the productivity multiplier and the power it pays per work
     * cycle. A block in the {@code magicbees:housing_accelerators} tag with no entry uses the defaults.
     */
    public record Accelerator(float productivity, int cost) {
        public static final Accelerator DEFAULT = new Accelerator(1.5f, 30);
        public static final Codec<Accelerator> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.FLOAT.optionalFieldOf("productivity", DEFAULT.productivity).forGetter(Accelerator::productivity),
                Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("cost", DEFAULT.cost).forGetter(Accelerator::cost)
        ).apply(i, Accelerator::new));
    }

    /**
     * An item that raises a housing's mutation chance from an item frame on it. An item in the
     * {@code magicbees:mutation_catalysts} tag with no entry uses the default.
     */
    public record Catalyst(float mutation) {
        public static final Catalyst DEFAULT = new Catalyst(1.5f);
        public static final Codec<Catalyst> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.FLOAT.optionalFieldOf("mutation", DEFAULT.mutation).forGetter(Catalyst::mutation)
        ).apply(i, Catalyst::new));
    }

    public static final DataMapType<Block, Accelerator> HOUSING_ACCELERATOR = DataMapType.builder(
            MagicBees.id("housing_accelerator"), Registries.BLOCK, Accelerator.CODEC).build();

    public static final DataMapType<Item, Catalyst> MUTATION_CATALYST = DataMapType.builder(
            MagicBees.id("mutation_catalyst"), Registries.ITEM, Catalyst.CODEC).build();

    public static final DataMapType<Block, Crumble> CRUMBLING = DataMapType.builder(
            MagicBees.id("crumbling"), Registries.BLOCK, Crumble.CODEC).synced(Crumble.CODEC, false).build();

    public static final DataMapType<Block, List<Transmute>> TRANSMUTING = DataMapType.builder(
            MagicBees.id("transmuting"), Registries.BLOCK, Transmute.CODEC.listOf())
            .synced(Transmute.CODEC.listOf(), false).build();

    /** The block's crumble entry, or null. */
    public static Crumble crumble(BlockState state) {
        return state.getBlock().builtInRegistryHolder().getData(CRUMBLING);
    }

    /** The block's transmute rules in file order (empty when it has none). */
    public static List<Transmute> transmute(BlockState state) {
        List<Transmute> rules = state.getBlock().builtInRegistryHolder().getData(TRANSMUTING);
        return rules == null ? List.of() : rules;
    }

    /** The block's accelerator entry, or null (a tagged block with no entry uses {@link Accelerator#DEFAULT}). */
    public static Accelerator accelerator(Block block) {
        return block.builtInRegistryHolder().getData(HOUSING_ACCELERATOR);
    }

    /** The item's catalyst entry, or null (a tagged item with no entry uses {@link Catalyst#DEFAULT}). */
    public static Catalyst catalyst(Item item) {
        return item.builtInRegistryHolder().getData(MUTATION_CATALYST);
    }

    public static void init(IEventBus modBus) {
        modBus.addListener((RegisterDataMapTypesEvent event) -> {
            event.register(CRUMBLING);
            event.register(TRANSMUTING);
            event.register(HOUSING_ACCELERATOR);
            event.register(MUTATION_CATALYST);
        });
        DataMapSelfTest.register();
    }

    private MBDataMaps() {
    }
}
