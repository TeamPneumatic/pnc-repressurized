package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.api.drone.ICustomBlockInteract;
import me.desht.pneumaticcraft.api.drone.IDroneRegistry;
import me.desht.pneumaticcraft.api.drone.IPathfindHandler;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetCustomBlockInteract;
import me.desht.pneumaticcraft.common.util.ProgrammedDroneUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.GlobalPos;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.Validate;

import java.util.HashMap;
import java.util.Map;

public enum DroneRegistry implements IDroneRegistry {
    INSTANCE;

    public final Map<Block, IPathfindHandler> pathfindableBlocks = new HashMap<>();

    public static DroneRegistry getInstance() {
        return INSTANCE;
    }

    @Override
    public void addPathfindableBlock(Block block, IPathfindHandler handler) {
        Validate.notNull(block);
        pathfindableBlocks.put(block, handler);
    }

    @Override
    public void registerCustomBlockInteractor(RegistryEvent.Register<ProgWidgetType<?>> event, ICustomBlockInteract interactor) {
        ProgWidgetType type = new ProgWidgetType<>(() ->
                new ProgWidgetCustomBlockInteract().setInteractor(interactor)).setRegistryName(interactor.getID());
        event.getRegistry().register(type);
//        ModProgWidgets.registerCustom(type);
    }

    @Override
    public CreatureEntity deliverItemsAmazonStyle(GlobalPos globalPos, ItemStack... deliveredStacks) {
        return ProgrammedDroneUtils.deliverItemsAmazonStyle(globalPos, deliveredStacks);
    }

    @Override
    public CreatureEntity retrieveItemsAmazonStyle(GlobalPos globalPos, ItemStack... queriedStacks) {
        return ProgrammedDroneUtils.retrieveItemsAmazonStyle(globalPos, queriedStacks);
    }

    @Override
    public CreatureEntity deliverFluidAmazonStyle(GlobalPos globalPos, FluidStack deliveredFluid) {
        return ProgrammedDroneUtils.deliverFluidAmazonStyle(globalPos, deliveredFluid);
    }

    @Override
    public CreatureEntity retrieveFluidAmazonStyle(GlobalPos globalPos, FluidStack queriedFluid) {
        return ProgrammedDroneUtils.retrieveFluidAmazonStyle(globalPos, queriedFluid);
    }
}
