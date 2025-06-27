package com.etka.travelersgarden.recipe;

import com.etka.travelersgarden.TravelersGarden;
import com.etka.travelersgarden.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredItem;

public class TravelersGardenRecipe extends CustomRecipe {
    private final CraftingBookCategory craftingCategory;
    
    public TravelersGardenRecipe(CraftingBookCategory category) {
        super(category);
        this.craftingCategory = category;
    }

    public CraftingBookCategory category() {
        return this.craftingCategory;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.TRAVELERS_GARDEN_RECIPE_TYPE.get();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean hasWaterBucket = false;
        boolean hasDirt = false;
        boolean hasGlass = false;
        ItemStack seedStack = ItemStack.EMPTY;
        int itemCount = 0;

        // Check all slots in the crafting grid
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                itemCount++;
                
                if (stack.is(Items.WATER_BUCKET)) {
                    hasWaterBucket = true;
                } else if (stack.is(Items.DIRT)) {
                    hasDirt = true;
                } else if (stack.is(Items.GLASS)) {
                    hasGlass = true;
                } else {
                    // Check if this item is a valid seed
                    String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                    if (ModItems.SEED_TO_CROP_MAP.containsKey(itemId)) {
                        if (seedStack.isEmpty()) {
                            seedStack = stack;
                        } else {
                            // Multiple seeds found - invalid recipe
                            return false;
                        }
                    } else {
                        // Invalid item found
                        return false;
                    }
                }
            }
        }

        // Must have exactly 4 items: water bucket, dirt, glass, and one valid seed
        return itemCount == 4 && hasWaterBucket && hasDirt && hasGlass && !seedStack.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        // Find the seed in the crafting grid
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                if (ModItems.SEED_TO_CROP_MAP.containsKey(itemId)) {
                    // Get the corresponding farm item
                    DeferredItem<? extends Item> farmItem = ModItems.FARM_ITEMS.get(itemId);
                    if (farmItem != null) {
                        return new ItemStack(farmItem.get());
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remainingItems = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        
        // Return empty bucket after using water bucket
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(Items.WATER_BUCKET)) {
                remainingItems.set(i, new ItemStack(Items.BUCKET));
                break;
            }
        }
        
        return remainingItems;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 2 && height >= 2; // Requires at least 2x2 crafting grid
    }    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.TRAVELERS_GARDEN_RECIPE_SERIALIZER.get();
    }
}
