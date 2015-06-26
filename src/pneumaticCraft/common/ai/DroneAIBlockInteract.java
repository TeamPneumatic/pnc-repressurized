package pneumaticCraft.common.ai;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemRedstone;
import net.minecraft.item.ItemReed;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import pneumaticCraft.common.progwidgets.IBlockRightClicker;
import pneumaticCraft.common.progwidgets.ISidedWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;
import pneumaticCraft.common.progwidgets.ProgWidgetPlace;
import pneumaticCraft.common.util.PneumaticCraftUtils;
import pneumaticCraft.lib.Log;

public class DroneAIBlockInteract extends DroneAIBlockInteraction{

    private final List<ChunkPosition> visitedPositions = new ArrayList<ChunkPosition>();

    public DroneAIBlockInteract(IDroneBase drone, ProgWidgetAreaItemBase widget){
        super(drone, widget);
        drone.getFakePlayer().setSneaking(((IBlockRightClicker)widget).isSneaking());
    }

    @Override
    protected boolean isValidPosition(ChunkPosition pos){
        return !visitedPositions.contains(pos) && (widget.isItemFilterEmpty() || DroneAIDig.isBlockValidForFilter(drone.getWorld(), drone, pos, widget));
    }

    @Override
    protected boolean doBlockInteraction(ChunkPosition pos, double distToBlock){
        visitedPositions.add(pos);
        boolean result = rightClick(pos);
        if(drone.getFakePlayer().getCurrentEquippedItem() != null && drone.getFakePlayer().getCurrentEquippedItem().stackSize <= 0) {
            drone.getFakePlayer().setCurrentItemOrArmor(0, null);
        }
        transferToDroneFromFakePlayer(drone);
        return result;
    }

    public static void transferToDroneFromFakePlayer(IDroneBase drone){
        //transfer items
        for(int j = 1; j < drone.getFakePlayer().inventory.mainInventory.length; j++) {
            ItemStack excessStack = drone.getFakePlayer().inventory.mainInventory[j];
            if(excessStack != null) {
                ItemStack remainder = PneumaticCraftUtils.exportStackToInventory(drone.getInventory(), excessStack, ForgeDirection.UNKNOWN);
                if(remainder != null) {
                    drone.dropItem(remainder);
                }
                drone.getFakePlayer().inventory.mainInventory[j] = null;
            }
        }

    }

    private boolean rightClick(ChunkPosition pos){
        int xCoord = pos.chunkPosX;
        int yCoord = pos.chunkPosY;
        int zCoord = pos.chunkPosZ;

        ForgeDirection faceDir = ProgWidgetPlace.getDirForSides(((ISidedWidget)widget).getSides());
        EntityPlayer player = drone.getFakePlayer();
        World worldObj = drone.getWorld();
        int dx = faceDir.offsetX;
        int dy = faceDir.offsetY;
        int dz = faceDir.offsetZ;
        int x = xCoord /*+ dx*/;
        int y = yCoord /*+ dy*/;
        int z = zCoord /*+ dz*/;

        player.setPosition(x + 0.5, y + 0.5 - player.eyeHeight, z + 0.5);
        player.rotationPitch = faceDir.offsetY * -90;
        switch(faceDir){
            case NORTH:
                player.rotationYaw = 180;
                break;
            case SOUTH:
                player.rotationYaw = 0;
                break;
            case WEST:
                player.rotationYaw = 90;
                break;
            case EAST:
                player.rotationYaw = -90;
        }

        try {
            PlayerInteractEvent event = ForgeEventFactory.onPlayerInteract(player, Action.RIGHT_CLICK_AIR, x, y, z, faceDir.ordinal(), worldObj);
            if(event.isCanceled()) return false;

            Block block = worldObj.getBlock(x, y, z);

            ItemStack stack = player.getCurrentEquippedItem();
            if(stack != null && stack.getItem().onItemUseFirst(stack, player, worldObj, x, y, z, faceDir.ordinal(), dx, dy, dz)) return false;

            if(!worldObj.isAirBlock(x, y, z) && block.onBlockActivated(worldObj, x, y, z, player, faceDir.ordinal(), dx, dy, dz)) return false;

            if(stack != null) {
                boolean isGoingToShift = false;
                if(stack.getItem() instanceof ItemReed || stack.getItem() instanceof ItemRedstone) {
                    isGoingToShift = true;
                }
                int useX = isGoingToShift ? xCoord : x;
                int useY = isGoingToShift ? yCoord : y;
                int useZ = isGoingToShift ? zCoord : z;
                if(stack.getItem().onItemUse(stack, player, worldObj, useX, useY, useZ, faceDir.ordinal(), dx, dy, dz)) return false;

                ItemStack copy = stack.copy();
                player.setCurrentItemOrArmor(0, stack.getItem().onItemRightClick(stack, worldObj, player));
                if(!copy.isItemEqual(stack)) return true;
            }
            return false;
        } catch(Throwable e) {
            Log.error("DroneAIBlockInteract crashed! Stacktrace: ");
            e.printStackTrace();
            return false;
        }
    }

}
