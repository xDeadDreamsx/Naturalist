package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.client.model.animation.AlligatorBabyAnimations;
import com.crispytwig.naturalist.server.entity.mob.Alligator;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.jspecify.annotations.NonNull;

public class AlligatorBabyModel extends NaturalistEntityModel<Alligator> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Naturalist.location("alligator_baby"), "main");
	private final ModelPart root;
	private final ModelPart neck;

	public AlligatorBabyModel(ModelPart root) {
		this.root = root.getChild("root");
		this.neck = this.root.getChild("body").getChild("neck");
	}

	@Override
	public @NonNull ModelPart root() {
		return this.root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
		PartDefinition legs = root.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition leftLeg = legs.addOrReplaceChild("leftLeg", CubeListBuilder.create()
		.texOffs(24, 6).addBox(0.5F, 0.0F, -1.5F, 5.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 0.0F, 3.5F));
		PartDefinition leftArm = legs.addOrReplaceChild("leftArm", CubeListBuilder.create()
		.texOffs(24, 6).addBox(0.5F, 0.0F, -1.5F, 5.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 0.0F, -3.5F));
		PartDefinition rightArm = legs.addOrReplaceChild("rightArm", CubeListBuilder.create()
		.texOffs(24, 6).mirror().addBox(-5.5F, 0.0F, -1.5F, 5.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.5F, 0.0F, -3.5F));
		PartDefinition rightLeg = legs.addOrReplaceChild("rightLeg", CubeListBuilder.create()
		.texOffs(24, 6).mirror().addBox(-5.5F, 0.0F, -1.5F, 5.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.5F, 0.0F, 3.5F));
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
		.texOffs(0, 0).addBox(-2.0F, -1.5F, -4.0F, 4.0F, 3.0F, 8.0F, new CubeDeformation(0.026F))
		.texOffs(18, 11).addBox(2.0F, -3.5F, -2.0F, 0.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(18, 11).addBox(-2.0F, -3.5F, -2.0F, 0.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.5F, 0.0F));
		PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create()
		.texOffs(0, 11).addBox(-3.0F, -2.0F, -3.5F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(28, 26).addBox(0.0F, -3.0F, -3.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(28, 22).addBox(-2.0F, -2.0F, -7.5F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(28, 26).mirror().addBox(-3.0F, -3.0F, -3.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(1, 28).addBox(-2.0F, -1.0F, -5.5F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, -1.5F, -3.5F));
		PartDefinition jaw = neck.addOrReplaceChild("jaw", CubeListBuilder.create()
		.texOffs(25, 1).addBox(-1.5F, 0.0F, -4.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 0.0F, -3.5F));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
		.texOffs(0, 18).addBox(-1.0F, -1.5F, 0.5F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(18, 11).addBox(1.0F, -3.5F, 0.5F, 0.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(18, 11).addBox(-1.0F, -3.5F, 0.5F, 0.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 3.5F));
		PartDefinition tailTip = tail.addOrReplaceChild("tailTip", CubeListBuilder.create()
		.texOffs(16, 22).addBox(0.0F, -3.5F, 0.0F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 6.5F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	protected void setupAnimations(Alligator entity, float limbSwing, float limbSwingAmount, float ageInTicks, float partialTick, float netHeadYaw, float headPitch) {

		this.animateIdleSmooth(entity.idleAnimationState, AlligatorBabyAnimations.ALLIGATOR_IDLE, ageInTicks, partialTick, limbSwingAmount, IDLE_FADE_SCALE, 0.6F);
		this.animateSmooth(entity.swimAnimationState, AlligatorBabyAnimations.ALLIGATOR_SWIM, ageInTicks, partialTick, movementAnimationSpeed(entity, limbSwingAmount, 1.0F, LARGE_SWIMMER_LIMB_SWING));
		this.animateSmooth(entity.walkAnimationState, AlligatorBabyAnimations.ALLIGATOR_WALK, ageInTicks, partialTick, movementAnimationSpeed(entity, limbSwingAmount, 5.0F));

		applyHeadLook(this.neck, netHeadYaw, headPitch);
	}
}
