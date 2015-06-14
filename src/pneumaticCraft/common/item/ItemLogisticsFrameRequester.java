package pneumaticCraft.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.semiblock.ItemSemiBlockBase;
import pneumaticCraft.common.semiblock.SemiBlockRequester;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.proxy.CommonProxy;

public class ItemLogisticsFrameRequester extends ItemSemiBlockBase{

    public ItemLogisticsFrameRequester(){
        super(SemiBlockRequester.ID);
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player){
        if(!world.isRemote) {
            player.openGui(PneumaticCraft.instance, CommonProxy.GUI_ID_LOGISTICS_REQUESTER, world, 0, 0, 0);
        }
        return stack;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List curInfo, boolean extraInfo){
        super.addInformation(stack, player, curInfo, extraInfo);
        if(stack.getTagCompound() != null && stack.getTagCompound().hasKey("requests")) {
            if(PneumaticCraft.proxy.isSneakingInGui()) {
                curInfo.add(StatCollector.translateToLocal("gui.logisticsRequester.requests"));
                SemiBlockRequester requester = new SemiBlockRequester();
                requester.onPlaced(player, stack);
                ItemStack[] stacks = new ItemStack[requester.getRequests().getSizeInventory()];
                for(int i = 0; i < stacks.length; i++) {
                    stacks[i] = requester.getRequests().getStackInSlot(i);
                }
                PneumaticCraftUtils.sortCombineItemStacksAndToString(curInfo, stacks);
            } else {
                curInfo.add(StatCollector.translateToLocal("gui.logisticsRequester.hasRequests"));
            }
        }
    }

}
