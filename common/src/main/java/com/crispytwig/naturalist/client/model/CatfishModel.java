package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.client.model.animation.CatfishAnimations;
import com.crispytwig.naturalist.server.entity.mob.Catfish;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.jspecify.annotations.NonNull;

public class CatfishModel extends NaturalistEntityModel<Catfish> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Naturalist.location("catfish"), "main");
	private final ModelPart root;

	public CatfishModel(ModelPart root) {
		this.root = root.getChild("root");
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
		.texOffs(0, 27).addBox(-3.0F, 3.5F, -1.0F, 6.0F, 4.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-5.0F, -3.5F, -6.0F, 10.0F, 9.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(0.0F, -8.5F, -3.0F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.5F, -2.0F));
		PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create()
		.texOffs(40, 38).addBox(-4.0F, -3.0F, -4.0F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.5F, -6.0F));
		PartDefinition left_whiskers = neck.addOrReplaceChild("left_whiskers", CubeListBuilder.create()
		.texOffs(56, 19).addBox(0.0F, 0.0F, 0.0F, 0.0F, 5.0F, 7.0F, new CubeDeformation(0.01F)), PartPose.offset(4.05F, 2.0F, -4.0F));
		PartDefinition right_whiskers = neck.addOrReplaceChild("right_whiskers", CubeListBuilder.create()
		.texOffs(56, 19).mirror().addBox(0.0F, 0.0F, 0.0F, 0.0F, 5.0F, 7.0F, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(-4.05F, 2.0F, -4.0F));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
		.texOffs(36, 0).addBox(-2.0F, -1.8F, 1.0F, 4.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(26, 13).addBox(0.0F, -5.8F, -1.0F, 0.0F, 11.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.7F, 9.0F));
		PartDefinition left_fin = body.addOrReplaceChild("left_fin", CubeListBuilder.create()
		.texOffs(0, 0).mirror().addBox(-0.5F, -1.0F, 0.0F, 5.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.0F, 4.5F, -3.0F, 0.0F, -0.7854F, 0.0F));
		PartDefinition right_fin = body.addOrReplaceChild("right_fin", CubeListBuilder.create()
		.texOffs(0, 0).addBox(-4.5F, -1.0F, 0.0F, 5.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 4.5F, -3.0F, 0.0F, 0.7854F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	protected void setupAnimations(Catfish entity, float limbSwing, float limbSwingAmount, float ageInTicks, float partialTick, float netHeadYaw, float headPitch) {

		this.animateSmooth(entity.flopAnimationState, CatfishAnimations.CATFISH_FLOP, ageInTicks, partialTick);
		this.animateSmooth(entity.swimAnimationState, CatfishAnimations.CATFISH_SWIM, ageInTicks, partialTick, movementAnimationSpeed(entity, limbSwingAmount, 1.0F, SMALL_SWIMMER_LIMB_SWING));
	}
}
