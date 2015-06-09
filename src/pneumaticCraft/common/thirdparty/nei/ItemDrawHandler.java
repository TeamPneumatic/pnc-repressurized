package pneumaticCraft.common.thirdparty.nei;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.render.pneumaticArmor.RenderSearchItemBlock;
import pneumaticCraft.common.item.ItemPneumaticArmor;
import pneumaticCraft.lib.Textures;
import codechicken.nei.guihook.IContainerDrawHandler;
import cpw.mods.fml.client.FMLClientHandler;

public class ItemDrawHandler implements IContainerDrawHandler{
    private ItemStack searchStack;

    @Override
    public void onPreDraw(GuiContainer gui){
        searchStack = ItemPneumaticArmor.getSearchedStack();
    }

    @Override
    public void renderObjects(GuiContainer gui, int mousex, int mousey){}

    @Override
    public void postRenderObjects(GuiContainer gui, int mousex, int mousey){}

    @Override
    public void renderSlotUnderlay(GuiContainer gui, Slot slot){
        if(slot.getHasStack() && searchStack != null && RenderSearchItemBlock.getSearchedItemCount(slot.getStack(), searchStack) > 0) {
            GL11.glEnable(GL11.GL_BLEND);
            FMLClientHandler.instance().getClient().getTextureManager().bindTexture(Textures.ITEM_SEARCH_OVERLAY);
            Gui.func_146110_a(slot.xDisplayPosition, slot.yDisplayPosition, 0, 0, 16, 16, 16, 16);
        }
    }

    @Override
    public void renderSlotOverlay(GuiContainer gui, Slot slot){}

}
