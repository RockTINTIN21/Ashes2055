package com.rocktintin21.ashes2055;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class TaczMobEntity extends Zombie {
    public TaczMobEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);

        // Use a specific TACZ gun if present and avoid deprecated constructors
        ResourceLocation gunId = ResourceLocation.fromNamespaceAndPath("tacz", "ak47");
        Item gunItem = ForgeRegistries.ITEMS.getValue(gunId);
        if (gunItem != null) {
            this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(gunItem));
        }
    }
}
