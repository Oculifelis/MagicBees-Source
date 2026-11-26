package com.magicbees.item;

import com.accbdd.complicated_bees.item.ScoopItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Magic Bees' Thaumium and Void scoops: sturdier Complicated Bees scoops that take enchantments. Void metal mends
 * itself: one point of durability per second while carried, like Thaumcraft's void tools.
 */
public class MagicScoopItem extends ScoopItem {
    private final boolean selfRepair;

    public MagicScoopItem(Properties properties, boolean selfRepair) {
        super(properties);
        this.selfRepair = selfRepair;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (selfRepair && stack.isDamaged() && entity.tickCount % 20 == 0) stack.setDamageValue(stack.getDamageValue() - 1);
    }
}
