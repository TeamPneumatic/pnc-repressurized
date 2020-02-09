package me.desht.pneumaticcraft.client.render.tileentity;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import me.desht.pneumaticcraft.client.event.ClientTickHandler;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberValve;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class RenderPressureChamber extends TileEntityRenderer<TileEntityPressureChamberValve> {

    @Override
    public void render(TileEntityPressureChamberValve te, double x, double y, double z, float partialTicks, int destroyStage) {
        if (te.multiBlockSize == 0 || !te.hasGlass) return;

        List<ItemStack> stacks = te.renderedItems;
        if (!stacks.isEmpty()){
            x += te.multiBlockX - te.getPos().getX() + te.multiBlockSize / 2D;
            y += te.multiBlockY - te.getPos().getY() + 1.1; //Set to '+ 1' for normal y value.
            z += te.multiBlockZ - te.getPos().getZ() + te.multiBlockSize / 2D;
            
            GlStateManager.pushMatrix();
            GlStateManager.translated(x, y, z);
            
            EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
            boolean fancySetting = renderManager.options.fancyGraphics;
            renderManager.options.fancyGraphics = true;

            // render single item centered (looks best), multiple items arranged in a circle
            // around the centre of the chamber, radius dependent on chamber size
            float circleRadius = stacks.size() == 1 ? 0 : 0.33f * (te.multiBlockSize - 2);
            float degreesPerStack = 360f / stacks.size();

            // some gentle rotation and bobbing looks good here
            double ticks = ClientTickHandler.TICKS + partialTicks;
            float yBob = MathHelper.sin(((float) ticks  / 10) % 360) * 0.01f;
            float yRot = (float) (ticks / 2) % 360;

            int light = te.getWorld().getCombinedLight(te.getPos().offset(te.getRotation()), 0);  // otherwise it will render unlit
            GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float) (light & 0xFFFF), (float) ((light >> 16) & 0xFFFF));

            for (int i = 0; i < stacks.size(); i++){
                GlStateManager.pushMatrix();
                GlStateManager.rotated(i * degreesPerStack, 0, 1, 0);
                GlStateManager.translated(circleRadius, yBob, 0);

                GlStateManager.rotated(yRot, 0, 1, 0);
                Minecraft.getInstance().getItemRenderer().renderItem(stacks.get(i), ItemCameraTransforms.TransformType.GROUND);

                GlStateManager.popMatrix();
            }

            renderManager.options.fancyGraphics = fancySetting;
            
            GlStateManager.popMatrix();
        }
    }
}
