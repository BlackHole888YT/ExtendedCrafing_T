package org.blackhole.extended_crafting_t.recipes;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import org.blackhole.extended_crafting_t.other.ModRecipes;

import java.util.List;
import java.util.Map;

public class ExtendedShapedRecipe implements CraftingRecipe {
    private final List<String> pattern;
    private final Map<String, ExtendedIngredient> key;
    private final AdditionalConditions conditions;
    private final ItemStack result;

    public ExtendedShapedRecipe(List<String> pattern, Map<String, ExtendedIngredient> key, AdditionalConditions conditions, ItemStack result) {
        this.pattern = pattern;
        this.key = key;
        this.conditions = conditions;
        this.result = result;
    }

    public List<String> getPattern() { return this.pattern; }
    public Map<String, ExtendedIngredient> getKey() { return this.key; }
    public AdditionalConditions getConditions() { return this.conditions; }
    public ItemStack getResult() { return this.result; }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.EXTENDED_CRAFTING_TYPE.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.EXTENDED_SHAPED_SERIALIZER.get();
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int recipeHeight = pattern.size();
        int recipeWidth = pattern.get(0).length();

        if (input.width() < recipeWidth || input.height() < recipeHeight) {
            return false;
        }

        return checkGridMatches(input);
    }

    public boolean checkGridMatches(CraftingInput input) {
        int recipeHeight = pattern.size();
        int recipeWidth = pattern.get(0).length();

        for (int i = 0; i <= input.width() - recipeWidth; ++i) {
            for (int j = 0; j <= input.height() - recipeHeight; ++j) {
                if (checkPattern(input, i, j, true)) return true;
                if (checkPattern(input, i, j, false)) return true;
            }
        }
        return false;
    }

    private boolean checkPattern(CraftingInput input, int startX, int startY, boolean mirror) {
        int recipeHeight = pattern.size();
        int recipeWidth = pattern.get(0).length();

        for (int x = 0; x < input.width(); ++x) {
            for (int y = 0; y < input.height(); ++y) {
                int dx = x - startX;
                int dy = y - startY;
                ExtendedIngredient ingredient = null;

                if (dx >= 0 && dy >= 0 && dx < recipeWidth && dy < recipeHeight) {
                    int charX = mirror ? recipeWidth - 1 - dx : dx;
                    String symbol = String.valueOf(pattern.get(dy).charAt(charX));
                    ingredient = key.get(symbol);
                }

                int inputIdx = y * input.width() + x;
                ItemStack stack = input.getItem(inputIdx);

                if (ingredient == null) {
                    if (!stack.isEmpty()) return false;
                } else {
                    if (!ingredient.test(stack)) return false;
                }
            }
        }
        return true;
    }

    // --- РАЗДЕЛЬНЫЕ МЕТОДЫ ПРОВЕРОК УСЛОВИЙ (Для правильной подсветки в GUI) ---

    public boolean testDimension(Level level) {
        if (conditions.requiredDimension().isEmpty()) return true;
        String dim = conditions.requiredDimension().get();
        return level.dimension().location().getPath().equalsIgnoreCase(dim);
    }

    public boolean testXpCost(Player player) {
        if (conditions.xpCost().isEmpty()) return true;
        return player.totalExperience >= conditions.xpCost().get().value();
    }

    public boolean testXpLvl(Player player) {
        if (conditions.xpLvl().isEmpty()) return true;
        return player.experienceLevel >= conditions.xpLvl().get().value();
    }

    public boolean testHp(Player player) {
        if (conditions.requiredHp().isEmpty()) return true;
        float hp = player.getHealth();
        float maxHp = player.getMaxHealth();
        String hpCond = conditions.requiredHp().get();

        if (hpCond.equalsIgnoreCase("max")) {
            return hp >= maxHp;
        } else if (hpCond.equalsIgnoreCase("min")) {
            return hp <= 1.0F;
        } else if (hpCond.equalsIgnoreCase("half")) {
            float half = maxHp / 2.0F;
            return Math.abs(hp - half) <= 1.5F;
        } else if (hpCond.endsWith("%")) {
            float pct = Float.parseFloat(hpCond.replace("%", "")) / 100.0F;
            return hp >= maxHp * pct;
        } else {
            float val = Float.parseFloat(hpCond);
            return hp >= val;
        }
    }

    public boolean testTime(Level level) {
        if (conditions.requiredTime().isEmpty()) return true;
        AdditionalConditions.TimeCondition timeCond = conditions.requiredTime().get();
        long time = level.getDayTime() % 24000L;

        long targetTime;
        try {
            // Пытаемся прочитать значение как точное число в тиках (например, "12500")
            targetTime = Long.parseLong(timeCond.value()) % 24000L;
        } catch (NumberFormatException e) {
            // Если введена строка, сопоставляем её с текстовыми пресетами
            targetTime = switch (timeCond.value().toLowerCase()) {
                case "sunrise" -> 0L;
                case "day", "noon" -> 6000L;
                case "sunset" -> 12000L;
                case "night" -> 14000;
                case "midnight" -> 18000L;
                default -> 6000L;
            };
        }

        long diff = Math.abs(time - targetTime);
        long minDiff = Math.min(diff, 24000L - diff);
        return minDiff <= timeCond.interval();
    }

    public boolean testAdvancement(Player player) {
        if (conditions.requiredAdvancement().isEmpty()) return true;
        if (player instanceof ServerPlayer serverPlayer) {
            AdvancementHolder adv = serverPlayer.server.getAdvancements().get(ResourceLocation.parse(conditions.requiredAdvancement().get()));
            return adv != null && serverPlayer.getAdvancements().getOrStartProgress(adv).isDone();
        }
        // На клиенте возвращаем true по умолчанию, так как сервер все равно перепроверит при крафте
        return true;
    }

    // Общий метод проверки (для сервера) использует новые точечные методы
    public boolean testPlayerAndWorldConditions(Player player, Level level) {
        return testDimension(level)
                && testXpCost(player)
                && testXpLvl(player)
                && testHp(player)
                && testTime(level)
                && testAdvancement(player);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack out = this.result.copy();
        CustomData customData = out.get(DataComponents.CUSTOM_DATA);
        if (customData != null && customData.copyTag().contains("book_id")) {
            CompoundTag tag = customData.copyTag();
            String bookId = tag.getString("book_id");
            int lvl = Integer.parseInt(tag.getString("book_lvl"));

            out = new ItemStack(net.minecraft.world.item.Items.ENCHANTED_BOOK, out.getCount());

            Holder<net.minecraft.world.item.enchantment.Enchantment> enchHolder = registries.lookupOrThrow(Registries.ENCHANTMENT)
                    .get(net.minecraft.resources.ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse(bookId)))
                    .orElseThrow();

            ItemEnchantments.Mutable mutableEnchants = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            mutableEnchants.set(enchHolder, lvl);

            EnchantmentHelper.setEnchantments(out, mutableEnchants.toImmutable());

            tag.remove("book_id");
            tag.remove("book_lvl");
            if (tag.isEmpty()) {
                out.remove(DataComponents.CUSTOM_DATA);
            } else {
                out.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        }
        return out;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }
}
