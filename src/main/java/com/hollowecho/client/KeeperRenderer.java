package com.hollowecho.client;

import com.hollowecho.HollowEcho;
import com.hollowecho.entity.KeeperEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class KeeperRenderer extends MobRenderer<KeeperEntity, KeeperModel> {
    private static final ResourceLocation TEX = new ResourceLocation(HollowEcho.MODID, "textures/entity/keeper.png");

    public KeeperRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new KeeperModel(ctx.bakeLayer(KeeperModel.LAYER)), 0.4f);
    }

    @Override public ResourceLocation getTextureLocation(KeeperEntity e) { return TEX; }

    /** Хранитель светится сам по себе. */
    @Override protected int getBlockLightLevel(KeeperEntity e, BlockPos pos) { return 15; }

    /** В момент ликования раскручиваем его вокруг оси. */
    @Override
    protected void setupRotations(KeeperEntity e, PoseStack ps, float age, float yaw, float pt) {
        super.setupRotations(e, ps, age, yaw, pt);
        if (e.getJoyTicks() > 0) ps.mulPose(Axis.YP.rotationDegrees(age * 25f));
    }
}
