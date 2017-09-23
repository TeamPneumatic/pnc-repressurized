package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.WidgetRegistrator;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ItemProgrammingPuzzle extends ItemPneumaticSubtyped {

    public ItemProgrammingPuzzle() {
        super("programming_puzzle");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> par3List, ITooltipFlag par4) {
        super.addInformation(stack, world, par3List, par4);
        par3List.add(new ItemStack(Itemss.PLASTIC, 1, stack.getItemDamage()).getDisplayName());
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName(stack) + "." + EnumDyeColor.byDyeDamage(MathHelper.clamp(stack.getItemDamage(), 0, 15));
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            addItems(items);
        }
    }

    public static void addItems(NonNullList<ItemStack> list) {
        for (int i = 0; i < 16; i++) {
            for (IProgWidget widget : WidgetRegistrator.registeredWidgets) {
                if (widget.getCraftingColorIndex() == i) {
                    list.add(new ItemStack(Itemss.PROGRAMMING_PUZZLE, 1, i));
                    break;
                }
            }
        }
    }

//    @Override
//    public void registerItemVariants() {
//        ResourceLocation resLoc = new ResourceLocation(Names.MOD_ID, getUnlocalizedName().substring(5));
//        ModelBakery.registerItemVariants(this, resLoc);
//        for (int i = 0; i < 16; i++)
//            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(this, i, new ModelResourceLocation(resLoc, "inventory"));
//    }

    public static IProgWidget getWidgetForPiece(ItemStack stack) {
        List<IProgWidget> widgets = getWidgetsForColor(stack.getItemDamage());
        if (widgets.size() > 0) {
            World world = PneumaticCraftRepressurized.proxy.getClientWorld();
            if (world == null) return null;
            return widgets.get((int) (world.getTotalWorldTime() % (widgets.size() * 20) / 20));
        } else {
            return null;
        }
    }

    private static List<IProgWidget> getWidgetsForColor(int color) {
        List<IProgWidget> widgets = new ArrayList<IProgWidget>();
        for (IProgWidget widget : WidgetRegistrator.registeredWidgets) {
            if (widget.getCraftingColorIndex() == color) {
                widgets.add(widget);
            }
        }
        return widgets;
    }

    public static ItemStack getStackForColor(int color) {
        return new ItemStack(Itemss.PROGRAMMING_PUZZLE, 1, color);
    }

    public static ItemStack getStackForWidgetKey(String widgetKey) {
        /*for(IProgWidget widget : TileEntityProgrammer.registeredWidgets) {
            if(widget.getWidgetString().equals(widgetKey)) {
                return new ItemStack(Itemss.programmingPuzzle, 1, widget.getCraftingColorIndex());
            }
        }*/
        ItemStack stack = new ItemStack(Itemss.PROGRAMMING_PUZZLE);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("type", widgetKey);
        stack.setTagCompound(tag);
        return stack;
        //    throw new IllegalArgumentException("No widget registered with the name " + widgetKey + "! This is not possible?!");
    }

    public static IProgWidget getWidgetForClass(Class<? extends IProgWidget> clazz) {
        for (IProgWidget widget : WidgetRegistrator.registeredWidgets) {
            if (widget.getClass() == clazz) return widget;
        }
        throw new IllegalArgumentException("Widget " + clazz.getCanonicalName() + " isn't registered!");
    }

    public static IProgWidget getWidgetForName(String name) {
        for (IProgWidget widget : WidgetRegistrator.registeredWidgets) {
            if (widget.getWidgetString().equals(name)) return widget;
        }
        throw new IllegalArgumentException("Widget " + name + " isn't registered!");
    }

    @Override
    public String getSubtypeModelName(int meta) {
        return "programming_puzzle";
    }

}
