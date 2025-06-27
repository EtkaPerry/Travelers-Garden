package com.etka.travelersgarden.recipe;

import com.etka.travelersgarden.TravelersGarden;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = 
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, TravelersGarden.MODID);
    
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = 
            DeferredRegister.create(Registries.RECIPE_TYPE, TravelersGarden.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<TravelersGardenRecipe>> TRAVELERS_GARDEN_RECIPE_TYPE =
            RECIPE_TYPES.register("travelers_garden", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(TravelersGarden.MODID, "travelers_garden")));    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TravelersGardenRecipe>> TRAVELERS_GARDEN_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("travelers_garden", () -> new TravelersGardenRecipeSerializer());

    public static class TravelersGardenRecipeSerializer implements RecipeSerializer<TravelersGardenRecipe> {
        private static final MapCodec<TravelersGardenRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                CraftingBookCategory.CODEC.fieldOf("category").forGetter(recipe -> recipe.category())
            ).apply(instance, TravelersGardenRecipe::new)
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, TravelersGardenRecipe> STREAM_CODEC = 
            StreamCodec.composite(
                CraftingBookCategory.STREAM_CODEC, recipe -> recipe.category(),
                TravelersGardenRecipe::new
            );
        
        @Override
        public MapCodec<TravelersGardenRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TravelersGardenRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
