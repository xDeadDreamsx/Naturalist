package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.client.model.animation.AlligatorAnimations;
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

public class AlligatorModel extends NaturalistEntityModel<Alligator> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Naturalist.location("alligator"), "main");
	private final ModelPart root;
	private final ModelPart neck;

	public AlligatorModel(ModelPart root) {
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
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
		.texOffs(0, 0).addBox(-6.5F, -4.0F, -12.0F, 13.0F, 9.0F, 24.0F, new CubeDeformation(0.0F))
		.texOffs(0, 33).addBox(-5.5F, -5.0F, -10.0F, 11.0F, 1.0F, 19.0F, new CubeDeformation(0.0F))
		.texOffs(41, 34).addBox(-2.5F, -5.0F, -10.0F, 5.0F, 1.0F, 19.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 6.0F));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
		.texOffs(50, 0).addBox(-3.5F, -1.0F, -2.0F, 7.0F, 8.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(65, 54).addBox(-1.5F, -4.0F, -2.0F, 3.0F, 3.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 14.0F));
		PartDefinition tail2 = tail.addOrReplaceChild("tail2", CubeListBuilder.create()
		.texOffs(23, 54).addBox(-1.5F, -4.0F, 0.0F, 3.0F, 8.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(0, 65).addBox(-1.5F, -7.0F, 0.0F, 3.0F, 3.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 11.0F));
		PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create()
		.texOffs(76, 75).addBox(-4.5F, -3.0F, -6.0F, 9.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 15).addBox(0.5F, -5.0F, -6.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 15).mirror().addBox(-4.5F, -5.0F, -6.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(77, 0).addBox(-4.5F, 1.0F, -6.0F, 9.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -12.0F));
		PartDefinition snout = neck.addOrReplaceChild("snout", CubeListBuilder.create()
		.texOffs(42, 54).addBox(-4.5F, -4.0F, -7.0F, 9.0F, 4.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(91, 14).addBox(-4.5F, -5.0F, -7.0F, 9.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(88, 37).addBox(-4.5F, 0.0F, -7.0F, 9.0F, 2.0F, 9.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 1.0F, -8.0F));
		PartDefinition lower_jaw = snout.addOrReplaceChild("lower_jaw", CubeListBuilder.create()
		.texOffs(46, 70).addBox(-4.5F, 0.0F, -9.0F, 9.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(84, 23).addBox(-4.5F, -2.0F, -9.0F, 9.0F, 2.0F, 9.0F, new CubeDeformation(-0.1F)), PartPose.offset(0.0F, 0.0F, 2.0F));
		PartDefinition legs = root.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(7.0F, -7.0F, -5.0F));
		PartDefinition left_arm = legs.addOrReplaceChild("left_arm", CubeListBuilder.create()
		.texOffs(0, 0).addBox(-0.5F, -2.0F, -3.0F, 5.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition left_hand = left_arm.addOrReplaceChild("left_hand", CubeListBuilder.create()
		.texOffs(0, 53).addBox(-6.0F, 0.0F, -7.0F, 12.0F, 0.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 7.0F, -1.0F));
		PartDefinition right_arm = legs.addOrReplaceChild("right_arm", CubeListBuilder.create()
		.texOffs(0, 0).mirror().addBox(-4.5F, -2.0F, -3.0F, 5.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-14.0F, 0.0F, 0.0F));
		PartDefinition right_hand = right_arm.addOrReplaceChild("right_hand", CubeListBuilder.create()
		.texOffs(0, 53).mirror().addBox(-6.0F, 0.0F, -7.0F, 12.0F, 0.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.5F, 7.0F, -1.0F));
		PartDefinition left_leg = legs.addOrReplaceChild("left_leg", CubeListBuilder.create()
		.texOffs(0, 0).addBox(-0.5F, -2.0F, -3.0F, 5.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 22.0F));
		PartDefinition left_foot = left_leg.addOrReplaceChild("left_foot", CubeListBuilder.create()
		.texOffs(0, 53).addBox(-4.0F, 0.0F, -8.0F, 12.0F, 0.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 7.0F, -1.0F));
		PartDefinition right_leg = legs.addOrReplaceChild("right_leg", CubeListBuilder.create()
		.texOffs(0, 0).mirror().addBox(-4.5F, -2.0F, -3.0F, 5.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-14.0F, 0.0F, 22.0F));
		PartDefinition right_foot = right_leg.addOrReplaceChild("right_foot", CubeListBuilder.create()
		.texOffs(0, 53).mirror().addBox(-8.0F, 0.0F, -8.0F, 12.0F, 0.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.5F, 7.0F, -1.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	protected void setupAnimations(Alligator entity, float limbSwing, float limbSwingAmount, float ageInTicks, float partialTick, float netHeadYaw, float headPitch) {

		this.animateSmooth(entity.biteAnimationState, AlligatorAnimations.ALLIGATOR_BITE, ageInTicks, partialTick);

		this.animateIdleSmooth(entity.idleAnimationState, AlligatorAnimations.ALLIGATOR_IDLE, ageInTicks, partialTick, limbSwingAmount, IDLE_FADE_SCALE, 0.6F);
		this.animateSmooth(entity.swimAnimationState, AlligatorAnimations.ALLIGATOR_SWIM, ageInTicks, partialTick, movementAnimationSpeed(entity, limbSwingAmount, 1.0F, LARGE_SWIMMER_LIMB_SWING));
		this.animateSmooth(entity.walkAnimationState, AlligatorAnimations.ALLIGATOR_WALK, ageInTicks, partialTick, movementAnimationSpeed(entity, limbSwingAmount, 3.0F));

		applyHeadLook(this.neck, netHeadYaw, headPitch);
	}
}
