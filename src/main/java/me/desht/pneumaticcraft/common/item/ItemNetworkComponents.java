package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.client.gui.GuiSecurityStationHacking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemNetworkComponents extends ItemPneumaticSubtyped implements IProgrammable {
    public static final int COMPONENT_AMOUNT = 6;

    public static final int DIAGNOSTIC_SUBROUTINE = 0;
    public static final int NETWORK_API = 1;
    public static final int NETWORK_DATA_STORAGE = 2;
    public static final int NETWORK_IO_PORT = 3;
    public static final int NETWORK_REGISTRY = 4;
    public static final int NETWORK_NODE = 5;

    public ItemNetworkComponents() {
        super("network_component");
        setHasSubtypes(true);
    }

    @Override
    public String getTranslationKey(ItemStack is) {
        return super.getTranslationKey(is) + is.getItemDamage();
    }

    @Override
    public int getMetadata(int meta) {
        return meta;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        if (isInCreativeTab(tab)) {
            for (int i = 0; i < COMPONENT_AMOUNT; i++) {
                subItems.add(new ItemStack(this, 1, i));
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> curInfo, ITooltipFlag extraInfo) {
        super.addInformation(stack, worldIn, curInfo, extraInfo);

        if (PneumaticCraftRepressurized.proxy.getClientWorld() != null) {
            if (Minecraft.getMinecraft().currentScreen instanceof GuiSecurityStationHacking) {
                ((GuiSecurityStationHacking) Minecraft.getMinecraft().currentScreen).addExtraHackInfo(curInfo);
            }
        }
    }

    @Override
    public boolean canProgram(ItemStack stack) {
        return stack.getItemDamage() == NETWORK_API || stack.getItemDamage() == NETWORK_DATA_STORAGE;
    }

    @Override
    public boolean usesPieces(ItemStack stack) {
        return stack.getItemDamage() == NETWORK_API;
    }

    @Override
    public boolean showProgramTooltip() {
        return true;
    }

    @Override
    public String getSubtypeModelName(int meta) {
        return "network_component" + meta;
    }
}
