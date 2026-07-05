package org.blackhole.extended_crafting_t.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.blackhole.extended_crafting_t.ExtendedCraftingT;
import org.joml.Matrix4f;

public class ExtendedCraftingTableRenderer implements BlockEntityRenderer<ExtendedCraftingTableEntity> {

    private static final ResourceLocation TOP_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ExtendedCraftingT.MODID, "textures/block/ecto_top.png");
    private static final ResourceLocation SIDE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ExtendedCraftingT.MODID, "textures/block/ecto_side.png");
    private static final ResourceLocation FRONT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ExtendedCraftingT.MODID, "textures/block/ecto_front.png");

    public ExtendedCraftingTableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ExtendedCraftingTableEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        Level world = entity.getLevel();
        if (world == null) return;

        BlockState blockState = entity.getBlockState();

        // ИСПРАВЛЕНО: Теперь используем глобальное ванильное свойство BlockStateProperties.HORIZONTAL_FACING
        Direction facing = blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;

        // 1. Отрисовка базовых досок дерева
        String woodId = entity.getWoodType();
        Block plankBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(woodId));
        if (plankBlock == Blocks.AIR) {
            plankBlock = Blocks.OAK_PLANKS;
        }

        BlockState plankState = plankBlock.defaultBlockState();
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        poseStack.pushPose();
        blockRenderer.renderSingleBlock(
                plankState,
                poseStack,
                bufferSource,
                combinedLight,
                combinedOverlay,
                net.neoforged.neoforge.client.model.data.ModelData.EMPTY,
                null
        );
        poseStack.popPose();

        // 2. Отрисовка полупрозрачных оверлеев
        float offset = 0.001F;
        float min = -offset;
        float max = 1.0F + offset;
        Matrix4f matrix = poseStack.last().pose();

        // Верхняя грань (Всегда текстура TOP)
        VertexConsumer topConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(TOP_TEXTURE));
        drawQuad(topConsumer, matrix,
                min, max, min, 0, 0,
                min, max, max, 0, 1,
                max, max, max, 1, 1,
                max, max, min, 1, 0,
                0, 1, 0, combinedOverlay, combinedLight);

        // Распределяем текстуры FRONT и SIDE по бокам в зависимости от поворота верстака
        VertexConsumer northConsumer = bufferSource.getBuffer(RenderType.entityTranslucent((facing == Direction.NORTH) ? FRONT_TEXTURE : SIDE_TEXTURE));
        drawQuad(northConsumer, matrix,
                min, max, min, 0, 0,
                max, max, min, 1, 0,
                max, min, min, 1, 1,
                min, min, min, 0, 1,
                0, 0, -1, combinedOverlay, combinedLight);

        VertexConsumer southConsumer = bufferSource.getBuffer(RenderType.entityTranslucent((facing == Direction.SOUTH) ? FRONT_TEXTURE : SIDE_TEXTURE));
        drawQuad(southConsumer, matrix,
                max, max, max, 0, 0,
                min, max, max, 1, 0,
                min, min, max, 1, 1,
                max, min, max, 0, 1,
                0, 0, 1, combinedOverlay, combinedLight);

        VertexConsumer eastConsumer = bufferSource.getBuffer(RenderType.entityTranslucent((facing == Direction.EAST) ? FRONT_TEXTURE : SIDE_TEXTURE));
        drawQuad(eastConsumer, matrix,
                max, max, max, 0, 0,
                max, max, min, 1, 0,
                max, min, min, 1, 1,
                max, min, max, 0, 1,
                1, 0, 0, combinedOverlay, combinedLight);

        VertexConsumer westConsumer = bufferSource.getBuffer(RenderType.entityTranslucent((facing == Direction.WEST) ? FRONT_TEXTURE : SIDE_TEXTURE));
        drawQuad(westConsumer, matrix,
                min, max, min, 0, 0,
                min, max, max, 1, 0,
                min, min, max, 1, 1,
                min, min, min, 0, 1,
                -1, 0, 0, combinedOverlay, combinedLight);
    }

    private void drawQuad(VertexConsumer builder, Matrix4f matrix,
                          float x1, float y1, float z1, float u1, float v1,
                          float x2, float y2, float z2, float u2, float v2,
                          float x3, float y3, float z3, float u3, float v3,
                          float x4, float y4, float z4, float u4, float v4,
                          float nx, float ny, float nz, int overlay, int light) {
        builder.addVertex(matrix, x1, y1, z1).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(nx, ny, nz);
        builder.addVertex(matrix, x2, y2, z2).setColor(255, 255, 255, 255).setUv(u2, v2).setOverlay(overlay).setLight(light).setNormal(nx, ny, nz);
        builder.addVertex(matrix, x3, y3, z3).setColor(255, 255, 255, 255).setUv(u3, v3).setOverlay(overlay).setLight(light).setNormal(nx, ny, nz);
        builder.addVertex(matrix, x4, y4, z4).setColor(255, 255, 255, 255).setUv(u4, v4).setOverlay(overlay).setLight(light).setNormal(nx, ny, nz);
    }
}
