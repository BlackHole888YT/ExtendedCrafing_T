package org.blackhole.extended_crafting_t.block;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import org.blackhole.extended_crafting_t.tracking.ExtendedCraftingTableRecipe;

public class ExtendedCraftingTableItem extends BlockItem {

    public ExtendedCraftingTableItem(Block block, Properties properties) {
        super(block, properties);
    }

    // ИСПРАВЛЕНО: Этот метод ПРИНИМАЕТ ItemStack и вызывается игрой для отрисовки имени в инвентаре!
    @Override
    public Component getName(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);

        if (customData != null && customData.copyTag().contains("wood_type")) {
            String woodType = customData.copyTag().getString("wood_type");
            // Извлекаем имя дерева (например, "Birch")
            String woodName = ExtendedCraftingTableRecipe.getCapitalizedWoodName(woodType);

            // Возвращаем динамический транслейт-компонент с аргументом без курсива наковальни
            return Component.translatable("ect_name", woodName.toUpperCase());
        }

        return super.getName(stack); // Откат на имя по умолчанию, если это пустой верстак без NBT
    }
}