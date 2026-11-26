package com.magicbees.item;

import com.accbdd.complicated_bees.bees.GeneticHelper;
import com.accbdd.complicated_bees.bees.effect.IBeeEffect;
import com.accbdd.complicated_bees.bees.gene.GeneEffect;
import com.accbdd.complicated_bees.item.DroneItem;
import com.magicbees.MBBlocks;
import com.magicbees.MBComponents;
import com.magicbees.block.EffectJarBlockEntity;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

/**
 * Bee Ring (ItemBeeRing): an Effect Jar worn on the hand. Use it with drones in the off hand to load them; sneak-use
 * with an empty off hand takes the waiting drones back. Worn in the Curios ring slot (or carried, without Curios), the
 * working bee's effect runs around the wearer and it ages like a jar's bee.
 */
public class BeeRingItem extends Item {
    /** With Curios the ring only works from the ring slot (curioTick); without it, anywhere in the inventory. */
    private static final boolean CURIOS = ModList.get().isLoaded("curios");

    public BeeRingItem(Properties properties) {
        super(properties);
    }

    private static ItemStack slot(ItemStack ring, int slot) {
        ItemContainerContents bees = ring.getOrDefault(MBComponents.RING_BEES.get(), ItemContainerContents.EMPTY);
        return slot < bees.getSlots() ? bees.getStackInSlot(slot).copy() : ItemStack.EMPTY;
    }

    private static void store(ItemStack ring, ItemStack drones, ItemStack active) {
        ring.set(MBComponents.RING_BEES.get(), ItemContainerContents.fromItems(List.of(drones, active)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack ring = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(ring);
        ItemStack offhand = player.getOffhandItem();
        if (level.isClientSide()) return InteractionResultHolder.success(ring);
        ItemStack drones = slot(ring, 0);
        ItemStack active = slot(ring, 1);
        if (offhand.getItem() instanceof DroneItem) {
            if (drones.isEmpty()) {
                drones = offhand.split(offhand.getCount());
            } else if (ItemStack.isSameItemSameComponents(drones, offhand)) {
                int moved = Math.min(offhand.getCount(), drones.getMaxStackSize() - drones.getCount());
                drones.grow(moved);
                offhand.shrink(moved);
            } else {
                return InteractionResultHolder.fail(ring);
            }
            store(ring, drones, active);
        } else if (player.isShiftKeyDown() && offhand.isEmpty() && !drones.isEmpty()) {
            player.getInventory().placeItemBackInInventory(drones);
            store(ring, ItemStack.EMPTY, active);
        }
        return InteractionResultHolder.success(ring);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity owner, int slot, boolean selected) {
        super.inventoryTick(stack, world, owner, slot, selected);
        if (!(world instanceof ServerLevel level)) return;
        if (owner instanceof Player player && !CURIOS) wornTick(stack, level, player);
    }

    /** onWornTick: start a bee from the drones if none works, run its effect, age it. */
    public static void wornTick(ItemStack ring, ServerLevel level, Player player) {
        ItemStack drones = slot(ring, 0);
        ItemStack active = slot(ring, 1);
        if (active.isEmpty()) {
            if (drones.isEmpty()) return;
            active = drones.split(1);
            store(ring, drones, active);
            ring.set(MBComponents.RING_LIFE.get(), EffectJarBlockEntity.LIFE);
        }
        if (GeneticHelper.getGeneValue(active, GeneEffect.ID, true) instanceof IBeeEffect effect) {
            // RingHousing: the player stands in for the hive, so effects centre on them
            EffectJarBlockEntity housing = new EffectJarBlockEntity(player.blockPosition(), MBBlocks.EFFECT_JAR.get().defaultBlockState());
            housing.setLevel(level);
            effect.runEffect(housing, active, (int) (level.getGameTime() % EffectJarBlockEntity.AGE_INTERVAL));
        }
        if (level.getGameTime() % EffectJarBlockEntity.AGE_INTERVAL == 0) {
            int life = ring.getOrDefault(MBComponents.RING_LIFE.get(), 0) - 1;
            if (life <= 0) {
                store(ring, drones, ItemStack.EMPTY);
                ring.remove(MBComponents.RING_LIFE.get());
            } else {
                ring.set(MBComponents.RING_LIFE.get(), life);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> builder, TooltipFlag flag) {
        ItemStack drones = slot(stack, 0);
        ItemStack active = slot(stack, 1);
        builder.add(active.isEmpty() ? Component.translatable("tooltip.magicbees.bee_ring.idle")
                : Component.translatable("tooltip.magicbees.bee_ring.active", active.getHoverName(),
                        stack.getOrDefault(MBComponents.RING_LIFE.get(), 0) * 100 / EffectJarBlockEntity.LIFE));
        if (!drones.isEmpty()) builder.add(Component.translatable("tooltip.magicbees.bee_ring.drones", drones.getCount()));
    }
}
