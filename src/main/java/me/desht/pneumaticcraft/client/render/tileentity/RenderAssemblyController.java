package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.model.block.ModelAssemblyControllerScreen;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyController;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

public class RenderAssemblyController extends AbstractTileModelRenderer<TileEntityAssemblyController> {
    private final ModelAssemblyControllerScreen model;

    public RenderAssemblyController() {
        model = new ModelAssemblyControllerScreen();
    }

    @Override
    ResourceLocation getTexture(TileEntityAssemblyController te) {
        return Textures.MODEL_ASSEMBLY_CONTROLLER;
    }

    @Override
    void renderModel(TileEntityAssemblyController te, float partialTicks) {
        RenderUtils.rotateMatrixByMetadata(Direction.NORTH);

        // have the screen face the player
        GlStateManager.rotated(180 + Minecraft.getInstance().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);

        model.renderModel(0.0625f);

        // status text & possible problem icon
        double textSize = 1 / 100D;
        GlStateManager.translated(-0.25D, 0.53D, 0.04D);
        GlStateManager.rotated(-34, 1, 0, 0);
        GlStateManager.scaled(textSize, textSize, textSize);
        GlStateManager.disableLighting();
        Minecraft.getInstance().fontRenderer.drawString(te.displayedText, 1, 4, 0xFFFFFFFF);
        if(te.hasProblem) {
            GuiUtils.drawTexture(Textures.GUI_PROBLEMS_TEXTURE, 28, 12);
        }
        GlStateManager.enableLighting();
    }
}
