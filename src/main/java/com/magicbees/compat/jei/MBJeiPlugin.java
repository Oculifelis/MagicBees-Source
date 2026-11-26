package com.magicbees.compat.jei;

import java.util.ArrayList;
import java.util.List;

import com.magicbees.MBBlocks;
import com.magicbees.MBItems;
import com.magicbees.MagicBees;
import com.magicbees.MBDataMaps;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

/**
 * JEI: a Bee Block Effects category for the Transmuting and Crumbling effects' block conversions (Complicated Bees'
 * own plugin already shows centrifuging, produce, mutations and flowers for our data), and information pages for the
 * devices and tools whose use is not a recipe. Items from optional modules are looked up by id, so nothing here loads
 * Thaumaturge, Neo Vitae or Curios classes. Loaded only with JEI.
 */
@JeiPlugin
public final class MBJeiPlugin implements IModPlugin {
    /** One block conversion; {@code effect} is the bee effect id, {@code where} a lang key suffix for the biome. */
    public record BlockEffect(String effect, List<Block> from, Block into, String where) {}

    static final IRecipeType<BlockEffect> BLOCK_EFFECTS = IRecipeType.create(MagicBees.id("block_effects"), BlockEffect.class);

    private static final String[] INFO = {"effect_jar", "enchanted_earth", "vis_aura_provider", "apimancers_drainer",
            "bee_ring", "mysterious_magnet", "moon_dial", "blood_frame", "blood_soaked_frame", "frenzied_frame", "aromatic_lump"};

    @Override
    public Identifier getPluginUid() {
        return MagicBees.id("jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new BlockEffectCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<BlockEffect> effects = new ArrayList<>();
        for (Block from : BuiltInRegistries.BLOCK) {
            var holder = from.builtInRegistryHolder();
            var rules = holder.getData(MBDataMaps.TRANSMUTING);
            if (rules != null) for (MBDataMaps.Transmute rule : rules) {
                rule.block().ifPresent(into -> effects.add(new BlockEffect("transmuting", List.of(from), into, rule.where())));
            }
            MBDataMaps.Crumble crumble = holder.getData(MBDataMaps.CRUMBLING);
            if (crumble != null) crumble.block().ifPresent(into -> effects.add(new BlockEffect("crumbling", List.of(from), into, "any")));
        }
        effects.sort((a, b) -> a.effect().compareTo(b.effect()));
        registration.addRecipes(BLOCK_EFFECTS, effects);

        for (String name : INFO) {
            BuiltInRegistries.ITEM.getOptional(MagicBees.id(name)).ifPresent(item ->
                    registration.addIngredientInfo(item, Component.translatable("jei.magicbees.info." + name)));
        }
        // other mods' items that do something for bees: a page for each one with a jei.magicbees.info.<ns>.<path> line
        net.minecraft.locale.Language lang = net.minecraft.locale.Language.getInstance();
        for (var item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            String key = "jei.magicbees.info." + id.getNamespace() + "." + id.getPath();
            if (!id.getNamespace().equals(MagicBees.MODID) && lang.has(key)) {
                registration.addIngredientInfo(item, Component.translatable(key));
            }
        }
    }

    private static final class BlockEffectCategory extends AbstractRecipeCategory<BlockEffect> {
        BlockEffectCategory(IGuiHelper gui) {
            super(BLOCK_EFFECTS, Component.translatable("jei.magicbees.block_effects"),
                    gui.createDrawableItemLike(MBBlocks.EFFECT_JAR_ITEM.get()), 140, 46);
        }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, BlockEffect recipe, IFocusGroup focuses) {
            var input = builder.addInputSlot(30, 4).setStandardSlotBackground();
            recipe.from().forEach(input::addItemLike);
            builder.addOutputSlot(92, 4).setOutputSlotBackground().addItemLike(recipe.into());
        }

        @Override
        public void createRecipeExtras(IRecipeExtrasBuilder builder, BlockEffect recipe, IFocusGroup focuses) {
            builder.addRecipeArrow().setPosition(58, 5);
            builder.addText(Component.translatable("effect.complicated_bees.magicbees:" + recipe.effect()), 140, 10)
                    .setPosition(0, 26).setColor(0xFF404040)
                    .setTextAlignment(HorizontalAlignment.CENTER);
            builder.addText(Component.translatable("jei.magicbees.block_effects." + recipe.where()), 140, 10)
                    .setPosition(0, 36).setColor(0xFF707070)
                    .setTextAlignment(HorizontalAlignment.CENTER);
        }
    }
}
