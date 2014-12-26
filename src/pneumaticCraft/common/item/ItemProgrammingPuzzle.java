package pneumaticCraft.common.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.NBTUtil;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;
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
        par3List.add(I18n.format("gui.tooltip.plasticPlant", I18n.format(Itemss.plasticPlant.getUnlocalizedName(stack) + ".name")));
    }

    @Override
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List){
        addItems(par3List);
    }

    public static void addItems(List<ItemStack> list){
        for(int i = 0; i < 16; i++) {
            for(IProgWidget widget : TileEntityProgrammer.registeredWidgets) {
                if(widget.getCraftingColorIndex() == i) {
                    list.add(new ItemStack(Itemss.programmingPuzzle, 1, i));
                    break;
                }
            }
        }
    }

    public static IProgWidget getWidgetForPiece(ItemStack stack){
        if(NBTUtil.hasTag(stack, "type")) {//TODO legacy remove
            String type = stack.getTagCompound().getString("type");
            for(IProgWidget widget : TileEntityProgrammer.registeredWidgets) {
                if(widget.getWidgetString().equals(type)) return widget;
            }
            Exception e = new IllegalArgumentException("No widget registered with the name " + type + "! This is not possible?!");
            e.printStackTrace();
            return null;
        } else {
            List<IProgWidget> widgets = getWidgetsForColor(stack.getItemDamage());
            if(widgets.size() > 0) {
                World world = PneumaticCraft.proxy.getClientWorld();
                return widgets.get((int)(world.getWorldTime() % (widgets.size() * 20) / 20));
            } else {
                return null;
            }
        }
    }

    private static List<IProgWidget> getWidgetsForColor(int color){
        List<IProgWidget> widgets = new ArrayList<IProgWidget>();
        for(IProgWidget widget : TileEntityProgrammer.registeredWidgets) {
            if(widget.getCraftingColorIndex() == color) {
                widgets.add(widget);
            }
        }
        return widgets;
    }

    public static ItemStack getStackForColor(int color){
        return new ItemStack(Itemss.programmingPuzzle, 1, color);
    }

    public static ItemStack getStackForWidgetKey(String widgetKey){
        for(IProgWidget widget : TileEntityProgrammer.registeredWidgets) {
            if(widget.getWidgetString().equals(widgetKey)) {
                return new ItemStack(Itemss.programmingPuzzle, 1, widget.getCraftingColorIndex());
            }
        }
        throw new IllegalArgumentException("No widget registered with the name " + widgetKey + "! This is not possible?!");
    }

    public static IProgWidget getWidgetForClass(Class<? extends IProgWidget> clazz){
        for(IProgWidget widget : TileEntityProgrammer.registeredWidgets) {
            if(widget.getClass() == clazz) return widget;
        }
        throw new IllegalArgumentException("Widget " + clazz.getCanonicalName() + " isn't registered!");
    }

}
