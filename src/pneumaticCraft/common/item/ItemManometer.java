package pneumaticCraft.common.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import pneumaticCraft.api.item.IPressurizable;
import pneumaticCraft.api.tileentity.IManoMeasurable;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.common.thirdparty.ModInteractionUtils;
import pneumaticCraft.lib.PneumaticValues;

public class ItemManometer extends ItemPressurizable{

    public ItemManometer(String textureLocation){
        super(textureLocation, PneumaticValues.AIR_CANISTER_MAX_AIR, PneumaticValues.AIR_CANISTER_VOLUME);
    }

    /**
     * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
     * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
     */
    @Override
    public boolean onItemUse(ItemStack iStack, EntityPlayer player, World world, int x, int y, int z, int side, float par8, float par9, float par10){
        if(world.isRemote) return true;
        for(int i = 0; i < 10; i++)
            world.getBlock(x, y, z).updateTick(world, x, y, z, new Random());
        if(((IPressurizable)iStack.getItem()).getPressure(iStack) > 0F) {
            TileEntity te = world.getTileEntity(x, y, z);
            IPneumaticMachine machine = ModInteractionUtils.getInstance().getMachine(te);
            List<String> curInfo = new ArrayList<String>();
            if(te instanceof IManoMeasurable) {
                ((IManoMeasurable)te).printManometerMessage(player, curInfo);
            } else if(machine != null) {
                machine.getAirHandler().printManometerMessage(player, curInfo);
            }
            if(curInfo.size() > 0) {
                ((IPressurizable)iStack.getItem()).addAir(iStack, -30);
                for(String s : curInfo) {
                    player.addChatComponentMessage(new ChatComponentTranslation(s));
                }
                return true;
            }
        } else {
            player.addChatComponentMessage(new ChatComponentTranslation(EnumChatFormatting.RED + "The Manometer doesn't have any charge!"));
            return false;
        }
        return false;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack iStack, EntityPlayer player, EntityLivingBase entity){
        if(!player.worldObj.isRemote) {
            if(entity instanceof IManoMeasurable) {
                if(((IPressurizable)iStack.getItem()).getPressure(iStack) > 0F) {
                    List<String> curInfo = new ArrayList<String>();
                    ((IManoMeasurable)entity).printManometerMessage(player, curInfo);
                    if(curInfo.size() > 0) {
                        ((IPressurizable)iStack.getItem()).addAir(iStack, -30);
                        for(String s : curInfo) {
                            player.addChatComponentMessage(new ChatComponentTranslation(s));
                        }
                        return true;
                    }
                } else {
                    player.addChatComponentMessage(new ChatComponentTranslation(EnumChatFormatting.RED + "The Manometer doesn't have any charge!"));
                }
            }
        }
        return false;
    }
}
