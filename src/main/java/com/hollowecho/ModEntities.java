package com.hollowecho;

import com.hollowecho.entity.KeeperEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, HollowEcho.MODID);

    public static final RegistryObject<EntityType<KeeperEntity>> KEEPER = ENTITIES.register("keeper",
            () -> EntityType.Builder.of(KeeperEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build("keeper"));
}
