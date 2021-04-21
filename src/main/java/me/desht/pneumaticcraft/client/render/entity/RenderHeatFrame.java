package me.desht.pneumaticcraft.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelHeatFrame;
import me.desht.pneumaticcraft.client.util.TintColor;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityHeatFrame;
import me.desht.pneumaticcraft.common.entity.semiblock.EntityTransferGadget;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.heat.TemperatureCategory;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class RenderHeatFrame extends RenderSemiblockBase<EntityHeatFrame> {
    public static final IRenderFactory<EntityHeatFrame> FACTORY = RenderHeatFrame::new;

    private static final ResourceLocation[] TEXTURES = new ResourceLocation[TemperatureCategory.values().length];
    static {
        for (TemperatureCategory tc : TemperatureCategory.values()) {
            TEXTURES[tc.getIndex()] = tc.getTextureName("heat_frame");
        }
    }

    private final ModelHeatFrame model = new ModelHeatFrame();

    private RenderHeatFrame(EntityRendererManager rendererManager) {
        super(rendererManager);
    }

    @Override
    public void render(EntityHeatFrame entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        AxisAlignedBB aabb = entityIn.getBoundingBox();

        matrixStackIn.push();
        matrixStackIn.scale((float) aabb.getXSize(), (float) aabb.getYSize(), (float) aabb.getZSize());
        matrixStackIn.translate(0, 1.5, 0);
        matrixStackIn.rotate(Vector3f.XP.rotationDegrees(180F));
        if (entityIn.getTimeSinceHit() > 0) {
            wobble(entityIn, partialTicks, matrixStackIn);
        }

        IVertexBuilder builder = bufferIn.getBuffer(RenderType.getEntityCutout(getEntityTexture(entityIn)));
        model.render(matrixStackIn, builder, kludgeLightingLevel(entityIn, packedLightIn), OverlayTexture.getPackedUV(0F, false), f[0], f[1], f[2], f[3]);

        matrixStackIn.pop();
    }

    @Override
    public ResourceLocation getEntityTexture(EntityHeatFrame entityHeatFrame) {
        TemperatureCategory tc = TemperatureCategory.forTemperature(entityHeatFrame.getSyncedTemperature());
        return TEXTURES[tc.getIndex()];
    }
}
