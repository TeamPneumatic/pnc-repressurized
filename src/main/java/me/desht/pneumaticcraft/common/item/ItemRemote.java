package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketNotifyVariablesRemote;
import me.desht.pneumaticcraft.common.remote.GlobalVariableManager;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySecurityStation;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.text.WordUtils;

import java.util.Collections;
import java.util.List;

public class ItemRemote extends ItemPneumatic {
    public ItemRemote() {
        super("remote");
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
        ItemStack stack = player.getHeldItem(handIn);
        if (handIn != EnumHand.MAIN_HAND) return ActionResult.newResult(EnumActionResult.PASS, stack);
        if (!world.isRemote) {
            openGui(player, stack);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        ItemStack remote = player.getHeldItem(hand);
        if (hand != EnumHand.MAIN_HAND) return EnumActionResult.PASS;
        if (!world.isRemote && !player.isSneaking() && isAllowedToEdit(player, remote)) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntitySecurityStation) {
                if (((TileEntitySecurityStation) te).doesAllowPlayer(player)) {
                    NBTTagCompound tag = remote.getTagCompound();
                    if (tag == null) {
                        tag = new NBTTagCompound();
                        remote.setTagCompound(tag);
                    }
                    tag.setInteger("securityX", pos.getX());
                    tag.setInteger("securityY", pos.getY());
                    tag.setInteger("securityZ", pos.getZ());
                    tag.setInteger("securityDimension", world.provider.getDimension());
                    player.sendStatusMessage(new TextComponentTranslation("gui.remote.boundSecurityStation", pos.getX(), pos.getY(), pos.getZ()), false);
                    return EnumActionResult.SUCCESS;
                } else {
                    player.sendStatusMessage(new TextComponentTranslation("gui.remote.cantBindSecurityStation"), false);
                }
            }
        }
        return EnumActionResult.PASS;
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack remote, World world, List<String> curInfo, ITooltipFlag moreInfo) {
        super.addInformation(remote, world, curInfo, moreInfo);
        curInfo.add(I18n.format("gui.remote.tooltip.sneakRightClickToEdit"));
        NBTTagCompound tag = remote.getTagCompound();
        if (tag != null && tag.hasKey("securityX")) {
            int x = tag.getInteger("securityX");
            int y = tag.getInteger("securityY");
            int z = tag.getInteger("securityZ");
            int dimensionId = tag.getInteger("securityDimension");
            Collections.addAll(curInfo, WordUtils.wrap(I18n.format("gui.remote.tooltip.boundToSecurityStation", dimensionId, x, y, z), 40).split(System.getProperty("line.separator")));
        } else {
            Collections.addAll(curInfo, WordUtils.wrap(I18n.format("gui.remote.tooltip.rightClickToBind"), 40).split(System.getProperty("line.separator")));
        }
    }

    private void openGui(EntityPlayer player, ItemStack remote) {
        if (player.isSneaking()) {
            if (isAllowedToEdit(player, remote)) {
                player.openGui(PneumaticCraftRepressurized.instance, EnumGuiId.REMOTE_EDITOR.ordinal(), player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
                NetworkHandler.sendTo(new PacketNotifyVariablesRemote(GlobalVariableManager.getInstance().getAllActiveVariableNames()), (EntityPlayerMP) player);
            }
        } else {
            player.openGui(PneumaticCraftRepressurized.instance, EnumGuiId.REMOTE.ordinal(), player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
        }
    }

    public static boolean hasSameSecuritySettings(ItemStack remote1, ItemStack remote2) {
        NBTTagCompound tag1 = remote1.getTagCompound();
        NBTTagCompound tag2 = remote2.getTagCompound();
        if (tag1 == null && tag2 == null) return true;
        if (tag1 == null || tag2 == null) return false;
        int x1 = tag1.getInteger("securityX");
        int y1 = tag1.getInteger("securityY");
        int z1 = tag1.getInteger("securityZ");
        int dimension1 = tag1.getInteger("securityDimension");
        int x2 = tag2.getInteger("securityX");
        int y2 = tag2.getInteger("securityY");
        int z2 = tag2.getInteger("securityZ");
        int dimension2 = tag2.getInteger("securityDimension");
        return x1 == x2 && y1 == y2 && z1 == z2 && dimension1 == dimension2;
    }

    private boolean isAllowedToEdit(EntityPlayer player, ItemStack remote) {
        NBTTagCompound tag = remote.getTagCompound();
        if (tag != null) {
            if (tag.hasKey("securityX")) {
                int x = tag.getInteger("securityX");
                int y = tag.getInteger("securityY");
                int z = tag.getInteger("securityZ");
                int dimensionId = tag.getInteger("securityDimension");
                WorldServer world = DimensionManager.getWorld(dimensionId);
                if (world != null) {
                    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                    if (te instanceof TileEntitySecurityStation) {
                        boolean canAccess = ((TileEntitySecurityStation) te).doesAllowPlayer(player);
                        if (!canAccess) {
                            player.sendStatusMessage(new TextComponentTranslation("gui.remote.noEditRights", x, y, z), false);
                        }
                        return canAccess;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void onUpdate(ItemStack remote, World worl, Entity entity, int slot, boolean holdingItem) {
        if (!worl.isRemote) {
            NBTTagCompound tag = remote.getTagCompound();
            if (tag != null) {
                if (tag.hasKey("securityX")) {
                    int x = tag.getInteger("securityX");
                    int y = tag.getInteger("securityY");
                    int z = tag.getInteger("securityZ");
                    int dimensionId = tag.getInteger("securityDimension");
                    WorldServer world = DimensionManager.getWorld(dimensionId);
                    if (world != null) {
                        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                        if (!(te instanceof TileEntitySecurityStation)) {
                            tag.removeTag("securityX");
                            tag.removeTag("securityY");
                            tag.removeTag("securityZ");
                            tag.removeTag("securityDimension");
                        }
                    }
                }
            }
        }
    }
}
