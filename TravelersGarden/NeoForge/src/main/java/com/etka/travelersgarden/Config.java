package com.etka.travelersgarden;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import com.etka.travelersgarden.TravelersGarden;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Farm-specific configuration
    public static final ModConfigSpec.IntValue FARM_TICK_RATE = BUILDER
            .comment("How many ticks between farm growth attempts (20 ticks = 1 second)")
            .defineInRange("farmTickRate", 37200, 100, 72000); // Default 31 minutes (1.5 MC days), range 5s-60min

    public static final ModConfigSpec.DoubleValue GROWTH_SPEED_MULTIPLIER = BUILDER
            .comment("Global growth speed multiplier for traveler's gardens (1.0 = 31 minutes default, higher = faster)")
            .defineInRange("growthSpeedMultiplier", 1.0, 0.1, 3.0);

    // Individual crop speed configurations
    public static final ModConfigSpec.DoubleValue WHEAT_SPEED_MULTIPLIER = BUILDER
            .comment("Speed multiplier for wheat crops")
            .defineInRange("wheatSpeedMultiplier", 1.0, 0.1, 5.0);
    
    public static final ModConfigSpec.DoubleValue CARROT_SPEED_MULTIPLIER = BUILDER
            .comment("Speed multiplier for carrot crops")
            .defineInRange("carrotSpeedMultiplier", 1.0, 0.1, 5.0);
    
    public static final ModConfigSpec.DoubleValue POTATO_SPEED_MULTIPLIER = BUILDER
            .comment("Speed multiplier for potato crops")
            .defineInRange("potatoSpeedMultiplier", 1.0, 0.1, 5.0);
    
    public static final ModConfigSpec.DoubleValue BEETROOT_SPEED_MULTIPLIER = BUILDER
            .comment("Speed multiplier for beetroot crops")
            .defineInRange("beetrootSpeedMultiplier", 1.0, 0.1, 5.0);
    
    public static final ModConfigSpec.DoubleValue NETHER_WART_SPEED_MULTIPLIER = BUILDER
            .comment("Speed multiplier for nether wart crops")
            .defineInRange("netherWartSpeedMultiplier", 1.0, 0.1, 5.0);
    
    public static final ModConfigSpec.DoubleValue SWEET_BERRIES_SPEED_MULTIPLIER = BUILDER
            .comment("Speed multiplier for sweet berries crops")
            .defineInRange("sweetBerriesSpeedMultiplier", 1.0, 0.1, 5.0);

    // Farmer's Delight crop speed configurations
    public static final ModConfigSpec.DoubleValue CABBAGE_SPEED_MULTIPLIER = BUILDER
            .comment("Speed multiplier for cabbage crops (Farmer's Delight)")
            .defineInRange("cabbageSpeedMultiplier", 1.0, 0.1, 5.0);
    
    public static final ModConfigSpec.DoubleValue TOMATO_SPEED_MULTIPLIER = BUILDER
            .comment("Speed multiplier for tomato crops (Farmer's Delight)")
            .defineInRange("tomatoSpeedMultiplier", 1.0, 0.1, 5.0);
    
    public static final ModConfigSpec.DoubleValue ONION_SPEED_MULTIPLIER = BUILDER
            .comment("Speed multiplier for onion crops (Farmer's Delight)")
            .defineInRange("onionSpeedMultiplier", 1.0, 0.1, 5.0);
    
    public static final ModConfigSpec.DoubleValue RICE_SPEED_MULTIPLIER = BUILDER
            .comment("Speed multiplier for rice crops (Farmer's Delight)")
            .defineInRange("riceSpeedMultiplier", 1.0, 0.1, 5.0);

    public static final ModConfigSpec.BooleanValue ENABLE_DEBUG_LOGGING = BUILDER
            .comment("Enable debug logging for farm operations")
            .define("enableDebugLogging", false);

    // Speed boost configuration
    public static final ModConfigSpec.DoubleValue SPEED_BOOST_CHANCE = BUILDER
            .comment("Chance for speed boost per tick (0.0001 = 0.01% = 1 in 10,000 ticks)")
            .defineInRange("speedBoostChance", 0.0001, 0.0, 1.0);
    
    public static final ModConfigSpec.DoubleValue SPEED_BOOST_MIN_PERCENTAGE = BUILDER
            .comment("Minimum speed boost percentage (0.03 = 3%)")
            .defineInRange("speedBoostMinPercentage", 0.03, 0.01, 0.5);
    
    public static final ModConfigSpec.DoubleValue SPEED_BOOST_MAX_PERCENTAGE = BUILDER
            .comment("Maximum speed boost percentage (0.10 = 10%)")
            .defineInRange("speedBoostMaxPercentage", 0.10, 0.01, 0.5);

    static final ModConfigSpec SPEC = BUILDER.build();

    /**
     * Get the speed multiplier for a specific crop type
     * @param cropType The crop type (e.g., "minecraft:wheat")
     * @return The speed multiplier for that crop type
     */
    public static double getCropSpeedMultiplier(String cropType) {
        return switch (cropType) {
            case "minecraft:wheat" -> WHEAT_SPEED_MULTIPLIER.getAsDouble();
            case "minecraft:carrot" -> CARROT_SPEED_MULTIPLIER.getAsDouble();
            case "minecraft:potato" -> POTATO_SPEED_MULTIPLIER.getAsDouble();
            case "minecraft:beetroot" -> BEETROOT_SPEED_MULTIPLIER.getAsDouble();
            case "minecraft:nether_wart" -> NETHER_WART_SPEED_MULTIPLIER.getAsDouble();
            case "minecraft:sweet_berries" -> SWEET_BERRIES_SPEED_MULTIPLIER.getAsDouble();
            // Farmer's Delight crops
            case "farmersdelight:cabbage" -> CABBAGE_SPEED_MULTIPLIER.getAsDouble();
            case "farmersdelight:tomato" -> TOMATO_SPEED_MULTIPLIER.getAsDouble();
            case "farmersdelight:onion" -> ONION_SPEED_MULTIPLIER.getAsDouble();
            case "farmersdelight:rice" -> RICE_SPEED_MULTIPLIER.getAsDouble();
            default -> 1.0; // Default multiplier for unknown crops
        };
    }

    /**
     * Calculate the final growth time in ticks for a specific crop
     * @param cropType The crop type (e.g., "minecraft:wheat")
     * @return The growth time in ticks
     */
    public static int calculateGrowthTime(String cropType) {
        double globalMultiplier = GROWTH_SPEED_MULTIPLIER.getAsDouble();
        double cropMultiplier = getCropSpeedMultiplier(cropType);
        double totalMultiplier = globalMultiplier * cropMultiplier;
        return (int) (FARM_TICK_RATE.getAsInt() / totalMultiplier);
    }

    /**
     * Format time in ticks to a human-readable string
     * @param ticks The time in ticks
     * @return Formatted time string (e.g., "5m 30s" or "45s")
     */
    public static String formatTime(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }

    /**
     * Get the speed boost chance from config
     * @return The speed boost chance as a float (0.0 to 1.0)
     */
    public static float getSpeedBoostChance() {
        return (float) SPEED_BOOST_CHANCE.getAsDouble();
    }

    /**
     * Get the speed boost min percentage from config
     * @return The minimum speed boost percentage as a float
     */
    public static float getSpeedBoostMinPercentage() {
        return (float) SPEED_BOOST_MIN_PERCENTAGE.getAsDouble();
    }

    /**
     * Get the speed boost max percentage from config
     * @return The maximum speed boost percentage as a float
     */
    public static float getSpeedBoostMaxPercentage() {
        return (float) SPEED_BOOST_MAX_PERCENTAGE.getAsDouble();
    }

    @EventBusSubscriber(modid = TravelersGarden.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class ConfigEventHandler {
        @SubscribeEvent
        static void onLoad(final ModConfigEvent event) {
            // This method is called when the config is loaded or reloaded
            if (ENABLE_DEBUG_LOGGING.getAsBoolean()) {
                TravelersGarden.LOGGER.info("Traveler's Garden config loaded/reloaded");
                TravelersGarden.LOGGER.info("Farm tick rate: {}", FARM_TICK_RATE.getAsInt());
                TravelersGarden.LOGGER.info("Global growth speed multiplier: {}", GROWTH_SPEED_MULTIPLIER.getAsDouble());
                TravelersGarden.LOGGER.info("Speed boost chance: {}%", SPEED_BOOST_CHANCE.getAsDouble() * 100);
                TravelersGarden.LOGGER.info("Speed boost range: {}%-{}%", 
                    SPEED_BOOST_MIN_PERCENTAGE.getAsDouble() * 100, 
                    SPEED_BOOST_MAX_PERCENTAGE.getAsDouble() * 100);
            }
        }
    }
}
