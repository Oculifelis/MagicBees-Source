package com.magicbees.item;

import com.accbdd.complicated_bees.item.ScoopItem;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Magic Bees' Thaumium and Void scoops: sturdier Complicated Bees scoops that take enchantments. Void metal mends
 * itself: one point of durability per second while carried, like Thaumcraft's void tools.
 */
public class MagicScoopItem extends ScoopItem {
    private final boolean selfRepair;
    private final int enchantability;
    private final TagKey<Item> repairItems;

    public MagicScoopItem(Properties properties, boolean selfRepair, int enchantability, TagKey<Item> repairItems) {
        super(properties);
        this.selfRepair = selfRepair;
        this.enchantability = enchantability;
        this.repairItems = repairItems;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantability;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return repair.is(repairItems);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide() && selfRepair && stack.isDamaged() && entity.tickCount % 20 == 0) stack.setDamageValue(stack.getDamageValue() - 1);
    }
}
