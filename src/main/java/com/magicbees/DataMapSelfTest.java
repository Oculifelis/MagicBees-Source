package com.magicbees;

import java.util.List;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * Checks the Crumbling and Transmuting data maps once the dev server has started, when the JVM has
 * {@code -Dmagicbees.selftest=true} (only the Gradle server run sets it). Shipped entries must load, and the world pack
 * {@code tools/dev/qa_pack} (copied in by {@code tools/dev/boot_run.sh}) must override one entry: gravel crumbles into
 * red sand instead of sand, and add a housing accelerator (lodestone) and a mutation catalyst (amethyst shard). Without that pack the shipped value (sand) is expected. Logs one {@code SELFTEST PASS} or
 * {@code SELFTEST FAIL} line each.
 */
final class DataMapSelfTest {
    private DataMapSelfTest() {
    }

    static void register() {
        if (Boolean.getBoolean("magicbees.selftest")) {
            NeoForge.EVENT_BUS.addListener((ServerStartedEvent e) -> run(
                    e.getServer().getPackRepository().getSelectedIds().contains("file/qa_pack")));
        }
    }

    private static void run(boolean qaPack) {
        try {
            crumbles("stone", "minecraft:cobblestone");
            crumbles("cracked_stone_bricks", "minecraft:mossy_stone_bricks");
            crumbles("gravel", qaPack ? "minecraft:red_sand" : "minecraft:sand");
            List<MBDataMaps.Transmute> sand = MBDataMaps.transmute(block("sand").defaultBlockState());
            report("transmuting sand", sand.size() == 1 && sand.get(0).into().equals("minecraft:sandstone")
                    && sand.get(0).anyBiome().size() == 2, String.valueOf(sand));
            List<MBDataMaps.Transmute> stone = MBDataMaps.transmute(block("stone").defaultBlockState());
            report("transmuting stone rules", stone.size() == 2 && stone.get(0).notBiome().size() == 1, String.valueOf(stone));
            report("transmuting dirt has none", MBDataMaps.transmute(block("dirt").defaultBlockState()).isEmpty(), "");
            MBDataMaps.Accelerator acc = MBDataMaps.accelerator(block("lodestone"));
            report("housing accelerator lodestone", qaPack ? acc != null && acc.productivity() == 2.0f && acc.cost() == 10
                    : acc == null, String.valueOf(acc));
            report("housing accelerator dirt has none", MBDataMaps.accelerator(block("dirt")) == null, "");
            MBDataMaps.Catalyst cat = MBDataMaps.catalyst(net.minecraft.world.item.Items.AMETHYST_SHARD);
            report("mutation catalyst amethyst", qaPack ? cat != null && cat.mutation() == 1.25f : cat == null, String.valueOf(cat));
        } catch (Exception ex) {
            MagicBees.LOG.error("SELFTEST FAIL data maps crashed", ex);
        }
    }

    private static void crumbles(String from, String expect) {
        MBDataMaps.Crumble c = MBDataMaps.crumble(block(from).defaultBlockState());
        report("crumbling " + from + " -> " + expect, c != null && c.result().equals(expect), String.valueOf(c));
    }

    private static Block block(String path) {
        return MagicBees.block("minecraft:" + path).orElseThrow();
    }

    private static void report(String name, boolean ok, String got) {
        if (ok) MagicBees.LOG.info("SELFTEST PASS {}", name);
        else MagicBees.LOG.error("SELFTEST FAIL {} (got {})", name, got);
    }
}
