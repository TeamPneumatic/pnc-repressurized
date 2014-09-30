package pneumaticCraft.common.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pneumaticCraft.common.NBTUtil;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;
import pneumaticCraft.lib.Log;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemProgrammingPuzzle extends ItemPneumatic{

    public ItemProgrammingPuzzle(){
        hasSubtypes = true;
    }

    @Override
    public void registerIcons(IIconRegister par1IconRegister){}

    @Override
    @SideOnly(Side.CLIENT)
    /**
     * allows items to add custom lines of information to the mouseover description
     */
    public void addInformation(ItemStack stack, EntityPlayer player, List par3List, boolean par4){
        par3List.add(I18n.format("programmingPuzzle." + stack.getTagCompound().getString("type") + ".name"));
    }

    @Override
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List){
        for(IProgWidget widget : TileEntityProgrammer.registeredWidgets) {
            par3List.add(getStackForWidgetKey(widget.getWidgetString()));
        }
    }

    public static IProgWidget getWidgetForPiece(ItemStack stack){
        String type = stack.getTagCompound().getString("type");
        for(IProgWidget widget : TileEntityProgrammer.registeredWidgets) {
            if(widget.getLegacyString().equals(type) && !widget.getWidgetString().equals(type)) {
                stack.getTagCompound().setString("type", widget.getWidgetString());
                Log.info("Found legacy piece: " + widget.getLegacyString() + ", transforming to: " + widget.getWidgetString());
                type = stack.getTagCompound().getString("type");
            }
            if(widget.getWidgetString().equals(type)) return widget;
        }
        Exception e = new IllegalArgumentException("No widget registered with the name " + type + "! This is not possible?!");
        e.printStackTrace();
        return null;
    }

    public static IProgWidget getWidgetForClass(Class<? extends IProgWidget> clazz){
        for(IProgWidget widget : TileEntityProgrammer.registeredWidgets) {
            if(widget.getClass() == clazz) return widget;
        }
        throw new IllegalArgumentException("Widget " + clazz.getCanonicalName() + " isn't registered!");
    }

    public static ItemStack getStackForWidgetKey(String widgetKey){
        ItemStack stack = new ItemStack(Itemss.programmingPuzzle, 1, 0);
        NBTUtil.setString(stack, "type", widgetKey);
        return stack;
    }

    //TODO legacy: remove
    @Override
    public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5){
        if(!par2World.isRemote && par2World.getWorldTime() % 40 == 0) {
            getWidgetForPiece(par1ItemStack);
        }
    }
}
