package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.progwidgets.ISidedWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetPlace;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;

public class DroneAIPlace extends DroneAIBlockInteraction<ProgWidgetPlace> {

    /**
     * @param drone the drone
     * @param widget needs to implement IBlockOrdered and IDirectionalWidget.
     */
    public DroneAIPlace(IDroneBase drone, ProgWidgetPlace widget) {
        super(drone, widget);
    }

    @Override
    protected boolean respectClaims() {
        return true;
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        if (drone.world().getBlockState(pos).getMaterial().isReplaceable()) {
            boolean failedOnPlacement = false;
            for (int i = 0; i < drone.getInv().getSlots(); i++) {
                ItemStack droneStack = drone.getInv().getStackInSlot(i);
                if (droneStack.getItem() instanceof BlockItem && progWidget.isItemValidForFilters(droneStack)) {
                    Direction side = ProgWidgetPlace.getDirForSides(((ISidedWidget) progWidget).getSides());
                    BlockPos placerPos = pos.offset(side);
                    BlockRayTraceResult brtr = drone.world().rayTraceBlocks(new RayTraceContext(PneumaticCraftUtils.getBlockCentre(placerPos), PneumaticCraftUtils.getBlockCentre(pos), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, drone.getFakePlayer()));
                    BlockItemUseContext ctx = new BlockItemUseContext(new ItemUseContext(drone.getFakePlayer(), Hand.MAIN_HAND, brtr));
                    Block placingBlock = ((BlockItem) droneStack.getItem()).getBlock();
                    BlockState state = placingBlock.getStateForPlacement(ctx);
                    if (worldCache.checkNoEntityCollision(null, state.getShape(drone.world(), pos))) {
                        if (state.isValidPosition(drone.world(), pos)) {
                            return true;
                        } else {
                            drone.addDebugEntry("gui.progWidget.place.debug.cantPlaceBlock", pos);
                            failedOnPlacement = true;
                        }
                    } else {
                        drone.addDebugEntry("gui.progWidget.place.debug.entityInWay", pos);
                        failedOnPlacement = true;
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
            Direction side = ProgWidgetPlace.getDirForSides(((ISidedWidget) progWidget).getSides());
            for (int i = 0; i < drone.getInv().getSlots(); i++) {
                ItemStack droneStack = drone.getInv().getStackInSlot(i);
                if (droneStack.getItem() instanceof BlockItem && progWidget.isItemValidForFilters(droneStack)) {
                    BlockItem blockItem = (BlockItem) droneStack.getItem();
                    BlockPos placerPos = pos.offset(side);
                    BlockRayTraceResult brtr = drone.world().rayTraceBlocks(new RayTraceContext(PneumaticCraftUtils.getBlockCentre(placerPos), PneumaticCraftUtils.getBlockCentre(pos), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, drone.getFakePlayer()));
                    BlockItemUseContext ctx = new BlockItemUseContext(new ItemUseContext(drone.getFakePlayer(), Hand.MAIN_HAND, brtr));
                    Block placingBlock = blockItem.getBlock();
                    BlockState state = placingBlock.getStateForPlacement(ctx);
                    ActionResultType res = blockItem.tryPlace(ctx);
                    if (res == ActionResultType.SUCCESS) {
                        drone.getCapability(PNCCapabilities.AIR_HANDLER_CAPABILITY)
                                .ifPresent(h -> h.addAir(-PneumaticValues.DRONE_USAGE_PLACE));
                        SoundType soundType = placingBlock.getSoundType(state, drone.world(), pos, drone.getFakePlayer());
                        drone.world().playSound(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, soundType.getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
                        droneStack.shrink(1);
                        if (droneStack.getCount() <= 0) {
                            drone.getInv().setStackInSlot(i, ItemStack.EMPTY);
                        }
                        return false;
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }
}
