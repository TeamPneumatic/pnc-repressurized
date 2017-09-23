package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.semiblock.ItemSemiBlockBase;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockLogistics;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockRequester;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class ItemLogisticsFrame extends ItemSemiBlockBase {

    public ItemLogisticsFrame(String registryName) {
        super(registryName);
        setCreativeTab(PneumaticCraftRepressurized.tabPneumaticCraft);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
        ItemStack stack = player.getHeldItem(handIn);
        if (handIn != EnumHand.MAIN_HAND) return ActionResult.newResult(EnumActionResult.PASS, stack);
        if (!world.isRemote) {
            player.openGui(PneumaticCraftRepressurized.instance, ((SemiBlockLogistics) getSemiBlock(world, null, stack)).getGuiID().ordinal(), world, 0, 0, 0);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> curInfo, ITooltipFlag extraInfo) {
        super.addInformation(stack, worldIn, curInfo, extraInfo);
        addTooltip(stack, worldIn, curInfo, PneumaticCraftRepressurized.proxy.isSneakingInGui());
        ItemPneumatic.addTooltip(stack, worldIn, curInfo);
    }

    public static void addTooltip(ItemStack stack, World world, List<String> curInfo, boolean sneaking) {
        if (stack.getTagCompound() != null && stack.getItem() instanceof ItemSemiBlockBase) {
            if (stack.getTagCompound().getBoolean("invisible")) curInfo.add(I18n.format("gui.logisticFrame.invisible"));
            if (stack.getTagCompound().hasKey("filters") && stack.getTagCompound().getTagList("filters", 10).tagCount() > 0 || stack.getTagCompound().hasKey("fluidFilters") && stack.getTagCompound().getTagList("fluidFilters", 10).tagCount() > 0) {
                String key = SemiBlockManager.getKeyForSemiBlock(SemiBlockManager.getSemiBlockForItem((ItemSemiBlockBase) stack.getItem()));
                if (sneaking) {
                    curInfo.add(I18n.format(String.format("gui.%s.filters", key)));
                    SemiBlockRequester requester = new SemiBlockRequester();
                    requester.onPlaced(PneumaticCraftRepressurized.proxy.getPlayer(), stack);
                    ItemStack[] stacks = new ItemStack[requester.getFilters().getSlots()];
                    for (int i = 0; i < stacks.length; i++) {
                        stacks[i] = requester.getFilters().getStackInSlot(i);
                    }
                    PneumaticCraftUtils.sortCombineItemStacksAndToString(curInfo, stacks);
                    for (int i = 0; i < 9; i++) {
                        FluidStack fluid = requester.getTankFilter(i).getFluid();
                        if (fluid != null) {
                            curInfo.add("-" + fluid.amount / 1000 + "B " + fluid.getLocalizedName());
                        }
                    }
                } else {
                    curInfo.add(I18n.format(String.format("gui.%s.hasFilters", key)));
                }
            }
        }
    }

}
