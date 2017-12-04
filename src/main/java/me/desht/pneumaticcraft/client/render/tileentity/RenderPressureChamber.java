package me.desht.pneumaticcraft.client.render.tileentity;

import java.util.List;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL11;

import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.client.model.block.ModelChargingStation;
import me.desht.pneumaticcraft.client.render.tileentity.AbstractModelRenderer.NoBobItemRenderer;
import me.desht.pneumaticcraft.common.tileentity.TileEntityChargingStation;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPressureChamberValve;
import me.desht.pneumaticcraft.common.util.ItemStackHandlerIterable;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

public class RenderPressureChamber extends TileEntitySpecialRenderer<TileEntityPressureChamberValve> {
   
    private EntityItem ghostEntityItem;
    private NoBobItemRenderer customRenderItem = new AbstractModelRenderer.NoBobItemRenderer();
    
    @Override
    public void render(TileEntityPressureChamberValve te, double x, double y, double z, float partialTicks, int destroyStage, float alpha){
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);

        List<ItemStack> stacks = new ItemStackHandlerIterable(te.getStacksInChamber())
                                        .stream()
                                        .filter(stack -> !stack.isEmpty())
                                        .collect(Collectors.toList());
        
        if(!stacks.isEmpty()){
            if (ghostEntityItem == null) {
                ghostEntityItem = new EntityItem(te.getWorld());
                ghostEntityItem.hoverStart = 0.0F;
                
                customRenderItem = new AbstractModelRenderer.NoBobItemRenderer();
            }
          
            x += te.multiBlockX - te.getPos().getX() + te.multiBlockSize / 2D;
            y += te.multiBlockY - te.getPos().getY() + 5; //Set to '+ 1' for normal y value.
            z += te.multiBlockZ - te.getPos().getZ() + te.multiBlockSize / 2D;
            
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            
            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            boolean fancySetting = renderManager.options.fancyGraphics;
            renderManager.options.fancyGraphics = true;
            
            float circleRadius = 1;
            float degreesPerStack = 360f / stacks.size();
            
            for(int i = 0; i < stacks.size(); i++){
                GlStateManager.pushMatrix();
                GlStateManager.rotate(i * degreesPerStack, 0, 1, 0);
                GlStateManager.translate(circleRadius, 0, 0);
                
                ghostEntityItem.setItem(stacks.get(i));
                customRenderItem.doRender(ghostEntityItem, 0, 0, 0, 0, 0);
                
                GlStateManager.popMatrix();
            }
            
            renderManager.options.fancyGraphics = fancySetting;
            
            GlStateManager.popMatrix();
        }
    }
}
