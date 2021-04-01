package me.desht.pneumaticcraft.client.render.entity.drone;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDroneCore;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.core.ModEntities;
import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.DyeColor;

public class DroneColourLayer extends LayerRenderer<EntityDroneBase, ModelDrone> {
    private final ModelDroneCore model = new ModelDroneCore();

    DroneColourLayer(IEntityRenderer<EntityDroneBase, ModelDrone> rendererIn) {
        super(rendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, EntityDroneBase entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        float[] cols = RenderUtils.decomposeColorF(DyeColor.byId(entityIn.getDroneColor()).getColorValue());
        IVertexBuilder builder = bufferIn.getBuffer(RenderType.entityCutoutNoCull(Textures.DRONE_ENTITY));
        model.renderToBuffer(matrixStackIn, builder, packedLightIn, LivingRenderer.getOverlayCoords(entityIn, 0.0F), cols[1], cols[2], cols[3], 1f);
    }
}
