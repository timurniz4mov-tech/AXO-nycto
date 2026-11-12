package com.hollowecho;

import com.hollowecho.item.EchoCompassItem;
import com.hollowecho.item.LightHeartItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HollowEcho.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HollowEcho.MODID);

    public static final RegistryObject<Item> ECHO_COMPASS = ITEMS.register("echo_compass",
            () -> new EchoCompassItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LIGHT_HEART = ITEMS.register("light_heart",
            () -> new LightHeartItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> KEEPER_EGG = ITEMS.register("keeper_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.KEEPER, 0x1B2A49, 0xFFD166, new Item.Properties()));

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hollowecho"))
                    .icon(() -> new ItemStack(LIGHT_HEART.get()))
                    .displayItems((params, out) -> {
                        out.accept(ECHO_COMPASS.get());
                        out.accept(LIGHT_HEART.get());
                        out.accept(KEEPER_EGG.get());
                    }).build());
}
