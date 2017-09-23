package me.desht.pneumaticcraft.common.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemPlastic extends ItemPneumaticSubtyped {

    public static final int BLACK = 0;
    public static final int RED = 1;
    public static final int GREEN = 2;
    public static final int BROWN = 3;
    public static final int BLUE = 4;
    public static final int PURPLE = 5;
    public static final int CYAN = 6;
    public static final int LIGHT_GREY = 7;
    public static final int GREY = 8;
    public static final int PINK = 9;
    public static final int LIME = 10;
    public static final int YELLOW = 11;
    public static final int LIGHT_BLUE = 12;
    public static final int MAGENTA = 13;
    public static final int ORANGE = 14;
    public static final int WHITE = 15;

    public ItemPlastic() {
        super("plastic");
        setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        for (int i = 0; i < 16; i++) {
//            if (i == ItemPlastic.ADRENALINE_PLANT_DAMAGE) continue;
//            if (i == ItemPlastic.MUSIC_PLANT_DAMAGE) continue;
            subItems.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public int getMetadata(int meta) {
        return meta;
    }

    public static int getColour(ItemStack iStack) {
        switch (iStack.getItemDamage()) {
            case ItemPlastic.LIGHT_GREY:
                return 0xb1b1b1;  // light grey
            case ItemPlastic.GREY:
                return 0x848484; // grey
            case ItemPlastic.LIGHT_BLUE:
                return 0x82ace7; // light blue
            case ItemPlastic.GREEN:
                return 0x4a6b18;
            case ItemPlastic.PURPLE:
                return 0x8230b2;
            case ItemPlastic.RED:
                return 0xa72222;
            case ItemPlastic.WHITE:
                return 0xffffff;
            case ItemPlastic.YELLOW:
                return 0xe5e62a;
            case ItemPlastic.CYAN:
                return 0x1a6482;
            case ItemPlastic.MAGENTA:
                return 0xbe5cb8;
            case ItemPlastic.PINK:
                return 0xf7b4d6;
            case ItemPlastic.ORANGE:
                return 0xe69e34;
            case ItemPlastic.BLUE:
                return 0x0a2b7a;
            case ItemPlastic.LIME:
                return 0x83d41c;
            case ItemPlastic.BROWN:
                return 0x795400;
            case ItemPlastic.BLACK:
                return 0x000000;
        }
        return -1;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName(stack) + "." + EnumDyeColor.byDyeDamage(MathHelper.clamp(stack.getItemDamage(), 0, 15));
    }

    @Override
    public String getSubtypeModelName(int meta) {
        return "plastic";
    }
}
