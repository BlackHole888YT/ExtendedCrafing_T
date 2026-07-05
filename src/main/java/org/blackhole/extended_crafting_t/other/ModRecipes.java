package org.blackhole.extended_crafting_t.other;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.blackhole.extended_crafting_t.ExtendedCraftingT;
import org.blackhole.extended_crafting_t.recipes.ExtendedShapedRecipe;
import org.blackhole.extended_crafting_t.recipes.ExtendedShapedSerializer;
import org.blackhole.extended_crafting_t.tracking.ExtendedCraftingTableRecipe;

public class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, ExtendedCraftingT.MODID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ExtendedCraftingT.MODID);

    // 1. Регистрируем уникальный ТИП рецепта для кастомных крафтов
    public static final DeferredHolder<RecipeType<?>, RecipeType<ExtendedShapedRecipe>> EXTENDED_CRAFTING_TYPE =
            RECIPE_TYPES.register("extended_crafting", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return "extended_crafting";
                }
            });

    // 2. Регистрируем СЕРИАЛИЗАТОР (парсер JSON) для наших кастомных крафтов
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ExtendedShapedRecipe>> EXTENDED_SHAPED_SERIALIZER =
            RECIPE_SERIALIZERS.register("extended_shaped", ExtendedShapedSerializer::new);

    // !!! ДОБАВЛЕНО: Регистрируем СПЕЦИАЛЬНЫЙ сериализатор для динамического крафта верстака из любых досок !!!
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ExtendedCraftingTableRecipe>> EXTENDED_CRAFTING_TABLE_SPECIAL =
            RECIPE_SERIALIZERS.register("extended_crafting_table_special", () ->
                    new SimpleCraftingRecipeSerializer<>(ExtendedCraftingTableRecipe::new)
            );
}
