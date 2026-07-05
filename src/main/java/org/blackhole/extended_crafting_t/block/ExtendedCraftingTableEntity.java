package org.blackhole.extended_crafting_t.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.blackhole.extended_crafting_t.other.ModBlockEntities;

public class ExtendedCraftingTableEntity extends BlockEntity {
    private String woodType = "minecraft:oak_planks"; // Дефолтный откат на дуб

    public ExtendedCraftingTableEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EXTENDED_CRAFTING_TABLE_ENTITY.get(), pos, state);
    }

    public String getWoodType() {
        return this.woodType;
    }

    public void setWoodType(String woodType) {
        this.woodType = woodType;
        this.setChanged();
        if (this.level != null) {
            // Отправляем пакет обновления блока на клиент для перерисовки текстуры
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("wood_type", this.woodType);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("wood_type")) {
            this.woodType = tag.getString("wood_type");
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putString("wood_type", this.woodType);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
