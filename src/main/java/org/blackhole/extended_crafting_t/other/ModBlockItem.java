package org.blackhole.extended_crafting_t.other;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.blackhole.extended_crafting_t.ExtendedCraftingT;
import org.blackhole.extended_crafting_t.block.ExtendedCraftingTableItem;

import static org.blackhole.extended_crafting_t.other.ModBlocks.ExtendedCratingTable;

public class ModBlockItem {
    // Добавляем регистратор предметов (ITEMS)
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ExtendedCraftingT.MODID);

    public static final DeferredItem<BlockItem> EXTENDED_CRAFTING_TABLE_ITEM =
            ITEMS.register("extended_crafting_table", () ->
                    new ExtendedCraftingTableItem(ExtendedCratingTable.get(), new Item.Properties())
            );
}
