package pneumaticExample;

import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.api.client.assemblymachine.AssemblyRenderOverriding.IAssemblyRenderOverriding;

public class AssemblyRenderOverrideExample implements IAssemblyRenderOverriding{

    @Override
    public boolean applyRenderChangeIOUnit(ItemStack renderedStack){
        return true;
    }

    @Override
    public boolean applyRenderChangePlatform(ItemStack renderedStack){
        GL11.glTranslated(0, -0.5D, 0);
        return true;
    }

    @Override
    public float getIOUnitClawShift(ItemStack renderedStack){
        return 0.0875F;
    }

    @Override
    public float getPlatformClawShift(ItemStack renderedStack){
        return 0.0875F;
    }

}
