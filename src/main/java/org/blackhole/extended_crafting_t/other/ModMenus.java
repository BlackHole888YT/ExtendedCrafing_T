package org.blackhole.extended_crafting_t.other;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.blackhole.extended_crafting_t.ExtendedCraftingT;
import org.blackhole.extended_crafting_t.logic.ExtendedCraftingTableMenu;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, ExtendedCraftingT.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ExtendedCraftingTableMenu>> EXTENDED_CRAFTING_TABLE_MENU =
            MENUS.register("extended_crafting_table_menu", () ->
                    new MenuType<>(ExtendedCraftingTableMenu::new, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS)
            );
}