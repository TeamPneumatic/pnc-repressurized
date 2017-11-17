package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.model.block.ModelDoor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoor;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class RenderPneumaticDoor extends AbstractModelRenderer<TileEntityPneumaticDoor> {
    private final ModelDoor modelDoor;

    public RenderPneumaticDoor() {
        modelDoor = new ModelDoor();
    }

    @Override
    ResourceLocation getTexture(TileEntityPneumaticDoor te) {
        return Textures.MODEL_PNEUMATIC_DOOR_DYNAMIC;
    }

    @Override
    protected boolean shouldRender(TileEntityPneumaticDoor te) {
        return te.rotationAngle > 0f && te.rotationAngle < 90f;
    }

    @Override
    void renderModel(TileEntityPneumaticDoor te, float partialTicks) {
        PneumaticCraftUtils.rotateMatrixByMetadata(te.getBlockMetadata() % 6);
        float rotation = te.oldRotationAngle + (te.rotationAngle - te.oldRotationAngle) * partialTicks;
        boolean rightGoing = te.rightGoing;
        GlStateManager.translate((rightGoing ? -1 : 1) * 6.5F / 16F, 0, -6.5F / 16F);
        GlStateManager.rotate(rotation, 0, rightGoing ? -1 : 1, 0);
        GlStateManager.translate((rightGoing ? -1 : 1) * -6.5F / 16F, 0, 6.5F / 16F);
        if (te.getBlockMetadata() < 6) {
            modelDoor.renderModel(0.0625f);
        }
    }
}
