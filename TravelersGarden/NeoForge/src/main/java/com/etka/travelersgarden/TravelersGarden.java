package com.etka.travelersgarden;

import org.slf4j.Logger;

import com.etka.travelersgarden.recipe.ModRecipes;
import com.etka.travelersgarden.registry.ModItems;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(TravelersGarden.MODID)
public class TravelersGarden {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "travelersgarden";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();    // Create a Deferred Register to hold Blocks which will all be registered under the "travelersgarden" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "travelersgarden" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "travelersgarden" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);    // Creates a creative tab with the id "travelersgarden:travelers_gardens" for our farm items
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TRAVELERS_GARDENS_TAB = CREATIVE_MODE_TABS.register("travelers_gardens", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.travelersgarden.travelers_gardens")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.FOOD_AND_DRINKS)
            .icon(() -> ModItems.FARM_ITEMS.get("minecraft:wheat_seeds") != null ? 
                ModItems.FARM_ITEMS.get("minecraft:wheat_seeds").get().getDefaultInstance() : 
                Items.WHEAT_SEEDS.getDefaultInstance())
            .displayItems((parameters, output) -> {
                // Add all farm items to the tab
                ModItems.FARM_ITEMS.values().forEach(farmItem -> output.accept(farmItem.get()));
            }).build());
    
    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public TravelersGarden(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Initialize ModItems to trigger farm item registration
        System.out.println("TravelersGarden constructor: Forcing ModItems class loading");
        ModItems.class.getName(); // This forces the class to load and run the static block
        System.out.println("TravelersGarden constructor: ModItems loaded, farm items map size: " + ModItems.FARM_ITEMS.size());

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        // Register recipe serializers and types
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        ModRecipes.RECIPE_TYPES.register(modEventBus);        // Register ourselves for server and other game events we are interested in.        // Note that this is necessary if and only if we want *this* class (TravelersGarden) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        
        // Register the config screen factory to make the config button clickable in the mods menu
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        
        LOGGER.info("Traveler's Garden setup completed successfully!");
    }    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
