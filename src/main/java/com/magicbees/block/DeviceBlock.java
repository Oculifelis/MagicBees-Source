package com.magicbees.block;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/** One block class for Magic Bees machines: a block entity, an optional server ticker and a use action. */
public class DeviceBlock<T extends BlockEntity> extends BaseEntityBlock {
    public interface Tick<T> {
        void tick(Level level, BlockPos pos, BlockState state, T be);
    }

    public interface Use<T> {
        /** Called on the server; return true when the click was handled. */
        boolean use(T be, Player player, InteractionHand hand, ItemStack held);
    }

    private final Supplier<? extends BlockEntityType<T>> type;
    private final @Nullable Tick<T> ticker;
    private final @Nullable Use<T> user;
    private final VoxelShape shape;

    public DeviceBlock(Properties properties, Supplier<? extends BlockEntityType<T>> type, @Nullable Tick<T> ticker, @Nullable Use<T> user, VoxelShape shape) {
        super(properties);
        this.type = type;
        this.ticker = ticker;
        this.user = user;
        this.shape = shape;
    }

    public DeviceBlock(Properties properties, Supplier<? extends BlockEntityType<T>> type, @Nullable Tick<T> ticker, @Nullable Use<T> user) {
        this(properties, type, ticker, user, Shapes.block());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return type.get().create(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public <E extends BlockEntity> @Nullable BlockEntityTicker<E> getTicker(Level level, BlockState state, BlockEntityType<E> beType) {
        if (level.isClientSide() || ticker == null) {
            return null;
        }
        return createTickerHelper(beType, type.get(), ticker::tick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return act(level, pos, player, hand, stack) ? InteractionResult.SUCCESS : InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return act(level, pos, player, InteractionHand.MAIN_HAND, ItemStack.EMPTY) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @SuppressWarnings("unchecked")
    private boolean act(Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
        if (user == null) {
            return false;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null || be.getType() != type.get()) {
            return false;
        }
        if (!(level instanceof ServerLevel)) {
            return true;
        }
        return user.use((T) be, player, hand, stack);
    }
}
