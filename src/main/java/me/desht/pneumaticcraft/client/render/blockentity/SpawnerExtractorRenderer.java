/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.render.fluid.AbstractFluidTER;
import me.desht.pneumaticcraft.client.render.fluid.TankRenderInfo;
import me.desht.pneumaticcraft.common.block.entity.spawning.SpawnerExtractorBlockEntity;
import me.desht.pneumaticcraft.common.registry.ModFluids;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;

public class SpawnerExtractorRenderer extends AbstractBlockEntityModelRenderer<SpawnerExtractorBlockEntity> {
    private static final AABB FLUID_BB = new AABB(6/16D, 0, 6/16D, 10/16D, 1, 10/16D);

    private static final String MODEL = "model";

    private final ModelPart model;

    public SpawnerExtractorRenderer(BlockEntityRendererProvider.Context ctx) {
        super(ctx);

        ModelPart root = ctx.bakeLayer(PNCModelLayers.SPAWNER_EXTRACTOR);
        model = root.getChild(MODEL);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild(MODEL, CubeListBuilder.create().texOffs(0, 0)
                        .addBox("model_0", -3.0F, -16.25F, -3.0F, 6, 1, 6, new CubeDeformation(-0.01F), 23, 57)
                        .addBox("model_1", -3.0F, -14.75F, -3.0F, 6, 1, 6, new CubeDeformation(-0.01F), 23, 57)
                        .addBox("model_2", -2.5F, -15.5F, -2.5F, 5, 1, 5, new CubeDeformation(-0.01F), 44, 57)
                        .addBox("model_3", -1.0F, -16.0F, -1.0F, 2, 16, 2, 15, 46)
                        .mirror(),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    @Override
    void renderModel(SpawnerExtractorBlockEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        VertexConsumer builder = bufferIn.getBuffer(RenderType.entityCutout(Textures.MODEL_SPAWNER_EXTRACTOR));

        float extension = te.getProgress() * -0.75f;

        matrixStackIn.translate(0, extension, 0);
        model.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);
    }

    @Override
    protected void renderExtras(SpawnerExtractorBlockEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource iRenderTypeBuffer, int combinedLightIn, int combinedOverlayIn) {
        if (te.getProgress() > 0f && te.getProgress() < 1f) {
            matrixStack.pushPose();
            matrixStack.translate(0, 13/16D, 0);
            VertexConsumer builder = iRenderTypeBuffer.getBuffer(RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS));
            TankRenderInfo info = new TankRenderInfo(new FluidStack(ModFluids.MEMORY_ESSENCE.get(), (int) (1000 * te.getProgress())), 1000, FLUID_BB);
            AbstractFluidTER.renderFluid(builder, info, matrixStack.last().pose(), combinedLightIn, combinedOverlayIn);
            matrixStack.popPose();
        }
    }

    @Override
    public AABB getRenderBoundingBox(SpawnerExtractorBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
    }
}
