package com.magicbees;

import com.magicbees.block.DeviceBlock;
import com.magicbees.block.EffectJarBlockEntity;
import com.magicbees.block.EnchantedEarthBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Magic Bees' own blocks (the Thaumaturge machines live in compat/thaumaturge). */
public final class MBBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MagicBees.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MagicBees.MODID);

    public static final DeferredBlock<EnchantedEarthBlock> ENCHANTED_EARTH = BLOCKS.registerBlock("enchanted_earth", EnchantedEarthBlock::new,
            p -> p.mapColor(MapColor.DIRT).strength(0.5f).sound(SoundType.GRAVEL));
    public static final DeferredItem<BlockItem> ENCHANTED_EARTH_ITEM = MBItems.ITEMS.registerSimpleBlockItem(ENCHANTED_EARTH);

    public static final DeferredBlock<DeviceBlock<EffectJarBlockEntity>> EFFECT_JAR = BLOCKS.registerBlock("effect_jar",
            p -> new DeviceBlock<>(p, MBBlocks.EFFECT_JAR_BE, EffectJarBlockEntity::serverTick,
                    (be, player, hand, held) -> be.use(player, held), net.minecraft.world.level.block.Block.box(3, 0, 3, 13, 14, 13)),
            p -> p.mapColor(MapColor.NONE).strength(0.3f).sound(SoundType.GLASS).noOcclusion());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EffectJarBlockEntity>> EFFECT_JAR_BE =
            BLOCK_ENTITIES.register("effect_jar", () -> new BlockEntityType<>(EffectJarBlockEntity::new, EFFECT_JAR.get()));
    public static final DeferredItem<BlockItem> EFFECT_JAR_ITEM = MBItems.ITEMS.registerSimpleBlockItem(EFFECT_JAR);

    private MBBlocks() {
    }

    static void register(IEventBus bus) {
        BLOCKS.register(bus);
        BLOCK_ENTITIES.register(bus);
        MBItems.addToTab(ENCHANTED_EARTH_ITEM);
        MBItems.addToTab(EFFECT_JAR_ITEM);
    }
}
