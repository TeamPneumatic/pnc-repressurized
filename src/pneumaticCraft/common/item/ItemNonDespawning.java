package pneumaticCraft.common.item;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemNonDespawning extends ItemPneumatic{
    public ItemNonDespawning(){
        super();
    }

    public ItemNonDespawning(String textureLocation){
        super(textureLocation);
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem){
        if(!entityItem.worldObj.isRemote) entityItem.age--;
        return false;
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List curInfo, boolean moreInfo){
        super.addInformation(stack, player, curInfo, moreInfo);
        curInfo.add(I18n.format("gui.tooltip.doesNotDespawn"));
    }
}
