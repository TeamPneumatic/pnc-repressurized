package pneumaticCraft.common.item;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import pneumaticCraft.common.fluid.Fluids;

public class ItemSeismicSensor extends ItemPneumatic{
    public ItemSeismicSensor(){
        super("seismicSensor");
        setMaxStackSize(1);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10){
        if(!world.isRemote) {
            int testingY = y;
            while(testingY > 0) {
                testingY--;
                if(world.getBlock(x, testingY, z) == FluidRegistry.getFluid(Fluids.oil.getName()).getBlock()) {
                    Set<ChunkPosition> oilPositions = new HashSet<ChunkPosition>();
                    Stack<ChunkPosition> pendingPositions = new Stack<ChunkPosition>();
                    pendingPositions.add(new ChunkPosition(x, testingY, z));
                    while(!pendingPositions.empty()) {
                        ChunkPosition checkingPos = pendingPositions.pop();
                        for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                            ChunkPosition newPos = new ChunkPosition(checkingPos.chunkPosX + d.offsetX, checkingPos.chunkPosY + d.offsetY, checkingPos.chunkPosZ + d.offsetZ);
                            if(world.getBlock(newPos.chunkPosX, newPos.chunkPosY, newPos.chunkPosZ) == Fluids.oil.getBlock() && world.getBlockMetadata(newPos.chunkPosX, newPos.chunkPosY, newPos.chunkPosZ) == 0 && oilPositions.add(newPos)) {
                                pendingPositions.add(newPos);
                            }
                        }
                    }
                    player.addChatComponentMessage(new ChatComponentTranslation("message.seismicSensor.foundOilDetails", EnumChatFormatting.GREEN.toString() + (y - testingY), EnumChatFormatting.GREEN.toString() + oilPositions.size() / 10 * 10));
                    return true;
                }
            }
            player.addChatComponentMessage(new ChatComponentTranslation("message.seismicSensor.noOilFound"));
        }
        return true; // we don't want to use the item.

    }
}
