package pneumaticCraft.common.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import pneumaticCraft.client.gui.GuiGPSTool;
import pneumaticCraft.common.NBTUtil;
import pneumaticCraft.common.remote.GlobalVariableManager;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemGPSTool extends ItemPneumatic{
    public ItemGPSTool(){
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister){
        itemIcon = par1IconRegister.registerIcon(Textures.ITEM_GPS_TOOL);
    }

    @Override
    public boolean onItemUse(ItemStack IStack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10){
        setGPSLocation(IStack, x, y, z);
        if(!world.isRemote) player.addChatComponentMessage(new ChatComponentTranslation(EnumChatFormatting.GREEN + "[GPS Tool] Set Coordinates to " + x + ", " + y + ", " + z + "."));
        return true; // we don't want to use the item.

    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player){
        if(world.isRemote) {
            ChunkPosition pos = getGPSLocation(stack);
            FMLCommonHandler.instance().showGuiScreen(new GuiGPSTool(pos != null ? pos : new ChunkPosition(0, 0, 0), getVariable(stack)));
        }
        return stack;
    }

    // the information displayed as tooltip info. (saved coordinates in this
    // case)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List infoList, boolean par4){
        super.addInformation(stack, player, infoList, par4);
        NBTTagCompound compound = stack.stackTagCompound;
        if(compound != null) {
            int x = compound.getInteger("x");
            int y = compound.getInteger("y");
            int z = compound.getInteger("z");
            if(x != 0 || y != 0 || z != 0) {
                infoList.add("\u00a72Set to " + x + ", " + y + ", " + z);
            }
            String varName = getVariable(stack);
            if(!varName.equals("")) {
                infoList.add(I18n.format("gui.tooltip.gpsTool.variable", varName));
            }
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean heldItem){
        String var = getVariable(stack);
        if(!var.equals("") && !world.isRemote) {
            ChunkPosition pos = GlobalVariableManager.getInstance().getPos(var);
            setGPSLocation(stack, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
        }
    }

    public static ChunkPosition getGPSLocation(ItemStack gpsTool){
        NBTTagCompound compound = gpsTool.stackTagCompound;
        if(compound != null) {
            String var = getVariable(gpsTool);
            if(!var.equals("") && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
                ChunkPosition pos = GlobalVariableManager.getInstance().getPos(var);
                setGPSLocation(gpsTool, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
            }
            int x = compound.getInteger("x");
            int y = compound.getInteger("y");
            int z = compound.getInteger("z");
            if(x != 0 || y != 0 || z != 0) {
                return new ChunkPosition(x, y, z);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static void setGPSLocation(ItemStack gpsTool, int x, int y, int z){
        NBTUtil.setInteger(gpsTool, "x", x);
        NBTUtil.setInteger(gpsTool, "y", y);
        NBTUtil.setInteger(gpsTool, "z", z);
        String var = getVariable(gpsTool);
        if(!var.equals("")) GlobalVariableManager.getInstance().set(var, x, y, z);
    }

    public static void setVariable(ItemStack gpsTool, String variable){
        NBTUtil.setString(gpsTool, "variable", variable);
    }

    public static String getVariable(ItemStack gpsTool){
        return gpsTool.hasTagCompound() ? gpsTool.getTagCompound().getString("variable") : "";
    }
}
