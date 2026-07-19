package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.client.model.animation.BassAnimations;
import com.crispytwig.naturalist.server.entity.mob.Bass;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.jspecify.annotations.NonNull;

public class BassModel extends NaturalistEntityModel<Bass> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Naturalist.location("bass"), "main");
	private final ModelPart root;

	public BassModel(ModelPart root) {
		this.root = root.getChild("root");
	}

	@Override
	public @NonNull ModelPart root() {
		return this.root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 1.5F));
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
		.texOffs(0, 0).addBox(-2.0F, -2.8333F, -6.5F, 4.0F, 6.0F, 11.0F, new CubeDeformation(0.0F))
		.texOffs(0, 5).addBox(0.0F, -5.8333F, -5.5F, 0.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(0, 10).addBox(0.0F, 3.1667F, -4.5F, 0.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -5.1667F, -1.5F));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
		.texOffs(0, 16).addBox(0.0F, -2.0F, 0.0F, 0.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.8333F, 4.5F));
		PartDefinition left_fin = body.addOrReplaceChild("left_fin", CubeListBuilder.create(), PartPose.offset(2.0F, 1.1667F, -3.5F));
		PartDefinition left_fin_r1 = left_fin.addOrReplaceChild("left_fin_r1", CubeListBuilder.create()
		.texOffs(0, 0).addBox(2.0F, -16.0F, -5.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.2429F, 15.0F, 1.2297F, 0.0F, -0.9599F, 0.0F));
		PartDefinition right_fin = body.addOrReplaceChild("right_fin", CubeListBuilder.create(), PartPose.offset(-2.0F, 1.1667F, -3.5F));
		PartDefinition right_fin_r1 = right_fin.addOrReplaceChild("right_fin_r1", CubeListBuilder.create()
		.texOffs(0, 0).mirror().addBox(-6.0F, -16.0F, -5.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.2429F, 15.0F, 1.2297F, 0.0F, 0.9599F, 0.0F));
		PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create()
		.texOffs(19, 0).addBox(-2.0F, -2.5F, -2.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 0.6667F, -7.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	protected void setupAnimations(Bass entity, float limbSwing, float limbSwingAmount, float ageInTicks, float partialTick, float netHeadYaw, float headPitch) {

		this.animateSmooth(entity.flopAnimationState, BassAnimations.BASS_FLOP, ageInTicks, partialTick);
		this.animateSmooth(entity.swimAnimationState, BassAnimations.BASS_SWIM, ageInTicks, partialTick, movementAnimationSpeed(entity, limbSwingAmount, 1.0F, SMALL_SWIMMER_LIMB_SWING));
	}
}
