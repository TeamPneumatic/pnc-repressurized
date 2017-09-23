package igwmod.gui;

import java.awt.Rectangle;

import igwmod.api.WikiRegistry;
import net.minecraft.item.ItemStack;

public class LocatedStack implements IReservedSpace, IPageLink{
    public ItemStack stack;
    public int x, y;

    public LocatedStack(ItemStack stack, int x, int y){
        this.stack = stack;
        this.x = x;
        this.y = y;
    }

    @Override
    public Rectangle getReservedSpace(){
        return new Rectangle((int)(x / GuiWiki.TEXT_SCALE), (int)(y / GuiWiki.TEXT_SCALE), (int)(16 / GuiWiki.TEXT_SCALE), (int)(16 / GuiWiki.TEXT_SCALE));
    }

    @Override
    public boolean onMouseClick(GuiWiki gui, int x, int y){
        return false;//Don't do anything, as pagelinking will be handled by the container call when a slot gets clicked.
    }

    @Override
    public void renderBackground(GuiWiki gui, int mouseX, int mouseY){} //Rendering will be done by the GuiContainer.

    @Override
    public void renderForeground(GuiWiki gui, int mouseX, int mouseY){}

    @Override
    public String getName(){
        return stack.getItem() == null ? "<STACK HAS NO ITEM!>" : stack.getDisplayName();
    }

    @Override
    public String getLinkAddress(){
        return WikiRegistry.getPageForItemStack(stack);
    }

    @Override
    public void setX(int x){
        this.x = x;
    }

    @Override
    public void setY(int y){
        this.y = y;
    }

    @Override
    public int getX(){
        return x;
    }

    @Override
    public int getY(){
        return y;
    }

    @Override
    public int getHeight(){
        return 16;
    }
}
