package org.blackhole.extended_crafting_t.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.Optional;

public class ExtendedShapedSerializer implements RecipeSerializer<ExtendedShapedRecipe> {

    private final MapCodec<ExtendedShapedRecipe> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("pattern").forGetter(ExtendedShapedRecipe::getPattern),
            Codec.unboundedMap(Codec.STRING, ExtendedIngredient.CODEC).fieldOf("key").forGetter(ExtendedShapedRecipe::getKey),
            AdditionalConditions.CODEC.optionalFieldOf("additional_conditions", new AdditionalConditions(
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()
            )).forGetter(ExtendedShapedRecipe::getConditions),
            ItemStack.CODEC.fieldOf("result").forGetter(ExtendedShapedRecipe::getResult)
    ).apply(instance, ExtendedShapedRecipe::new));

    // ИСПРАВЛЕНО: Заменено net.mojang... на импортированный JsonOps.INSTANCE
    private final StreamCodec<RegistryFriendlyByteBuf, ExtendedShapedRecipe> streamCodec =
            StreamCodec.of(
                    (buf, recipe) -> buf.writeUtf(this.codec.codec().encodeStart(buf.registryAccess().createSerializationContext(JsonOps.INSTANCE), recipe).getOrThrow().toString()),
                    (buf) -> this.codec.codec().parse(buf.registryAccess().createSerializationContext(JsonOps.INSTANCE), new com.google.gson.JsonParser().parse(buf.readUtf())).getOrThrow()
            );

    @Override
    public MapCodec<ExtendedShapedRecipe> codec() {
        return this.codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ExtendedShapedRecipe> streamCodec() {
        return this.streamCodec;
    }
}