package pneumaticCraft.common.item;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.semiblock.ItemSemiBlockBase;
import pneumaticCraft.common.semiblock.SemiBlockLogistics;
import pneumaticCraft.common.semiblock.SemiBlockManager;
import pneumaticCraft.common.semiblock.SemiBlockRequester;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class ItemLogisticsFrame extends ItemSemiBlockBase{

    public ItemLogisticsFrame(String semiBlockId){
        super(semiBlockId);
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player){
        if(!world.isRemote) {
            player.openGui(PneumaticCraft.instance, ((SemiBlockLogistics)getSemiBlock(world, 0, 0, 0, stack)).getGuiID().ordinal(), world, 0, 0, 0);
        }
        return stack;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List curInfo, boolean extraInfo){
        super.addInformation(stack, player, curInfo, extraInfo);
        addTooltip(stack, player, curInfo, PneumaticCraft.proxy.isSneakingInGui());
        ItemPneumatic.addTooltip(stack, player, curInfo);
    }

    public static void addTooltip(ItemStack stack, EntityPlayer player, List<String> curInfo, boolean sneaking){

        if(stack.getTagCompound() != null) {
            if(stack.getTagCompound().getBoolean("invisible")) curInfo.add(I18n.format("gui.logisticFrame.invisible"));
            if(stack.getTagCompound().hasKey("filters") && stack.getTagCompound().getTagList("filters", 10).tagCount() > 0 || stack.getTagCompound().hasKey("fluidFilters") && stack.getTagCompound().getTagList("fluidFilters", 10).tagCount() > 0) {
                String key = SemiBlockManager.getKeyForSemiBlock(SemiBlockManager.getSemiBlockForItem(stack.getItem()));
                if(sneaking) {
                    curInfo.add(StatCollector.translateToLocal(String.format("gui.%s.filters", key)));
                    SemiBlockRequester requester = new SemiBlockRequester();
                    requester.onPlaced(player, stack);
                    ItemStack[] stacks = new ItemStack[requester.getFilters().getSizeInventory()];
                    for(int i = 0; i < stacks.length; i++) {
                        stacks[i] = requester.getFilters().getStackInSlot(i);
                    }
                    PneumaticCraftUtils.sortCombineItemStacksAndToString(curInfo, stacks);
                    for(int i = 0; i < 9; i++) {
                        FluidStack fluid = requester.getTankFilter(i).getFluid();
                        if(fluid != null) {
                            curInfo.add("-" + fluid.amount / 1000 + "B " + fluid.getLocalizedName());
                        }
                    }
                } else {
                    curInfo.add(StatCollector.translateToLocal(String.format("gui.%s.hasFilters", key)));
                }
            }
        }
    }

}
