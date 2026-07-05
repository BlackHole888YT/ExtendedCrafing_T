package org.blackhole.extended_crafting_t.other;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.blackhole.extended_crafting_t.ExtendedCraftingT;
import org.blackhole.extended_crafting_t.block.ExtendedCraftingTableEntity;

public class ModBlockEntities {
    // Создаем регистратор для Block Entity
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ExtendedCraftingT.MODID);

    // Регистрируем наш Block Entity тип и привязываем его к ВАШЕМУ одиночному блоку
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtendedCraftingTableEntity>> EXTENDED_CRAFTING_TABLE_ENTITY =
            BLOCK_ENTITIES.register("extended_crafting_table_entity", () ->
                    BlockEntityType.Builder.of(
                            ExtendedCraftingTableEntity::new,
                            ModBlocks.ExtendedCratingTable.get()
                    ).build(null)
            );
}
