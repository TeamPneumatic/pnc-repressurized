package pneumaticCraft.common.item;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.worldgen.OilTracker;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemSeismicSensor extends ItemPneumatic{
    public ItemSeismicSensor(){
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister){
        itemIcon = par1IconRegister.registerIcon(Textures.ITEM_GPS_TOOL);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10){

        if(!world.isRemote) {
            boolean containsOil = OilTracker.containsReserves(world, x, z);
            if(containsOil) {
                int testingY = y;
                player.addChatComponentMessage(new ChatComponentTranslation(EnumChatFormatting.GREEN + "[Seismic Sensor] Potentientally oil found! Scanning..." + containsOil));
                while(testingY > 0) {
                    testingY--;
                    if(world.getBlock(x, testingY, z) == Blockss.oil) {
                        Set<ChunkPosition> oilPositions = new HashSet<ChunkPosition>();
                        Stack<ChunkPosition> pendingPositions = new Stack<ChunkPosition>();
                        pendingPositions.add(new ChunkPosition(x, testingY, z));
                        while(!pendingPositions.empty()) {
                            ChunkPosition checkingPos = pendingPositions.pop();
                            for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                                ChunkPosition newPos = new ChunkPosition(checkingPos.chunkPosX + d.offsetX, checkingPos.chunkPosY + d.offsetY, checkingPos.chunkPosZ + d.offsetZ);
                                if(world.getBlock(newPos.chunkPosX, newPos.chunkPosY, newPos.chunkPosZ) == Blockss.oil && world.getBlockMetadata(newPos.chunkPosX, newPos.chunkPosY, newPos.chunkPosZ) == 0 && oilPositions.add(newPos)) {
                                    pendingPositions.add(newPos);
                                }
                            }
                        }
                        player.addChatComponentMessage(new ChatComponentTranslation(EnumChatFormatting.GREEN + "[Seismic Sensor] Found oil %sm below. It contains about %s buckets of oil.", y - testingY, oilPositions.size() / 10 * 10));
                        return true;
                    }
                }
                player.addChatComponentMessage(new ChatComponentTranslation(EnumChatFormatting.GREEN + "[Seismic Sensor] No oil found"));

            } else {
                player.addChatComponentMessage(new ChatComponentTranslation(EnumChatFormatting.GREEN + "[Seismic Sensor] No oil found"));

            }
        }
        return true; // we don't want to use the item.

    }

    // the information displayed as tooltip info. (saved coordinates in this
    // case)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List infoList, boolean par4){

    }

}
