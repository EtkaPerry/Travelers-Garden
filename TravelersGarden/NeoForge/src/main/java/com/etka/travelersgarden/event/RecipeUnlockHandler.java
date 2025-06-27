package com.etka.travelersgarden.event;

import com.etka.travelersgarden.TravelersGarden;
import com.etka.travelersgarden.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = TravelersGarden.MODID, bus = EventBusSubscriber.Bus.GAME)
public class RecipeUnlockHandler {
    
    // Keep track of players and which recipes they have unlocked to avoid spam
    private static final Map<String, Set<String>> playersWithRecipes = new HashMap<>();
      @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        // Check if player has any seeds/plants in inventory when they log in
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            checkPlayerInventory(serverPlayer);
        }}
    
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        // Check inventory every 20 ticks (1 second) for new items
        if (event.getEntity() instanceof ServerPlayer serverPlayer && 
            serverPlayer.tickCount % 20 == 0) {
            checkPlayerInventory(serverPlayer);
        }
    }
      private static void checkPlayerInventory(ServerPlayer player) {
        String playerUUID = player.getUUID().toString();
        
        // Check each item in current inventory against our seed mappings
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty()) {
                String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                if (ModItems.SEED_TO_CROP_MAP.containsKey(itemId)) {
                    unlockSpecificRecipe(player, itemId);
                }
            }
        }
    }
      private static void unlockSpecificRecipe(ServerPlayer player, String seedItemId) {
        String playerUUID = player.getUUID().toString();
        
        // Get the crop type and generate the farm recipe name
        String cropType = ModItems.SEED_TO_CROP_MAP.get(seedItemId);
        if (cropType == null) return;
        
        String farmRecipe = cropType.replace("minecraft:", "").replace("farmersdelight:", "fd_") + "_farm";
        
        // Initialize player's recipe set if not exists
        playersWithRecipes.computeIfAbsent(playerUUID, k -> new HashSet<>());
        
        // Check if we already gave this specific recipe to this player
        if (playersWithRecipes.get(playerUUID).contains(farmRecipe)) {
            return;
        }
        
        // Add recipe to the player's unlocked set
        playersWithRecipes.get(playerUUID).add(farmRecipe);
        
        // Get the recipe manager and unlock the specific recipe
        var recipeManager = player.getServer().getRecipeManager();
        
        // Create the recipe resource location
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(TravelersGarden.MODID, farmRecipe);
        
        // Get the recipe holder
        var recipeHolder = recipeManager.byKey(recipeId);
        if (recipeHolder.isPresent()) {
            var recipesToUnlock = new java.util.ArrayList<net.minecraft.world.item.crafting.RecipeHolder<?>>();
            recipesToUnlock.add(recipeHolder.get());
            
            // Grant the recipe to the player
            player.awardRecipes(recipesToUnlock);
              // Send a message to the player with proper display name
            String itemTranslationKey = "item.travelersgarden." + farmRecipe;
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                "message.travelersgarden.recipe_unlocked", 
                net.minecraft.network.chat.Component.translatable(itemTranslationKey)
            ));
            
            TravelersGarden.LOGGER.info("Unlocked {} recipe for player {}", 
                farmRecipe, player.getName().getString());
        }
    }
}
