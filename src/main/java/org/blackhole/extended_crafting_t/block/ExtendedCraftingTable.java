package org.blackhole.extended_crafting_t.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.blackhole.extended_crafting_t.logic.ExtendedCraftingTableMenu;
import org.blackhole.extended_crafting_t.other.ModBlockItem;
import org.blackhole.extended_crafting_t.other.ModBlocks;
import org.blackhole.extended_crafting_t.tracking.ExtendedCraftingTableRecipe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ExtendedCraftingTable extends BaseEntityBlock {
    public static final MapCodec<ExtendedCraftingTable> CODEC = simpleCodec(ExtendedCraftingTable::new);
    private static final Component CONTAINER_TITLE = Component.translatable("container.extended_crafting");
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public ExtendedCraftingTable(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExtendedCraftingTableEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ExtendedCraftingTableEntity tableEntity) {
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData != null && customData.copyTag().contains("wood_type")) {
                tableEntity.setWoodType(customData.copyTag().getString("wood_type"));
            }
        }
    }

    // Клик колесиком мыши (Pick Block) в креативе
    @Override
    public @NotNull ItemStack getCloneItemStack(LevelReader level, @NotNull BlockPos pos, @NotNull BlockState state) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ExtendedCraftingTableEntity tableEntity) {
            ItemStack stack = new ItemStack(ModBlockItem.EXTENDED_CRAFTING_TABLE_ITEM.get());

            // Сохраняем только тип дерева, имя предмет сгенерирует сам в инвентаре
            CompoundTag tag = new CompoundTag();
            tag.putString("wood_type", tableEntity.getWoodType());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            return stack;
        }
        return super.getCloneItemStack(level, pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            player.openMenu(state.getMenuProvider(level, pos));
            return InteractionResult.CONSUME;
        }
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (containerId, playerInventory, playerEntity) ->
                        new ExtendedCraftingTableMenu(containerId, playerInventory, ContainerLevelAccess.create(level, pos)),
                CONTAINER_TITLE
        );
    }
}