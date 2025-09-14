package com.ashes2055.item;

import com.ashes2055.Ashes2055;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Ashes2055.MOD_ID);

    public static final RegistryObject<CreativeModeTab> ASHES_TAB = TABS.register(
            "ashes2055_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.ashes2055_tab"))
                    .icon(() -> new ItemStack(ModItems.RAIDER_SPAWN_EGG.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.RAIDER_SPAWN_EGG.get());
                        // сюда добавляйте остальные предметы мода
                    })
                    .build()
    );

    private ModCreativeTabs() {}
}