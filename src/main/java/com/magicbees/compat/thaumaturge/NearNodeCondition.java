package com.magicbees.compat.thaumaturge;

import com.accbdd.complicated_bees.recipe.mutation.condition.IMutationCondition;
import com.accbdd.complicated_bees.recipe.mutation.condition.MutationCondition;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeLocationIndex;
import com.magicbees.MagicBees;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * Magic Bees' requireResource(airy NODE): the Nexus mutation needed an aura node. Thaumaturge nodes float in the air,
 * so the hive must be within {@code range} blocks of one. JSON: {"magicbees:near_node": {"range": 12}}.
 */
public class NearNodeCondition extends MutationCondition {
    public static final String ID = "near_node";
    private final int range;

    public NearNodeCondition(int range) {
        this.range = Math.max(1, range);
    }

    @Override
    public ResourceLocation getID() {
        return MagicBees.id(ID);
    }

    @Override
    public boolean check(Level level, BlockPos pos) {
        return level instanceof ServerLevel server && NodeLocationIndex.get(server).findNearestAny(pos, range).isPresent();
    }

    @Override
    public Component getDescription() {
        return Component.translatable("gui.magicbees.mutations.near_node", range);
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("range", range);
        return tag;
    }

    @Override
    public IMutationCondition deserialize(CompoundTag tag) {
        return new NearNodeCondition(tag.contains("range") ? tag.getInt("range") : (int) NodeEffect.RANGE);
    }
}
