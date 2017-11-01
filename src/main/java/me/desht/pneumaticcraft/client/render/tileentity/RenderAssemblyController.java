package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.gui.GuiPneumaticContainerBase;
import me.desht.pneumaticcraft.client.model.block.ModelAssemblyControllerScreen;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyController;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

public class RenderAssemblyController extends AbstractModelRenderer<TileEntityAssemblyController> {
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
        PneumaticCraftUtils.rotateMatrixByMetadata(2);

        // have the screen face the player
        GlStateManager.rotate(180 + Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);

        model.renderModel(0.0625f);

        // status text & possible problem icon
        double textSize = 1 / 100D;
        GlStateManager.translate(-0.25D, 0.53D, 0.04D);
        GlStateManager.rotate(-34, 1, 0, 0);
        GlStateManager.scale(textSize, textSize, textSize);
        GlStateManager.disableLighting();
        Minecraft.getMinecraft().fontRenderer.drawString(te.displayedText, 1, 4, 0xFFFFFFFF);
        if(te.hasProblem) {
            GuiPneumaticContainerBase.drawTexture(Textures.GUI_PROBLEMS_TEXTURE, 28, 12);
        }
        GlStateManager.enableLighting();
    }
}
