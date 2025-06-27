package com.etka.travelersgarden.item;

import com.etka.travelersgarden.Config;
import com.etka.travelersgarden.TravelersGarden;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TravelersGardenItem extends Item {
    private final String seedType;
    private final String cropType;

    public TravelersGardenItem(String seedType, String cropType, Properties properties) {
        super(properties.stacksTo(1)); // Non-stackable for balance
        this.seedType = seedType;
        this.cropType = cropType;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide()) return;

        // Get or create NBT data using new component system
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        
        // Initialize farm data if needed
        if (!tag.contains("farmTicks")) {
            tag.putInt("farmTicks", 0);
            tag.putString("seedType", this.seedType);
            tag.putString("cropType", this.cropType);
        }

        // Increment farm ticks with potential speed boost
        int currentTicks = tag.getInt("farmTicks");
        int ticksToAdd = 1;
        
        // Configurable chance for speed boost
        if (level.random.nextFloat() < Config.getSpeedBoostChance()) {
            float minBoost = Config.getSpeedBoostMinPercentage();
            float maxBoost = Config.getSpeedBoostMaxPercentage();
            float speedBoost = minBoost + (level.random.nextFloat() * (maxBoost - minBoost));
            ticksToAdd = Math.max(1, Math.round(1 + speedBoost * Config.calculateGrowthTime(this.cropType) / 100f));
            
            if (Config.ENABLE_DEBUG_LOGGING.getAsBoolean()) {
                TravelersGarden.LOGGER.info("Speed boost applied! Adding {} ticks instead of 1 ({}% boost)", 
                    ticksToAdd, Math.round(speedBoost * 100));
            }
        }
        
        currentTicks += ticksToAdd;
        tag.putInt("farmTicks", currentTicks);

        // Update the stack with new data
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));        // Check if it's time to generate crop
        int tickRate = Config.calculateGrowthTime(this.cropType);
        
        if (currentTicks >= tickRate) {
            // Reset ticks
            tag.putInt("farmTicks", 0);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            
            // Try to generate crop
            generateCrop(stack, level, entity);
        }
    }    private void generateCrop(ItemStack farmStack, Level level, Entity entity) {
        if (Config.ENABLE_DEBUG_LOGGING.getAsBoolean()) {
            TravelersGarden.LOGGER.info("Attempting to generate crop: {}", this.cropType);
        }

        // Get the crop item
        ResourceLocation cropId = ResourceLocation.parse(this.cropType);
        Item cropItem = BuiltInRegistries.ITEM.get(cropId);
        
        if (cropItem != null && entity instanceof net.minecraft.world.entity.player.Player player) {
            ItemStack cropStack = new ItemStack(cropItem, 1);
            
            // Try to add to player inventory
            if (player.getInventory().add(cropStack)) {
                if (Config.ENABLE_DEBUG_LOGGING.getAsBoolean()) {
                    TravelersGarden.LOGGER.info("Generated crop: {} for player: {}", this.cropType, player.getName().getString());
                }
            } else {
                // Drop the item near the player if inventory is full
                player.drop(cropStack, false);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();        if (!tag.isEmpty()) {
            String seedName = tag.getString("seedType");
            String cropName = tag.getString("cropType");
            int currentTicks = tag.getInt("farmTicks");
            int maxTicks = Config.calculateGrowthTime(this.cropType);
            
            // Get the actual crop item to display its localized name
            ResourceLocation cropId = ResourceLocation.parse(cropName);
            Item cropItem = BuiltInRegistries.ITEM.get(cropId);
            String displayName = cropItem.getDescription().getString();
            
            tooltipComponents.add(Component.literal("Crop: " + displayName)
                    .withStyle(ChatFormatting.GREEN));
            
            // Progress bar
            int progress = (int) ((double) currentTicks / maxTicks * 100);
            tooltipComponents.add(Component.literal("Growth: " + progress + "%")
                    .withStyle(ChatFormatting.YELLOW));
            
            // Show time information
            int remainingTicks = maxTicks - currentTicks;
            String timeLeft = Config.formatTime(remainingTicks);
            tooltipComponents.add(Component.literal("Time left: " + timeLeft)
                    .withStyle(ChatFormatting.AQUA));
            
            // Show total growth time
            String totalTime = Config.formatTime(maxTicks);
            tooltipComponents.add(Component.literal("Total time: " + totalTime)
                    .withStyle(ChatFormatting.DARK_AQUA));
            
            tooltipComponents.add(Component.literal("A portable farm that generates crops")
                    .withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.literal("while in your inventory")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    public String getSeedType() {
        return seedType;
    }

    public String getCropType() {
        return cropType;
    }
}
