package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelAirCannon;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAirCannon;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

public class RenderAirCannon extends AbstractModelRenderer<TileEntityAirCannon> {
    private final ModelAirCannon model;

    public RenderAirCannon() {
        model = new ModelAirCannon();
    }

    @Override
    ResourceLocation getTexture(TileEntityAirCannon te) {
        return Textures.MODEL_AIR_CANNON;
    }

    @Override
    void renderModel(TileEntityAirCannon te, float partialTicks) {
        float angle = (float) PneumaticCraftUtils.rotateMatrixByMetadata(te.getBlockMetadata());
        float rotationAngle = te.rotationAngle - angle + 180F;
        model.renderModel(0.0625F, rotationAngle, te.heightAngle);
    }
}
