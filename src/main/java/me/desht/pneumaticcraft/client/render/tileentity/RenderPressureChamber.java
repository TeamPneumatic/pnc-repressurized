package me.desht.pneumaticcraft.client.render.tileentity;

import me.desht.pneumaticcraft.client.ClientTickHandler;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberValve;
import me.desht.pneumaticcraft.common.util.ItemStackHandlerIterable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.stream.Collectors;

public class RenderPressureChamber extends TileEntitySpecialRenderer<TileEntityPressureChamberValve> {

    @Override
    public void render(TileEntityPressureChamberValve te, double x, double y, double z, float partialTicks, int destroyStage, float alpha){

        List<ItemStack> stacks = new ItemStackHandlerIterable(te.getStacksInChamber())
                                        .stream()
                                        .filter(stack -> !stack.isEmpty())
                                        .collect(Collectors.toList());
        
        if(!stacks.isEmpty()){
            x += te.multiBlockX - te.getPos().getX() + te.multiBlockSize / 2D;
            y += te.multiBlockY - te.getPos().getY() + 1.1; //Set to '+ 1' for normal y value.
            z += te.multiBlockZ - te.getPos().getZ() + te.multiBlockSize / 2D;
            
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            
            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
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

            for(int i = 0; i < stacks.size(); i++){
                GlStateManager.pushMatrix();
                GlStateManager.rotate(i * degreesPerStack, 0, 1, 0);
                GlStateManager.translate(circleRadius, yBob, 0);

                GlStateManager.rotate(yRot, 0, 1, 0);
                Minecraft.getMinecraft().getRenderItem().renderItem(stacks.get(i), ItemCameraTransforms.TransformType.GROUND);

                GlStateManager.popMatrix();
            }

            renderManager.options.fancyGraphics = fancySetting;
            
            GlStateManager.popMatrix();
        }
    }
}
