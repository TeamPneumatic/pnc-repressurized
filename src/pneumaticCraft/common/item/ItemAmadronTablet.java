package pneumaticCraft.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.lib.Log;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class ItemAmadronTablet extends ItemPressurizable implements IAmadronInterface{

    public ItemAmadronTablet(String textureLocation, int maxAir, int volume){
        super(textureLocation, maxAir, volume);
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player){
        if(!world.isRemote) {
            Log.info("Opening");
            player.openGui(PneumaticCraft.instance, EnumGuiId.AMADRON.ordinal(), player.worldObj, (int)player.posX, (int)player.posY, (int)player.posZ);
        }
        return stack;
    }

}
