package com.rocktintin21.ashes2055;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.ForgeRegistries;

public class TaczMobEntity extends Zombie {
    public TaczMobEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);

        // Equip the first registered TACZ item so the ID is always valid
        for (var entry : ForgeRegistries.ITEMS.getEntries()) {
            ResourceKey<Item> key = entry.getKey();
            if ("tacz".equals(key.location().getNamespace())) {
                Item gun = entry.getValue();
                this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(gun));
                break;
            }
        }
    }
}
