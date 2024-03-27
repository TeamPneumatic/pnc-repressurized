package me.desht.pneumaticcraft;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.pressure.IPressurizableItem;
import me.desht.pneumaticcraft.common.block.entity.AbstractAirHandlingBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.AbstractPneumaticCraftBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.IHeatExchangingTE;
import me.desht.pneumaticcraft.common.capabilities.AirHandlerItemStack;
import me.desht.pneumaticcraft.common.item.IFluidCapProvider;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.List;

public class CapabilitySetup {
    public static void registerCaps(RegisterCapabilitiesEvent event) {
        // entities
        List.of(ModEntityTypes.DRONE,
                ModEntityTypes.COLLECTOR_DRONE,
                ModEntityTypes.GUARD_DRONE,
                ModEntityTypes.LOGISTICS_DRONE,
                ModEntityTypes.HARVESTING_DRONE,
                ModEntityTypes.AMADRONE
        ).forEach(d -> {
            event.registerEntity(PNCCapabilities.AIR_HANDLER_ENTITY, d.get(), (drone, ctx) -> drone.getAirHandler());
            event.registerEntity(Capabilities.ItemHandler.ENTITY, d.get(), (drone, ctx) -> drone.getDroneItemHandler());
            event.registerEntity(Capabilities.FluidHandler.ENTITY, d.get(), (drone, ctx) -> drone.getFluidTank());
            event.registerEntity(Capabilities.EnergyStorage.ENTITY, d.get(), (drone, ctx) -> drone.getEnergyStorage());
        });

        event.registerEntity(PNCCapabilities.HEAT_EXCHANGER_ENTITY, ModEntityTypes.HEAT_FRAME.get(), (frame, ctx) -> frame.getHeatExchangerLogic());

        // block entities
        ModBlockEntityTypes.streamBlockEntities().forEach(blockEntity -> {
            if (blockEntity instanceof AbstractPneumaticCraftBlockEntity pncBE) {
                if (pncBE.hasItemCapability()) {
                    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, pncBE.getType(),
                            (object, dir) -> object instanceof AbstractPneumaticCraftBlockEntity be ? be.getItemHandler(dir) : null);
                }
                if (pncBE.hasFluidCapability()) {
                    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, pncBE.getType(),
                            (object, dir) -> object instanceof AbstractPneumaticCraftBlockEntity be ? be.getFluidHandler(dir) : null);
                }
                if (pncBE.hasEnergyCapability()) {
                    event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, pncBE.getType(),
                            (object, dir) -> object instanceof AbstractPneumaticCraftBlockEntity be ? be.getEnergyHandler(dir) : null);
                }
                if (pncBE instanceof IHeatExchangingTE) {
                    event.registerBlockEntity(PNCCapabilities.HEAT_EXCHANGER_BLOCK, pncBE.getType(),
                            (object, dir) -> object instanceof IHeatExchangingTE heat ? heat.getHeatExchanger(dir) : null);
                }
                if (pncBE instanceof AbstractAirHandlingBlockEntity) {
                    event.registerBlockEntity(PNCCapabilities.AIR_HANDLER_MACHINE, pncBE.getType(),
                            (object, dir) -> object instanceof AbstractAirHandlingBlockEntity a ? a.getAirHandler(dir) : null);
                }
            }
        });

        // items
        ModItems.ITEMS.getEntries().forEach(entry -> {
            if (entry.get() instanceof IPressurizableItem) {
                event.registerItem(PNCCapabilities.AIR_HANDLER_ITEM, (stack, ctx) -> new AirHandlerItemStack(stack), entry.get());
            }
            if (entry.get() instanceof IFluidCapProvider f) {
                event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> f.provideFluidCapability(stack), entry.get());
            }
        });
    }
}
