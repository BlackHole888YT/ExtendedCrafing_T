package org.blackhole.extended_crafting_t.tracking;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.blackhole.extended_crafting_t.other.ModBlockItem;
import org.blackhole.extended_crafting_t.other.ModBlocks;
import org.blackhole.extended_crafting_t.other.ModRecipes;
import org.jetbrains.annotations.NotNull;

public class ExtendedCraftingTableRecipe extends CustomRecipe {

    public ExtendedCraftingTableRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, @NotNull Level level) {
        ItemStack firstPlank = ItemStack.EMPTY;
        int count = 0;

        for (ItemStack stack : input.items()) {
            if (!stack.isEmpty()) {
                if (!stack.is(ItemTags.PLANKS)) return false;

                if (firstPlank.isEmpty()) {
                    firstPlank = stack;
                } else if (!ItemStack.isSameItem(firstPlank, stack)) {
                    return false;
                }
                count++;
            }
        }
        return count == 4;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack firstPlank = ItemStack.EMPTY;
        for (ItemStack stack : input.items()) {
            if (!stack.isEmpty()) {
                firstPlank = stack;
                break;
            }
        }

        if (firstPlank.isEmpty()) return ItemStack.EMPTY;

        ResourceLocation plankId = BuiltInRegistries.ITEM.getKey(firstPlank.getItem());
        ItemStack result = new ItemStack(ModBlockItem.EXTENDED_CRAFTING_TABLE_ITEM.get());

        // Записываем только тип дерева в CustomData. Имя предмет рассчитает сам!
        CompoundTag tag = new CompoundTag();
        tag.putString("wood_type", plankId.toString());
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        return result;
    }

    // Вспомогательный метод парсинга имени дерева
    public static String getCapitalizedWoodName(String woodId) {
        String path = woodId.contains(":") ? woodId.split(":")[1] : woodId;

        // Очищаем от технических суффиксов
        path = path.replace("_planks", "")
                .replace("_log", "")
                .replace("_wood", "")
                .replace("_stem", "")
                .replace("_hyphae", "");

        path = path.replace("_", " "); // Заменяем подчеркивания на пробелы

        // Делаем заглавными первые буквы слов (например: dark oak -> Dark Oak)
        StringBuilder capitalized = new StringBuilder();
        for (String word : path.split(" ")) {
            if (!word.isEmpty()) {
                if (!capitalized.isEmpty()) capitalized.append(" ");
                capitalized.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1));
            }
        }
        return capitalized.toString();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.EXTENDED_CRAFTING_TABLE_SPECIAL.get();
    }
}
