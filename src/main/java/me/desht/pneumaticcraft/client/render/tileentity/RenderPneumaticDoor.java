package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.model.block.ModelDoor;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.BlockPneumaticDoor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.util.ResourceLocation;

public class RenderPneumaticDoor extends AbstractTileModelRenderer<TileEntityPneumaticDoor> {
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
        return !te.getBlockState().get(BlockPneumaticDoor.TOP_DOOR);
    }

    @Override
    void renderModel(TileEntityPneumaticDoor te, float partialTicks) {
        RenderUtils.rotateMatrixForDirection(te.getRotation());
        float rotation = te.oldRotationAngle + (te.rotationAngle - te.oldRotationAngle) * partialTicks;
        boolean rightGoing = te.rightGoing;
        GlStateManager.translated((rightGoing ? -1 : 1) * 6.5F / 16F, 0, -6.5F / 16F);
        GlStateManager.rotated(rotation, 0, rightGoing ? -1 : 1, 0);
        GlStateManager.translated((rightGoing ? -1 : 1) * -6.5F / 16F, 0, 6.5F / 16F);
        modelDoor.renderModel(0.0625f);
    }
}
