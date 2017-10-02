package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.gui.GuiPneumaticContainerBase;
import me.desht.pneumaticcraft.client.model.block.ModelAssemblyControllerScreen;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyController;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

public class RenderAssemblyController extends TileEntitySpecialRenderer<TileEntityAssemblyController> {
    private final ModelAssemblyControllerScreen model;

    public RenderAssemblyController() {
        model = new ModelAssemblyControllerScreen();
    }

    @Override
    public void render(TileEntityAssemblyController te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();

        GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
        GL11.glScalef(1.0F, -1F, -1F);
        PneumaticCraftUtils.rotateMatrixByMetadata(2);

        // have the screen face the player
        GL11.glRotatef(180 + Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);

        FMLClientHandler.instance().getClient().getTextureManager().bindTexture(Textures.MODEL_ASSEMBLY_CONTROLLER);
        model.renderModel(0.0625f);

        double textSize = 1 / 100D;
        GL11.glTranslated(-0.25D, 0.53D, 0.04D);
        GL11.glRotated(-34, 1, 0, 0);
        GL11.glScaled(textSize, textSize, textSize);
        GL11.glDisable(GL11.GL_LIGHTING);
        Minecraft.getMinecraft().fontRenderer.drawString(te.displayedText, 1, 4, 0xFFFFFFFF);
        if(te.hasProblem) GuiPneumaticContainerBase.drawTexture(Textures.GUI_PROBLEMS_TEXTURE, 28, 12);
        GL11.glEnable(GL11.GL_LIGHTING);

        GlStateManager.popMatrix();
    }
}
