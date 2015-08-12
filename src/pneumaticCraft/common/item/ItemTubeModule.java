package pneumaticCraft.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import pneumaticCraft.common.block.tubes.ModuleRegistrator;
import pneumaticCraft.common.block.tubes.TubeModule;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemTubeModule extends ItemPneumatic{
    public final String moduleName;

    public ItemTubeModule(String moduleName){
        this.moduleName = moduleName;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4){
        super.addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);
        TubeModule module = ModuleRegistrator.getModule(moduleName);
        module.addItemDescription(par3List);
        par3List.add(EnumChatFormatting.DARK_GRAY + "In line: " + (module.isInline() ? "Yes" : "No"));
    }

}
