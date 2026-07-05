package org.blackhole.extended_crafting_t.logic;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.blackhole.extended_crafting_t.other.ModBlocks;
import org.blackhole.extended_crafting_t.other.ModMenus;
import org.blackhole.extended_crafting_t.other.ModRecipes;
import org.blackhole.extended_crafting_t.recipes.ExtendedIngredient;
import org.blackhole.extended_crafting_t.recipes.ExtendedShapedRecipe;

import java.util.Optional;

import static org.blackhole.extended_crafting_t.other.ModBlocks.ExtendedCratingTable;

public class ExtendedCraftingTableMenu extends CraftingMenu {
    private final Player player;
    private final ContainerLevelAccess access;
    private ExtendedShapedRecipe activeCustomRecipe = null;

    public ExtendedCraftingTableMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(containerId, playerInventory, access);
        this.player = playerInventory.player;
        this.access = access;
        replaceResultSlot();
        replaceCraftingSlots();
    }

    public ExtendedCraftingTableMenu(int containerId, Inventory playerInventory) {
        super(containerId, playerInventory, ContainerLevelAccess.NULL);
        this.player = playerInventory.player;
        this.access = ContainerLevelAccess.NULL;
        replaceResultSlot();
        replaceCraftingSlots();
    }

    public ExtendedShapedRecipe getActiveCustomRecipe() {
        return this.activeCustomRecipe;
    }

    public Player getPlayer() {
        return this.player;
    }

    private void replaceResultSlot() {
        Slot oldSlot = this.getSlot(0);
        Container resultContainer = oldSlot.container;
        CraftingContainer craftSlotsContainer = (CraftingContainer) this.getSlot(1).container;

        ExtendedResultSlot newSlot = new ExtendedResultSlot(this.player, craftSlotsContainer, resultContainer, 0, 124, 35, this);
        newSlot.index = oldSlot.index;

        this.slots.set(0, newSlot);
    }

    private void replaceCraftingSlots() {
        for (int i = 1; i <= 9; i++) {
            Slot oldSlot = this.getSlot(i);
            ExtendedCraftingSlot newSlot = new ExtendedCraftingSlot(oldSlot.container, oldSlot.getContainerSlot(), oldSlot.x, oldSlot.y);
            newSlot.index = oldSlot.index;
            this.slots.set(i, newSlot);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ExtendedCratingTable.get());
    }

    // --- НОВЫЙ МЕТОД: Обновление изменений контейнера каждый тик (20 раз в секунду) ---
    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        // Если на верстаке сейчас выложен кастомный рецепт, мы принудительно вызываем slotsChanged
        // каждый тик на сервере, чтобы среагировать на изменение времени или здоровья игрока на лету!
        if (!this.player.level().isClientSide() && this.activeCustomRecipe != null) {
            Container craftContainer = this.getSlot(1).container;
            this.slotsChanged(craftContainer);
        }
    }
    // ---------------------------------------------------------------------------------

    @Override
    public void slotsChanged(Container container) {
        Level currentLevel = this.player.level();

        if (container instanceof CraftingContainer craftingContainer) {
            CraftingInput input = craftingContainer.asPositionedCraftInput().input();

            Optional<RecipeHolder<ExtendedShapedRecipe>> customRecipe = currentLevel.getRecipeManager()
                    .getRecipeFor(ModRecipes.EXTENDED_CRAFTING_TYPE.get(), input, currentLevel);

            if (customRecipe.isPresent()) {
                this.activeCustomRecipe = customRecipe.get().value();

                if (!currentLevel.isClientSide) {
                    ServerLevel serverLevel = (ServerLevel) currentLevel;
                    if (this.activeCustomRecipe.testPlayerAndWorldConditions(this.player, serverLevel)) {
                        ItemStack result = this.activeCustomRecipe.assemble(input, serverLevel.registryAccess());
                        this.getSlot(0).set(result);
                    } else {
                        // Если условия перестали выполняться (например, наступила ночь вместо дня) — убираем результат крафта!
                        this.getSlot(0).set(ItemStack.EMPTY);
                    }
                }
            } else {
                this.activeCustomRecipe = null;
                super.slotsChanged(container);
            }
        } else {
            super.slotsChanged(container);
        }
    }

    public void handleCustomConsumption(Player player, ItemStack craftedStack) {
        if (this.activeCustomRecipe == null || this.player.level().isClientSide()) return;

        Level level = this.player.level();

        this.activeCustomRecipe.getConditions().xpCost().ifPresent(xp -> {
            if (xp.consumable()) player.giveExperiencePoints(-xp.value());
        });

        this.activeCustomRecipe.getConditions().xpLvl().ifPresent(xp -> {
            if (xp.consumable()) player.giveExperienceLevels(-xp.value());
        });

        int width = activeCustomRecipe.getPattern().get(0).length();
        int height = activeCustomRecipe.getPattern().size();

        int startX = 0, startY = 0;

        Container craftContainer = this.getSlot(1).container;
        if (!(craftContainer instanceof CraftingContainer craftingContainer)) return;

        CraftingInput input = craftingContainer.asPositionedCraftInput().input();

        outer:
        for (int i = 0; i <= 3 - width; ++i) {
            for (int j = 0; j <= 3 - height; ++j) {
                if (activeCustomRecipe.checkGridMatches(input)) {
                    startX = i;
                    startY = j;
                    break outer;
                }
            }
        }

        for (int x = 0; x < 3; ++x) {
            for (int y = 0; y < 3; ++y) {
                int dx = x - startX;
                int dy = y - startY;
                ExtendedIngredient ingredient = null;

                if (dx >= 0 && dy >= 0 && dx < width && dy < height) {
                    String symbol = String.valueOf(activeCustomRecipe.getPattern().get(dy).charAt(dx));
                    ingredient = activeCustomRecipe.getKey().get(symbol);
                }

                int slotIdx = x + y * 3;
                Slot slot = this.getSlot(slotIdx + 1);
                ItemStack stack = slot.getItem();

                if (ingredient != null && !stack.isEmpty()) {
                    if (ingredient.consumable()) {
                        stack.shrink(ingredient.count());
                        if (stack.isEmpty() && ingredient.itemAfterConsume().isPresent()) {
                            ItemStack after = ingredient.itemAfterConsume().get().copy();
                            slot.set(processEnchantedBook(after, level.registryAccess()));
                        }
                    } else {
                        if (ingredient.dealDamage() > 0) {
                            int damage = ingredient.dealDamage();
                            if (ingredient.workEnchantments()) {
                                net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> unbreakingHolder = level.registryAccess()
                                        .registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                                        .getHolder(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING)
                                        .orElseThrow();

                                int unbreakingLvl = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                                        unbreakingHolder,
                                        stack
                                );
                                if (unbreakingLvl > 0) {
                                    int savedDamage = 0;
                                    for (int k = 0; k < damage; k++) {
                                        if (level.random.nextInt(unbreakingLvl + 1) > 0) {
                                            savedDamage++;
                                        }
                                    }
                                    damage -= savedDamage;
                                }
                            }

                            stack.setDamageValue(stack.getDamageValue() + damage);

                            if (stack.getDamageValue() >= stack.getMaxDamage()) {
                                if (ingredient.itemAfterConsume().isPresent()) {
                                    ItemStack after = ingredient.itemAfterConsume().get().copy();
                                    slot.set(processEnchantedBook(after, level.registryAccess()));
                                } else {
                                    slot.set(ItemStack.EMPTY);
                                }
                            }
                        }
                    }
                    slot.setChanged();
                }
            }
        }
        this.slotsChanged(craftContainer);
    }

    @Override
    public MenuType<?> getType() {
        return ModMenus.EXTENDED_CRAFTING_TABLE_MENU.get();
    }

    private static class ExtendedCraftingSlot extends Slot {
        public ExtendedCraftingSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 64;
        }
    }

    private static class ExtendedResultSlot extends ResultSlot {
        private final ExtendedCraftingTableMenu menu;

        public ExtendedResultSlot(Player player, CraftingContainer craftSlots, Container container, int slot, int x, int y, ExtendedCraftingTableMenu menu) {
            super(player, craftSlots, container, slot, x, y);
            this.menu = menu;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            if (this.menu.activeCustomRecipe != null) {
                this.menu.handleCustomConsumption(player, stack);
            } else {
                super.onTake(player, stack);
            }
        }
    }

    private ItemStack processEnchantedBook(ItemStack stack, net.minecraft.core.RegistryAccess registries) {
        net.minecraft.world.item.component.CustomData customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData != null && customData.copyTag().contains("book_id")) {
            net.minecraft.nbt.CompoundTag tag = customData.copyTag();
            String bookId = tag.getString("book_id");
            int lvl = Integer.parseInt(tag.getString("book_lvl"));

            ItemStack out = new ItemStack(net.minecraft.world.item.Items.ENCHANTED_BOOK, stack.getCount());

            net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchHolder = registries.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                    .get(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT, ResourceLocation.parse(bookId)))
                    .orElseThrow();

            net.minecraft.world.item.enchantment.ItemEnchantments.Mutable mutableEnchants = new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
            mutableEnchants.set(enchHolder, lvl);

            net.minecraft.world.item.enchantment.EnchantmentHelper.setEnchantments(out, mutableEnchants.toImmutable());
            return out;
        }
        return stack;
    }
}
