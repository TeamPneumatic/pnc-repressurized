package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.NBTUtil;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncAmadronOffers;
import me.desht.pneumaticcraft.common.recipes.AmadronOffer;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferCustom;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemAmadronTablet extends ItemPressurizable implements IAmadronInterface {

    public ItemAmadronTablet() {
        super("amadron_tablet", PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (handIn != EnumHand.MAIN_HAND) return ActionResult.newResult(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
        if (!worldIn.isRemote) {
            NetworkHandler.sendTo(new PacketSyncAmadronOffers(AmadronOfferManager.getInstance().getAllOffers()), (EntityPlayerMP) playerIn);
            playerIn.openGui(PneumaticCraftRepressurized.instance, EnumGuiId.AMADRON.ordinal(), playerIn.world, (int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, playerIn.getHeldItemMainhand());
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND) return EnumActionResult.PASS;
        TileEntity te = worldIn.getTileEntity(pos);
        if (te == null) return EnumActionResult.PASS;
        if (te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, facing)) {
            if (!worldIn.isRemote) {
                setLiquidProvidingLocation(player.getHeldItemMainhand(), pos, worldIn.provider.getDimension());
                player.sendStatusMessage(new TextComponentTranslation("message.amadronTable.setLiquidProvidingLocation", pos.getX(), pos.getY(), pos.getZ(),
                        worldIn.provider.getDimension(), worldIn.provider.getDimensionType().toString()), false);
            }
        } else if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
            if (!worldIn.isRemote) {
                setItemProvidingLocation(player.getHeldItemMainhand(), pos, worldIn.provider.getDimension());
                player.sendStatusMessage(new TextComponentTranslation("message.amadronTable.setItemProvidingLocation", pos.getX(), pos.getY(), pos.getZ(),
                        worldIn.provider.getDimension(), worldIn.provider.getDimensionType().toString()), false);
            }
        } else {
            return EnumActionResult.PASS;
        }
        return EnumActionResult.SUCCESS;

    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> infoList, ITooltipFlag flag) {
        super.addInformation(stack, worldIn, infoList, flag);
        BlockPos pos = getItemProvidingLocation(stack);
        if (pos != null) {
            int dim = getItemProvidingDimension(stack);
            infoList.add(I18n.format("gui.tooltip.amadronTablet.itemLocation", pos.getX(), pos.getY(), pos.getZ(), dim));
        } else {
            infoList.add(I18n.format("gui.tooltip.amadronTablet.selectItemLocation"));
        }

        pos = getLiquidProvidingLocation(stack);
        if (pos != null) {
            int dim = getLiquidProvidingDimension(stack);
            infoList.add(I18n.format("gui.tooltip.amadronTablet.fluidLocation", pos.getX(), pos.getY(), pos.getZ(), dim));
        } else {
            infoList.add(I18n.format("gui.tooltip.amadronTablet.selectFluidLocation"));
        }
    }

    public static IItemHandler getItemProvider(ItemStack tablet) {
        BlockPos pos = getItemProvidingLocation(tablet);
        if (pos != null) {
            int dimension = getItemProvidingDimension(tablet);
            TileEntity te = PneumaticCraftUtils.getTileEntity(pos, dimension);
            return IOHelper.getInventoryForTE(te);
        }
        return null;
    }

    public static BlockPos getItemProvidingLocation(ItemStack tablet) {
        NBTTagCompound compound = tablet.getTagCompound();
        if (compound != null) {
            int x = compound.getInteger("itemX");
            int y = compound.getInteger("itemY");
            int z = compound.getInteger("itemZ");
            if (x != 0 || y != 0 || z != 0) {
                return new BlockPos(x, y, z);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static int getItemProvidingDimension(ItemStack tablet) {
        return tablet.hasTagCompound() ? tablet.getTagCompound().getInteger("itemDim") : 0;
    }

    public static void setItemProvidingLocation(ItemStack tablet, BlockPos pos, int dimensionId) {
        NBTUtil.setInteger(tablet, "itemX", pos.getX());
        NBTUtil.setInteger(tablet, "itemY", pos.getY());
        NBTUtil.setInteger(tablet, "itemZ", pos.getZ());
        NBTUtil.setInteger(tablet, "itemDim", dimensionId);
    }

    public static IFluidHandler getLiquidProvider(ItemStack tablet) {
        BlockPos pos = getLiquidProvidingLocation(tablet);
        if (pos != null) {
            int dimension = getLiquidProvidingDimension(tablet);
            return FluidUtil.getFluidHandler(DimensionManager.getWorld(dimension), pos, null);
        }
        return null;
    }

    public static BlockPos getLiquidProvidingLocation(ItemStack tablet) {
        NBTTagCompound compound = tablet.getTagCompound();
        if (compound != null) {
            int x = compound.getInteger("liquidX");
            int y = compound.getInteger("liquidY");
            int z = compound.getInteger("liquidZ");
            if (x != 0 || y != 0 || z != 0) {
                return new BlockPos(x, y, z);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static int getLiquidProvidingDimension(ItemStack tablet) {
        return tablet.hasTagCompound() ? tablet.getTagCompound().getInteger("liquidDim") : 0;
    }

    public static void setLiquidProvidingLocation(ItemStack tablet, BlockPos pos, int dimensionId) {
        NBTUtil.setInteger(tablet, "liquidX", pos.getX());
        NBTUtil.setInteger(tablet, "liquidY", pos.getY());
        NBTUtil.setInteger(tablet, "liquidZ", pos.getZ());
        NBTUtil.setInteger(tablet, "liquidDim", dimensionId);
    }

    public static Map<AmadronOffer, Integer> getShoppingCart(ItemStack tablet) {
        Map<AmadronOffer, Integer> offers = new HashMap<AmadronOffer, Integer>();

        if (tablet.hasTagCompound() && tablet.getTagCompound().hasKey("shoppingCart")) {
            NBTTagList list = tablet.getTagCompound().getTagList("shoppingCart", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound tag = list.getCompoundTagAt(i);
                offers.put(tag.hasKey("inStock") ? AmadronOfferCustom.loadFromNBT(tag) : AmadronOffer.loadFromNBT(tag), tag.getInteger("amount"));
            }
        }
        return offers;
    }

    public static void setShoppingCart(ItemStack tablet, Map<AmadronOffer, Integer> cart) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<AmadronOffer, Integer> entry : cart.entrySet()) {
            NBTTagCompound tag = new NBTTagCompound();
            entry.getKey().writeToNBT(tag);
            tag.setInteger("amount", entry.getValue());
            list.appendTag(tag);
        }
        NBTUtil.setCompoundTag(tablet, "shoppingCart", list);
    }
}
