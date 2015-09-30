package pneumaticCraft.common.ai;

import java.lang.reflect.Field;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.world.BlockEvent;
import pneumaticCraft.lib.Log;
import pneumaticCraft.lib.PneumaticValues;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class FakePlayerItemInWorldManager extends ItemInWorldManager{

    private static Field isDigging, acknowledged;
    public boolean isAccepted;
    private final IDroneBase drone;

    public FakePlayerItemInWorldManager(World par1World, EntityPlayerMP player, IDroneBase drone){
        super(par1World);
        thisPlayerMP = player;
        this.drone = drone;
    }

    @Override
    public void onBlockClicked(int par1, int par2, int par3, int par4){
        super.onBlockClicked(par1, par2, par3, par4);
        isAccepted = isDigging();
        uncheckedTryHarvestBlock(par1, par2, par3);
    }

    public boolean isDigging(){
        if(isDigging == null) isDigging = ReflectionHelper.findField(ItemInWorldManager.class, "field_73088_d", "isDestroyingBlock");
        try {
            return isDigging.getBoolean(this);
        } catch(Exception e) {
            Log.error("Drone FakePlayerItemInWorldManager failed with reflection (Digging)!");
            e.printStackTrace();
            return true;
        }
    }

    public boolean isAcknowledged(){
        if(acknowledged == null) acknowledged = ReflectionHelper.findField(ItemInWorldManager.class, "field_73097_j", "receivedFinishDiggingPacket");
        try {
            return acknowledged.getBoolean(this);
        } catch(Exception e) {
            Log.error("Drone FakePlayerItemInWorldManager failed with reflection (Acknowledge get)!");
            e.printStackTrace();
            return true;
        }
    }

    public void cancelDigging(){
        cancelDestroyingBlock(-1, -1, -1);
    }

    /**
     * Attempts to harvest a block at the given coordinate
     */
    @Override
    public boolean tryHarvestBlock(int x, int y, int z){
        BlockEvent.BreakEvent event = ForgeHooks.onBlockBreakEvent(theWorld, getGameType(), thisPlayerMP, x, y, z);
        if(event.isCanceled()) {
            return false;
        } else {
            ItemStack stack = thisPlayerMP.getCurrentEquippedItem();
            if(stack != null && stack.getItem().onBlockStartBreak(stack, x, y, z, thisPlayerMP)) {
                return false;
            }
            Block block = theWorld.getBlock(x, y, z);
            int l = theWorld.getBlockMetadata(x, y, z);
            theWorld.playAuxSFXAtEntity(thisPlayerMP, 2001, x, y, z, Block.getIdFromBlock(block) + (theWorld.getBlockMetadata(x, y, z) << 12));
            boolean flag = false;

            ItemStack itemstack = thisPlayerMP.getCurrentEquippedItem();

            if(itemstack != null) {
                itemstack.func_150999_a(theWorld, block, x, y, z, thisPlayerMP);

                if(itemstack.stackSize == 0) {
                    thisPlayerMP.destroyCurrentEquippedItem();
                }
            }

            if(removeBlock(x, y, z)) {
                block.harvestBlock(theWorld, thisPlayerMP, x, y, z, l);
                flag = true;
            }

            // Drop experience
            if(!isCreative() && flag && event != null) {
                block.dropXpOnBlockBreak(theWorld, x, y, z, event.getExpToDrop());
            }
            drone.addAir(null, -PneumaticValues.DRONE_USAGE_DIG);
            return true;
        }
    }

    /**
     * Removes a block and triggers the appropriate events
     */
    private boolean removeBlock(int par1, int par2, int par3){
        Block block = theWorld.getBlock(par1, par2, par3);
        int l = theWorld.getBlockMetadata(par1, par2, par3);
        block.onBlockHarvested(theWorld, par1, par2, par3, l, thisPlayerMP);
        boolean flag = block != null && block.removedByPlayer(theWorld, thisPlayerMP, par1, par2, par3);

        if(flag) {
            block.onBlockDestroyedByPlayer(theWorld, par1, par2, par3, l);
        }

        return flag;
    }

}
