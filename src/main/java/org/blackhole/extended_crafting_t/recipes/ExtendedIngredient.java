package org.blackhole.extended_crafting_t.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Optional;

public record ExtendedIngredient(
        Ingredient ingredient,
        int count,
        boolean consumable,
        int dealDamage,
        boolean workEnchantments,
        Optional<ItemStack> itemAfterConsume,
        Optional<String> bookId, // Добавлен ID книги
        Optional<Integer> bookLvl // Добавлен уровень книги
) {
    public static final Codec<ExtendedIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(ExtendedIngredient::ingredient),
            Codec.INT.optionalFieldOf("count", 1).forGetter(ExtendedIngredient::count),
            Codec.BOOL.optionalFieldOf("consumable", true).forGetter(ExtendedIngredient::consumable),
            Codec.INT.optionalFieldOf("deal_damage", 0).forGetter(ExtendedIngredient::dealDamage),
            Codec.BOOL.optionalFieldOf("work_enchantments", false).forGetter(ExtendedIngredient::workEnchantments),
            ItemStack.CODEC.optionalFieldOf("item_after_consume").forGetter(ExtendedIngredient::itemAfterConsume),
            Codec.STRING.optionalFieldOf("book_id").forGetter(ExtendedIngredient::bookId),
            Codec.INT.optionalFieldOf("book_lvl").forGetter(ExtendedIngredient::bookLvl)
    ).apply(instance, ExtendedIngredient::new));

    public boolean test(ItemStack stack) {
        if (stack.isEmpty() || !this.ingredient.test(stack)) return false;
        if (stack.getCount() < this.count) return false;

        if (this.dealDamage > 0) {
            int remainingDamage = stack.getMaxDamage() - stack.getDamageValue();
            if (remainingDamage < this.dealDamage) return false;
        }

        // ЖЕСТКАЯ ПРОВЕРКА КНИГ: Проверяем точное совпадение чар и уровня
        if (this.bookId.isPresent() && this.bookLvl.isPresent()) {
            ItemEnchantments enchants = EnchantmentHelper.getEnchantmentsForCrafting(stack);

            // Если на книге больше 1 зачарования или нет чар вообще — отменяем
            if (enchants.size() != 1) return false;

            Optional<Holder<Enchantment>> targetEnch = enchants.keySet().stream().filter(holder ->
                    holder.unwrapKey().isPresent() && holder.unwrapKey().get().location().toString().equals(this.bookId.get())
            ).findFirst();

            // Если нужного чара нет или его уровень не совпадает — отменяем
            if (targetEnch.isEmpty() || enchants.getLevel(targetEnch.get()) != this.bookLvl.get()) {
                return false;
            }
        }

        return true;
    }
}
