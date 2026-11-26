package com.magicbees.item;

import com.magicbees.MBComponents;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Mysterious Magnet (ItemMysteriousMagnet): sneak-use toggles it; while on, it pulls dropped items from
 * 3 + 0.75 x level blocks around the holder, and from level 7 also turns stuck arrows into items.
 */
public class MysteriousMagnetItem extends Item {
    // Config.magnetBaseRange, magnetLevelMultiplier
    private static final float BASE_RANGE = 3f;
    private static final float PER_LEVEL = 0.75f;
    private static final float FUDGE = 0.2f;

    public MysteriousMagnetItem(Properties properties) {
        super(properties);
    }

    public static int level(ItemStack stack) {
        return stack.getOrDefault(MBComponents.MAGNET_LEVEL.get(), 0);
    }

    public static boolean active(ItemStack stack) {
        return stack.has(MBComponents.MAGNET_ACTIVE.get());
    }

    public static void setActive(ItemStack stack, boolean on) {
        if (on) {
            stack.set(MBComponents.MAGNET_ACTIVE.get(), Unit.INSTANCE);
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        } else {
            stack.remove(MBComponents.MAGNET_ACTIVE.get());
            stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);
        setActive(stack, !active(stack));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, owner, slot);
        if (owner instanceof Player player && active(stack)) pull(stack, level, player);
    }

    /** onWornTick: pulls items (and, from level 7, stuck arrows) toward the player. */
    public static void pull(ItemStack stack, ServerLevel level, Player player) {
        if (player.isSpectator()) return;
        float radius = BASE_RANGE + PER_LEVEL * level(stack);
        AABB box = player.getBoundingBox().inflate(radius);
        if (level(stack) >= 7) {
            for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, box, a -> a.getOwner() != player)) {
                if (arrow.pickup == AbstractArrow.Pickup.ALLOWED || level.getRandom().nextFloat() < 0.3f) {
                    level.addFreshEntity(new ItemEntity(level, arrow.getX(), arrow.getY(), arrow.getZ(), arrow.getPickupItemStackOrigin().copy()));
                }
                arrow.discard();
            }
        }
        box = box.deflate(FUDGE);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box, e -> e.getAge() >= 10)) {
            Vec3 to = player.position().add(0, -1, 0).subtract(item.position());
            double length = to.length() * 2;
            if (length < 1.2) continue;
            Vec3 motion = to.scale(1 / length).add(player.getDeltaMovement().scale(0.5));
            if (item.horizontalCollision) motion = motion.add(0, 1, 0);
            item.setDeltaMovement(motion);
            item.hurtMarked = true;
            if (level.getRandom().nextFloat() < 0.2f) {
                level.playSound(null, item.getX(), item.getY(), item.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS,
                        0.6f, 0.85f - level.getRandom().nextFloat() * 0.3f);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable(active(stack) ? "tooltip.magicbees.magnet_active" : "tooltip.magicbees.magnet_inactive",
                level(stack)));
    }
}
