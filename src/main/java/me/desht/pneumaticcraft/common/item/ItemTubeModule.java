package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.block.tubes.ModuleRegistrator;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemTubeModule extends ItemPneumatic {
    public final String moduleName;

    public ItemTubeModule(String moduleName) {
        super(moduleName);
        this.moduleName = moduleName;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack par1ItemStack, World par2EntityPlayer, List<String> par3List, ITooltipFlag par4) {
        super.addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);
        TubeModule module = ModuleRegistrator.getModule(moduleName);
        module.addItemDescription(par3List);
        par3List.add(TextFormatting.DARK_GRAY + "In line: " + (module.isInline() ? "Yes" : "No"));
    }

}
