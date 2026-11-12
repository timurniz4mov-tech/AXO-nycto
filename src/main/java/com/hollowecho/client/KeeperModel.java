package com.hollowecho.client;

import com.hollowecho.HollowEcho;
import com.hollowecho.entity.KeeperEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Модель Хранителя с процедурной анимацией: парение, покачивание мантии, жесты при речи, прыжки радости. */
public class KeeperModel extends EntityModel<KeeperEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(HollowEcho.MODID, "keeper"), "main");

    private final ModelPart root, head, body, robe, rightArm, leftArm;

    public KeeperModel(ModelPart root) {
        this.root = root;
        head = root.getChild("head");
        body = root.getChild("body");
        robe = root.getChild("robe");
        rightArm = root.getChild("right_arm");
        leftArm = root.getChild("left_arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4f, -8f, -4f, 8f, 8f, 8f)
                .texOffs(32, 0).addBox(-4f, -8f, -4f, 8f, 8f, 8f, new CubeDeformation(0.6f)), PartPose.ZERO);
        r.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-4f, 0f, -2f, 8f, 12f, 4f), PartPose.ZERO);
        r.addOrReplaceChild("robe", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-5f, 0f, -3f, 10f, 12f, 6f), PartPose.offset(0f, 10f, 0f));
        PartDefinition ra = r.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-3f, -2f, -2f, 4f, 12f, 4f), PartPose.offset(-5f, 2f, 0f));
        ra.addOrReplaceChild("lantern", CubeListBuilder.create()
                .texOffs(40, 32).addBox(-2f, 0f, -2f, 4f, 5f, 4f), PartPose.offset(-1f, 10f, 0f));
        r.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(40, 16).addBox(-1f, -2f, -2f, 4f, 12f, 4f), PartPose.offset(5f, 2f, 0f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(KeeperEntity e, float limbSwing, float limbSwingAmount, float age, float netHeadYaw, float headPitch) {
        boolean talk = e.getTalkTicks() > 0;
        boolean joy = e.getJoyTicks() > 0;

        float bob = Mth.sin(age * 0.08f) * 1.5f;
        if (joy) bob = Mth.sin(age * 0.6f) * 3f - 3f;

        head.y = bob; body.y = bob; robe.y = 10f + bob;
        rightArm.y = 2f + bob; leftArm.y = 2f + bob;

        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;

        robe.xRot = Mth.sin(age * 0.07f) * 0.06f;
        robe.zRot = Mth.cos(age * 0.05f) * 0.05f;

        rightArm.xRot = -0.35f + Mth.sin(age * 0.09f) * 0.04f;
        rightArm.zRot = 0.08f;
        leftArm.xRot = -0.35f + Mth.sin(age * 0.09f + 1f) * 0.04f;
        leftArm.zRot = -0.08f;

        if (talk) {
            leftArm.xRot = -1.2f + Mth.sin(age * 0.5f) * 0.35f;
            leftArm.zRot = -0.3f;
            head.xRot += Mth.sin(age * 0.4f) * 0.06f;
        }
        if (joy) {
            rightArm.xRot = -3.0f + Mth.sin(age * 0.8f) * 0.25f;
            leftArm.xRot = -3.0f + Mth.cos(age * 0.8f) * 0.25f;
            rightArm.zRot = 0.4f;
            leftArm.zRot = -0.4f;
        }
    }

    @Override
    public void renderToBuffer(PoseStack ps, VertexConsumer vc, int light, int overlay, float r, float g, float b, float a) {
        root.render(ps, vc, light, overlay, r, g, b, a);
    }
}
