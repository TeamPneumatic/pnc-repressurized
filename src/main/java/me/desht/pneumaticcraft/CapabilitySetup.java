package me.desht.pneumaticcraft;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.pressure.IPressurizableItem;
import me.desht.pneumaticcraft.common.block.entity.AbstractAirHandlingBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.IHeatExchangingTE;
import me.desht.pneumaticcraft.common.capabilities.AirHandlerItemStack;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.PlayerArmorInvWrapper;
import net.neoforged.neoforge.items.wrapper.PlayerInvWrapper;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;

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
            // TODO 26.1 transfer: export as ResourceHandler/EnergyHandler (PR2)
//            event.registerEntity(Capabilities.ItemHandler.ENTITY, d.get(), (drone, ctx) -> drone.getDroneItemHandler());
//            event.registerEntity(Capabilities.FluidHandler.ENTITY, d.get(), (drone, ctx) -> drone.getFluidTank());
//            event.registerEntity(Capabilities.EnergyStorage.ENTITY, d.get(), (drone, ctx) -> drone.getEnergyStorage());
        });

        event.registerEntity(PNCCapabilities.HEAT_EXCHANGER_ENTITY, ModEntityTypes.HEAT_FRAME.get(), (frame, ctx) -> frame.getHeatExchangerLogic());

        event.registerEntity(PNCCapabilities.ENTITY_AUTOMATION, EntityType.PLAYER, (player, dir) -> {
            if (dir == null) {
                return new PlayerInvWrapper(player.getInventory());
            } else if (dir.getAxis().isVertical()) {
                return new PlayerMainInvWrapper(player.getInventory());
            } else if (dir.getAxis().isHorizontal()) {
                return new PlayerArmorInvWrapper(player.getInventory());
            } else {
                return null;
            }
        });

        // block entities
        // Since 26.1 this event fires before item components are bound, and many block entity constructors create
        //  ItemStacks (e.g. side configurator icons), so dummy block entities can't be created here any more.
        //  Instead, the providers are registered for every PNC block entity type; they check the actual block entity
        //  and return null when it doesn't have the capability, which is the same as not registering it.
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.getEntries().forEach(holder -> {
            BlockEntityType<?> type = holder.get();
            // TODO 26.1 transfer: PNC's item/fluid/energy handlers still implement the legacy interfaces and
            //  can't be registered for the new Capabilities.Item/Fluid/Energy; exported in PR2
//            if (pncBE.hasItemCapability()) {
//                event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, pncBE.getType(),
//                        (object, dir) -> object instanceof AbstractPneumaticCraftBlockEntity be ? be.getItemHandler(dir) : null);
//            }
//            if (pncBE.hasFluidCapability()) {
//                event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, pncBE.getType(),
//                        (object, dir) -> object instanceof AbstractPneumaticCraftBlockEntity be ? be.getFluidHandler(dir) : null);
//            }
//            if (pncBE.hasEnergyCapability()) {
//                event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, pncBE.getType(),
//                        (object, dir) -> object instanceof AbstractPneumaticCraftBlockEntity be ? be.getEnergyHandler(dir) : null);
//            }
            event.registerBlockEntity(PNCCapabilities.HEAT_EXCHANGER_BLOCK, type,
                    (object, dir) -> object instanceof IHeatExchangingTE heat ? heat.getHeatExchanger(dir) : null);
            event.registerBlockEntity(PNCCapabilities.AIR_HANDLER_MACHINE, type,
                    (object, dir) -> object instanceof AbstractAirHandlingBlockEntity a ? a.getAirHandler(dir) : null);
        });

        // items
        ModItems.ITEMS.getEntries().forEach(entry -> {
            if (entry.get() instanceof IPressurizableItem) {
                event.registerItem(PNCCapabilities.AIR_HANDLER_ITEM, (stack, ctx) -> new AirHandlerItemStack(stack), entry.get());
            }
            // TODO 26.1 transfer: item fluid handlers are exported in PR2
//            if (entry.get() instanceof IFluidCapProvider f) {
//                event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> f.provideFluidCapability(stack), entry.get());
//            }
        });
    }
}
