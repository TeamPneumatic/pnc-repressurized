package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.progwidgets.ISidedWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetPlace;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class DroneAIPlace extends DroneAIBlockInteraction {

    /**
     * @param drone
     * @param widget needs to implement IBlockOrdered and IDirectionalWidget.
     */
    public DroneAIPlace(IDroneBase drone, ProgWidgetAreaItemBase widget) {
        super(drone, widget);
    }

    @Override
    protected boolean respectClaims() {
        return true;
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        if (drone.world().isAirBlock(pos)) {
            boolean failedOnPlacement = false;
            for (int i = 0; i < drone.getInv().getSlots(); i++) {
                ItemStack droneStack = drone.getInv().getStackInSlot(i);
                if (droneStack.getItem() instanceof ItemBlock) {
                    if (widget.isItemValidForFilters(droneStack)) {
                        Block placingBlock = ((ItemBlock) droneStack.getItem()).getBlock();
                        EnumFacing side = ProgWidgetPlace.getDirForSides(((ISidedWidget) widget).getSides());
                        if (drone.world().mayPlace(placingBlock, pos, false, side, drone instanceof EntityDrone ? (EntityDrone) drone : null)) {
                            return true;
                        } else {
                            if (drone.world().mayPlace(placingBlock, pos, true, side, drone instanceof EntityDrone ? (EntityDrone) drone : null)) {
                                drone.addDebugEntry("gui.progWidget.place.debug.cantPlaceBlock", pos);
                            } else {
                                drone.addDebugEntry("gui.progWidget.place.debug.entityInWay", pos);
                            }
                            failedOnPlacement = true;
                        }
                    }
                }
            }
            if (!failedOnPlacement) abort();
        }
        return false;
    }

    //TODO 1.8 test
    @Override
    protected boolean doBlockInteraction(BlockPos pos, double distToBlock) {
        if (drone.getPathNavigator().hasNoPath()) {
            EnumFacing side = ProgWidgetPlace.getDirForSides(((ISidedWidget) widget).getSides());
            for (int i = 0; i < drone.getInv().getSlots(); i++) {
                ItemStack droneStack = drone.getInv().getStackInSlot(i);
                if (droneStack.getItem() instanceof ItemBlock && ((ItemBlock) droneStack.getItem()).getBlock().canPlaceBlockOnSide(drone.world(), pos, ProgWidgetPlace.getDirForSides(((ISidedWidget) widget).getSides()))) {
                    if (widget.isItemValidForFilters(droneStack)) {
                        ItemBlock itemBlock = (ItemBlock) droneStack.getItem();
                        Block block = itemBlock.getBlock();
                        if (drone.world().mayPlace(block, pos, false, side, drone instanceof EntityDrone ? (EntityDrone) drone : null)) {
                            int newMeta = itemBlock.getMetadata(droneStack.getMetadata());
                            setFakePlayerAccordingToDir();
                            IBlockState iblockstate1 = block.getStateForPlacement(drone.world(), pos, side, side.getFrontOffsetX(), side.getFrontOffsetY(), side.getFrontOffsetZ(), newMeta, drone.getFakePlayer(), EnumHand.MAIN_HAND);
                            if (itemBlock.placeBlockAt(droneStack, drone.getFakePlayer(), drone.world(), pos, side, side.getFrontOffsetX(), side.getFrontOffsetY(), side.getFrontOffsetZ(), iblockstate1)) {
                                drone.addAir(null, -PneumaticValues.DRONE_USAGE_PLACE);
                                SoundType soundType = block.getSoundType(iblockstate1, drone.world(), pos, drone.getFakePlayer());
                                drone.world().playSound(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, soundType.getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
                                droneStack.shrink(1);
                                if (droneStack.getCount() <= 0) {
                                    drone.getInv().setStackInSlot(i, ItemStack.EMPTY);
                                }
                            }
                            return false;
                        }
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }

    private void setFakePlayerAccordingToDir() {
        EntityPlayer fakePlayer = drone.getFakePlayer();
        Vec3d pos = drone.getDronePos();
        fakePlayer.posX = pos.x;
        fakePlayer.posZ = pos.z;
        switch (ProgWidgetPlace.getDirForSides(((ISidedWidget) widget).getSides())) {
            case UP:
                fakePlayer.rotationPitch = -90;
                fakePlayer.posY = pos.y - 10;//do this for PistonBase.determineDirection()
                return;
            case DOWN:
                fakePlayer.rotationPitch = 90;
                fakePlayer.posY = pos.y + 10;//do this for PistonBase.determineDirection()
                return;
            case NORTH:
                fakePlayer.rotationYaw = 180;
                fakePlayer.posY = pos.y;//do this for PistonBase.determineDirection()
                break;
            case EAST:
                fakePlayer.rotationYaw = 270;
                fakePlayer.posY = pos.y;//do this for PistonBase.determineDirection()
                break;
            case SOUTH:
                fakePlayer.rotationYaw = 0;
                fakePlayer.posY = pos.y;//do this for PistonBase.determineDirection()
                break;
            case WEST:
                fakePlayer.rotationYaw = 90;
                fakePlayer.posY = pos.y;//do this for PistonBase.determineDirection()
                break;
        }
    }

}
