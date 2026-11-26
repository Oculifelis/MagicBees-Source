package com.magicbees;

import com.mojang.logging.LogUtils;
import com.accbdd.complicated_bees.registry.SpeciesRegistration;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Magic Bees (GTNH 2.10.13), ported from the Forestry addon to Complicated Bees on NeoForge 1.21.1.
 * Species, combs, flowers, mutations, centrifuge recipes and nests are datapack JSON made by tools/gen_data.py;
 * this class only registers what Complicated Bees keeps in code registries (items, bee effects, mutation conditions).
 */
@Mod(MagicBees.MODID)
public final class MagicBees {
    public static final String MODID = "magicbees";
    public static final Logger LOG = LogUtils.getLogger();

    public MagicBees(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, MBConfig.SPEC);
        MBComponents.register(modBus);
        MBItems.register(modBus);
        MBBlocks.register(modBus);
        MBEffects.register(modBus);
        MBConditions.register(modBus);
        MBDataMaps.init(modBus);
        if (ModList.get().isLoaded("thaumaturge")) com.magicbees.compat.thaumaturge.ThaumaturgeCompat.register(modBus);
        if (ModList.get().isLoaded("neovitae")) com.magicbees.compat.neovitae.NeoVitaeCompat.register(modBus);
        com.magicbees.compat.HousingCompat.register();
        if (ModList.get().isLoaded("curios")) com.magicbees.compat.curios.CuriosCompat.register(modBus);
        NeoForge.EVENT_BUS.addListener(MagicBees::logLoadedData);
    }

    /** One line at server start saying what of the datapack loaded (and whether the Thaumaturge overlay applied). */
    private static void logLoadedData(ServerStartedEvent event) {
        var species = event.getServer().registryAccess().registryOrThrow(SpeciesRegistration.SPECIES_REGISTRY_KEY);
        long ours = species.keySet().stream().filter(k -> k.getNamespace().equals(MODID)).count();
        int brainy = species.get(id("brainy")) == null ? -1 : species.get(id("brainy")).getSpecialtyProducts().size();
        LOG.info("Magic Bees data: {} species; Brainy has {} specialty products (1 with the Thaumaturge overlay)", ours, brainy);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    /** A block by id, empty when the id is bad or the block's mod is not installed. */
    public static java.util.Optional<net.minecraft.world.level.block.Block> block(String id) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        return key == null ? java.util.Optional.empty() : net.minecraft.core.registries.BuiltInRegistries.BLOCK.getOptional(key);
    }
}
