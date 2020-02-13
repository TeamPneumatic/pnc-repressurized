package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.model.block.ModelDoor;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.block.BlockPneumaticDoor;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDoor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

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
        float rotation = MathHelper.lerp(partialTicks, te.oldRotationAngle, te.rotationAngle);
        boolean rightGoing = te.rightGoing;
        float[] rgb = DyeColor.byId(te.color).getColorComponentValues();
        GlStateManager.color3f(rgb[0], rgb[1], rgb[2]);
        GlStateManager.translated((rightGoing ? -1 : 1) * 6.5F / 16F, 0, -6.5F / 16F);
        GlStateManager.rotated(rotation, 0, rightGoing ? -1 : 1, 0);
        GlStateManager.translated((rightGoing ? -1 : 1) * -6.5F / 16F, 0, 6.5F / 16F);
        modelDoor.renderModel(0.0625f);
        GlStateManager.color3f(1f, 1f, 1f);
    }
}
