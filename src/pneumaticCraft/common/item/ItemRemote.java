package pneumaticCraft.common.item;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import org.apache.commons.lang3.text.WordUtils;

import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketNotifyVariablesRemote;
import pneumaticCraft.common.remote.GlobalVariableManager;
import pneumaticCraft.common.tileentity.TileEntitySecurityStation;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemRemote extends ItemPneumatic{
    public ItemRemote(String texture){
        super(texture);
        setMaxStackSize(1);
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player){
        if(!world.isRemote) {
            openGui(player, stack);
        }
        return stack;
    }

    /**
     * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
     * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
     */
    @Override
    public boolean onItemUseFirst(ItemStack remote, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ){
        if(!world.isRemote && !player.isSneaking() && isAllowedToEdit(player, remote)) {
            TileEntity te = world.getTileEntity(x, y, z);
            if(te instanceof TileEntitySecurityStation) {
                if(((TileEntitySecurityStation)te).doesAllowPlayer(player)) {
                    NBTTagCompound tag = remote.getTagCompound();
                    if(tag == null) {
                        tag = new NBTTagCompound();
                        remote.setTagCompound(tag);
                    }
                    tag.setInteger("securityX", x);
                    tag.setInteger("securityY", y);
                    tag.setInteger("securityZ", z);
                    tag.setInteger("securityDimension", world.provider.dimensionId);
                    player.addChatComponentMessage(new ChatComponentTranslation("gui.remote.boundSecurityStation", x, y, z));
                    return true;
                } else {
                    player.addChatComponentMessage(new ChatComponentTranslation("gui.remote.cantBindSecurityStation"));
                }
            }
        }
        return false;
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack remote, EntityPlayer player, List curInfo, boolean moreInfo){
        super.addInformation(remote, player, curInfo, moreInfo);
        curInfo.add(I18n.format("gui.remote.tooltip.sneakRightClickToEdit"));
        NBTTagCompound tag = remote.getTagCompound();
        if(tag != null && tag.hasKey("securityX")) {
            int x = tag.getInteger("securityX");
            int y = tag.getInteger("securityY");
            int z = tag.getInteger("securityZ");
            int dimensionId = tag.getInteger("securityDimension");
            for(String s : WordUtils.wrap(I18n.format("gui.remote.tooltip.boundToSecurityStation", dimensionId, x, y, z), 40).split(System.getProperty("line.separator"))) {
                curInfo.add(s);
            }
        } else {
            for(String s : WordUtils.wrap(I18n.format("gui.remote.tooltip.rightClickToBind"), 40).split(System.getProperty("line.separator"))) {
                curInfo.add(s);
            }
        }
    }

    private void openGui(EntityPlayer player, ItemStack remote){
        if(player.isSneaking()) {
            if(isAllowedToEdit(player, remote)) {
                player.openGui(PneumaticCraft.instance, EnumGuiId.REMOTE_EDITOR.ordinal(), player.worldObj, (int)player.posX, (int)player.posY, (int)player.posZ);
                NetworkHandler.sendTo(new PacketNotifyVariablesRemote(GlobalVariableManager.getInstance().getAllActiveVariableNames()), (EntityPlayerMP)player);
            }
        } else {
            player.openGui(PneumaticCraft.instance, EnumGuiId.REMOTE.ordinal(), player.worldObj, (int)player.posX, (int)player.posY, (int)player.posZ);
        }
    }

    public static boolean hasSameSecuritySettings(ItemStack remote1, ItemStack remote2){
        NBTTagCompound tag1 = remote1.getTagCompound();
        NBTTagCompound tag2 = remote2.getTagCompound();
        if(tag1 == null && tag2 == null) return true;
        if(tag1 == null || tag2 == null) return false;
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

    private boolean isAllowedToEdit(EntityPlayer player, ItemStack remote){
        NBTTagCompound tag = remote.getTagCompound();
        if(tag != null) {
            if(tag.hasKey("securityX")) {
                int x = tag.getInteger("securityX");
                int y = tag.getInteger("securityY");
                int z = tag.getInteger("securityZ");
                int dimensionId = tag.getInteger("securityDimension");
                WorldServer world = null;
                for(WorldServer w : MinecraftServer.getServer().worldServers) {
                    if(w.provider.dimensionId == dimensionId) {
                        world = w;
                        break;
                    }
                }
                if(world != null) {
                    TileEntity te = world.getTileEntity(x, y, z);
                    if(te instanceof TileEntitySecurityStation) {
                        boolean canAccess = ((TileEntitySecurityStation)te).doesAllowPlayer(player);
                        if(!canAccess) {
                            player.addChatComponentMessage(new ChatComponentTranslation("gui.remote.noEditRights", x, y, z));
                        }
                        return canAccess;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void onUpdate(ItemStack remote, World worl, Entity entity, int slot, boolean holdingItem){
        if(!worl.isRemote) {
            NBTTagCompound tag = remote.getTagCompound();
            if(tag != null) {
                if(tag.hasKey("securityX")) {
                    int x = tag.getInteger("securityX");
                    int y = tag.getInteger("securityY");
                    int z = tag.getInteger("securityZ");
                    int dimensionId = tag.getInteger("securityDimension");
                    WorldServer world = null;
                    for(WorldServer w : MinecraftServer.getServer().worldServers) {
                        if(w.provider.dimensionId == dimensionId) {
                            world = w;
                            break;
                        }
                    }
                    if(world != null) {
                        TileEntity te = world.getTileEntity(x, y, z);
                        if(!(te instanceof TileEntitySecurityStation)) {
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
