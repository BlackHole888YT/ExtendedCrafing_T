package org.blackhole.extended_crafting_t.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.blackhole.extended_crafting_t.ExtendedCraftingT;
import org.blackhole.extended_crafting_t.block.ExtendedCraftingScreen;
import org.blackhole.extended_crafting_t.block.ExtendedCraftingTableRenderer;
import org.blackhole.extended_crafting_t.other.ModBlockEntities;
import org.blackhole.extended_crafting_t.other.ModMenus;

@EventBusSubscriber(modid = ExtendedCraftingT.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Регистрируем наш динамический рендерер под наш тип Block Entity
        event.registerBlockEntityRenderer(
                ModBlockEntities.EXTENDED_CRAFTING_TABLE_ENTITY.get(),
                ExtendedCraftingTableRenderer::new
        );
    }

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        // Заменили CraftingScreen::new на ExtendedCraftingScreen::new
        event.register(ModMenus.EXTENDED_CRAFTING_TABLE_MENU.get(), ExtendedCraftingScreen::new);
    }
}
