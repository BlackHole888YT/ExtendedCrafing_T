package org.blackhole.extended_crafting_t;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.blackhole.extended_crafting_t.other.*;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ExtendedCraftingT.MODID)
public class ExtendedCraftingT {
    public static final String MODID = "extended_crafting_t";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ExtendedCraftingT(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockItem.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);

        // РЕГИСТРАЦИЯ РЕЦЕПТОВ (Обязательно для крафта!)
        ModRecipes.RECIPE_TYPES.register(modEventBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);

        // Ручная гарантированная регистрация экрана для клиента (РЕШАЕТ ВАШ КРАШ ПРИ ОТКРЫТИИ!)
    }

}
