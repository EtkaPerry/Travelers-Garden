package com.etka.travelersgarden.registry;

import com.etka.travelersgarden.TravelersGarden;
import com.etka.travelersgarden.item.TravelersGardenItem;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.HashMap;
import java.util.Map;

public class ModItems {
    // Map to store seed -> crop mappings
    public static final Map<String, String> SEED_TO_CROP_MAP = new HashMap<>();
      // Map to store registered farm items
    public static final Map<String, DeferredItem<TravelersGardenItem>> FARM_ITEMS = new HashMap<>();
    
    static {
        // Initialize seed to crop mappings
        SEED_TO_CROP_MAP.put("minecraft:wheat_seeds", "minecraft:wheat");
        SEED_TO_CROP_MAP.put("minecraft:carrot", "minecraft:carrot");
        SEED_TO_CROP_MAP.put("minecraft:potato", "minecraft:potato");        
        SEED_TO_CROP_MAP.put("minecraft:beetroot_seeds", "minecraft:beetroot");
        SEED_TO_CROP_MAP.put("minecraft:nether_wart", "minecraft:nether_wart");
        SEED_TO_CROP_MAP.put("minecraft:red_mushroom", "minecraft:red_mushroom");
        SEED_TO_CROP_MAP.put("minecraft:brown_mushroom", "minecraft:brown_mushroom");
        SEED_TO_CROP_MAP.put("minecraft:sweet_berries", "minecraft:sweet_berries");        // Torchflower and Pitcher Pod from 1.20
        //SEED_TO_CROP_MAP.put("minecraft:torchflower_seeds", "minecraft:torchflower");
        //SEED_TO_CROP_MAP.put("minecraft:pitcher_pod", "minecraft:pitcher_plant");
        
        // Add Farmer's Delight crops if the mod is loaded
        if (ModList.get().isLoaded("farmersdelight")) {
            System.out.println("Farmer's Delight detected! Adding FD crops to TravelersGarden...");
            SEED_TO_CROP_MAP.put("farmersdelight:cabbage_seeds", "farmersdelight:cabbage");
            SEED_TO_CROP_MAP.put("farmersdelight:tomato_seeds", "farmersdelight:tomato");
            SEED_TO_CROP_MAP.put("farmersdelight:onion", "farmersdelight:onion");
            SEED_TO_CROP_MAP.put("farmersdelight:rice", "farmersdelight:rice");
            System.out.println("Added " + 4 + " Farmer's Delight crops to seed mappings");
        }
        
        // Register farm items for each seed type immediately
        registerFarmItems();
    }      private static void registerFarmItems() {
        System.out.println("ModItems static block: Starting farm item registration");
        
        // Register farm items for each seed type
        for (Map.Entry<String, String> entry : SEED_TO_CROP_MAP.entrySet()) {
            String seedType = entry.getKey();
            String cropType = entry.getValue();
            
            // Create a clean name for the farm item
            String farmName = cropType.replace("minecraft:", "").replace("farmersdelight:", "fd_") + "_farm";
              DeferredItem<TravelersGardenItem> farmItem = TravelersGarden.ITEMS.register(farmName,
                () -> new TravelersGardenItem(seedType, cropType, new Item.Properties()));
            
            FARM_ITEMS.put(seedType, farmItem);
            System.out.println("Registered farm item: " + farmName + " for seed: " + seedType);
        }
        
        System.out.println("ModItems static block: Registered " + FARM_ITEMS.size() + " traveler's garden items");
    }
}
