package org.blackhole.extended_crafting_t.block;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.blackhole.extended_crafting_t.logic.ExtendedCraftingTableMenu;
import org.blackhole.extended_crafting_t.recipes.ExtendedShapedRecipe;

public class ExtendedCraftingScreen extends AbstractContainerScreen<ExtendedCraftingTableMenu> {

    private static final ResourceLocation CRAFTING_TABLE_LOCATION =
            ResourceLocation.withDefaultNamespace("textures/gui/container/crafting_table.png");

    public ExtendedCraftingScreen(ExtendedCraftingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 29;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 73;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(CRAFTING_TABLE_LOCATION, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);

        Player player = this.menu.getPlayer();
        ExtendedShapedRecipe activeRecipe = this.menu.getActiveCustomRecipe();

        int startX = 185;
        int startY = 6;

        guiGraphics.drawString(this.font, Component.translatable("ect.craft_req"), startX, startY, 0xFFFFAA00, false);
        startY += 15;

        long currentTime = player.level().getDayTime() % 24000L;
        guiGraphics.drawString(this.font,  Component.translatable("ect.current_time", currentTime), startX, startY, 0xFFFFFFFF, false);
        startY += 12;

        if (activeRecipe != null) {
            var conditions = activeRecipe.getConditions();

            // 1. Проверка Измерения (Используем локализацию)
            if (conditions.requiredDimension().isPresent()) {
                String reqDim = conditions.requiredDimension().get();
                boolean dimMatched = activeRecipe.testDimension(player.level());
                String color = dimMatched ? "§a" : "§c";

                // Форматируем имя измерения цветом в верхнем регистре (например: "§aOVERWORLD")
                String coloredDimName = color + reqDim.toUpperCase();

                // Выводим через транслейт-ключ, передавая имя измерения в качестве аргумента %s
                guiGraphics.drawString(
                        this.font,
                        Component.translatable("ect.dimension", coloredDimName),
                        startX, startY, 0xFFFFFFFF, false
                );
                startY += 12;
            }

            if (conditions.requiredHp().isPresent()) {
                String hpCond = conditions.requiredHp().get();
                float hp = player.getHealth();
                float maxHp = player.getMaxHealth();
                boolean hpMatched = activeRecipe.testHp(player);
                String color = hpMatched ? "§a" : "§c";

                // Выбираем правильный перевод для ХП: ФУЛЛ ХП или указанное число
                Component displayHp = hpCond.equalsIgnoreCase("max")
                        ? Component.translatable("ect.health.max")
                        : Component.translatable("ect.health.suffix", hpCond);

                Component formattedHp = Component.literal(color).append(displayHp);

                guiGraphics.drawString(this.font, Component.translatable("ect.health", formattedHp, (int)hp, (int)maxHp), startX, startY, 0xFFFFFFFF, false);
                startY += 12;
            }

            // 3. Проверка Опыта поинтов
            if (conditions.xpCost().isPresent()) {
                int costValue = conditions.xpCost().get().value();
                boolean xpMatched = activeRecipe.testXpCost(player);
                String color = xpMatched ? "§a" : "§c";
                Component formattedXp = Component.literal(color + player.totalExperience);

                guiGraphics.drawString(this.font, Component.translatable("ect.xp_cost", formattedXp, costValue), startX, startY, 0xFFFFFFFF, false);
                startY += 12;
            }

            // 4. Проверка уровней опыта
            if (conditions.xpLvl().isPresent()) {
                int lvlValue = conditions.xpLvl().get().value();
                boolean lvlMatched = activeRecipe.testXpLvl(player);
                String color = lvlMatched ? "§a" : "§c";
                Component formattedLvl = Component.literal(color + player.experienceLevel);

                guiGraphics.drawString(this.font, Component.translatable("ect.xp_lvl", formattedLvl, lvlValue), startX, startY, 0xFFFFFFFF, false);
                startY += 12;
            }

            // 5. Проверка времени суток (С автоматическим переводом самого времени: Day, Night и т.д.)
            if (conditions.requiredTime().isPresent()) {
                var timeCond = conditions.requiredTime().get();
                boolean timeMatched = activeRecipe.testTime(player.level());
                String color = timeMatched ? "§a" : "§c";

                // Динамический перевод времени (ect.time.day, ect.time.night и т.д.)
                Component timeName = Component.translatable("ect.time." + timeCond.value().toLowerCase());
                Component formattedTime = Component.literal(color).append(timeName);

                guiGraphics.drawString(this.font, Component.translatable("ect.time", formattedTime, timeCond.interval()), startX, startY, 0xFFFFFFFF, false);
                startY += 12;
            }

            // 6. Проверка Достижений
            if (conditions.requiredAdvancement().isPresent()) {
                String adv = conditions.requiredAdvancement().get();
                boolean advMatched = activeRecipe.testAdvancement(player);
                String color = advMatched ? "§a" : "§c";
                String shortAdvName = adv.contains("/") ? adv.substring(adv.lastIndexOf('/') + 1) : adv;
                Component formattedAdv = Component.literal(color + shortAdvName);

                guiGraphics.drawString(this.font, Component.translatable("ect.advancement", formattedAdv), startX, startY, 0xFFFFFFFF, false);
                startY += 12;
            }

        } else {
            // Отрисовка локализованной заглушки ("ect.no_recipe...")
            guiGraphics.drawString(this.font, Component.translatable("ect.no_recipe_line1"), startX, startY, 0xFFFFFFFF, false);
            guiGraphics.drawString(this.font, Component.translatable("ect.no_recipe_line2"), startX, startY + 12, 0xFFFFFFFF, false);
            guiGraphics.drawString(this.font, Component.translatable("ect.no_recipe_line3"), startX, startY + 24, 0xFFFFFFFF, false);
        }
    }
}