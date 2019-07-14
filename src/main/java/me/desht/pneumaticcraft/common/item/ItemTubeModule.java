package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.block.tubes.ModuleRegistrator;
import me.desht.pneumaticcraft.common.block.tubes.TubeModule;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class ItemTubeModule extends ItemPneumatic {
    public final String moduleName;

    public ItemTubeModule(String moduleName) {
        super(moduleName);

        this.moduleName = moduleName;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack par1ItemStack, World par2EntityPlayer, List<ITextComponent> par3List, ITooltipFlag par4) {
        super.addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);
        TubeModule module = ModuleRegistrator.getModule(moduleName);
        if (module != null) {
            par3List.add(new StringTextComponent("In line: " + (module.isInline() ? "Yes" : "No")).applyTextStyle(TextFormatting.DARK_AQUA));
        }
    }
}
