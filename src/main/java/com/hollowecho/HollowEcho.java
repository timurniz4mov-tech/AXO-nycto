package com.hollowecho;

import com.hollowecho.entity.KeeperEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HollowEcho.MODID)
public class HollowEcho {
    public static final String MODID = "hollowecho";

    public HollowEcho() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(bus);
        ModItems.TABS.register(bus);
        ModEntities.ENTITIES.register(bus);
        bus.addListener(this::attributes);
        MinecraftForge.EVENT_BUS.register(new ModEvents());
    }

    private void attributes(EntityAttributeCreationEvent e) {
        e.put(ModEntities.KEEPER.get(), KeeperEntity.createAttributes().build());
    }
}
