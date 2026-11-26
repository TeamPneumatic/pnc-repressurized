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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;

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
            event.registerEntity(Capabilities.Item.ENTITY, d.get(), (drone, ctx) -> drone.getDroneItemHandler());
            event.registerEntity(Capabilities.Fluid.ENTITY, d.get(), (drone, ctx) -> drone.getFluidTank());
            event.registerEntity(Capabilities.Energy.ENTITY, d.get(), (drone, ctx) -> drone.getEnergyStorage());
        });

        event.registerEntity(PNCCapabilities.HEAT_EXCHANGER_ENTITY, ModEntityTypes.HEAT_FRAME.get(), (frame, ctx) -> frame.getHeatExchangerLogic());

        event.registerEntity(PNCCapabilities.ENTITY_AUTOMATION, EntityType.PLAYER, (player, dir) -> {
            if (dir == null) {
                return PlayerInventoryWrapper.of(player);
            } else if (dir.getAxis().isVertical()) {
                return PlayerInventoryWrapper.of(player).getMainSlots();
            } else {
                return PlayerInventoryWrapper.of(player).getArmorSlots();
            }
        });

        // block entities
        // Since 26.1 this event fires before item components are bound, and many block entity constructors create
        //  ItemStacks (e.g. side configurator icons), so dummy block entities can't be created here any more.
        //  Instead, the providers are registered for every PNC block entity type; they check the actual block entity
        //  and return null when it doesn't have the capability, which is the same as not registering it.
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.getEntries().forEach(holder -> {
            BlockEntityType<?> type = holder.get();
            event.registerBlockEntity(Capabilities.Item.BLOCK, type,
                    (object, dir) -> object instanceof AbstractPneumaticCraftBlockEntity be && be.hasItemCapability() ? be.getItemHandler(dir) : null);
            event.registerBlockEntity(Capabilities.Fluid.BLOCK, type,
                    (object, dir) -> object instanceof AbstractPneumaticCraftBlockEntity be && be.hasFluidCapability() ? be.getFluidHandler(dir) : null);
            event.registerBlockEntity(Capabilities.Energy.BLOCK, type,
                    (object, dir) -> object instanceof AbstractPneumaticCraftBlockEntity be && be.hasEnergyCapability() ? be.getEnergyHandler(dir) : null);
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
            if (entry.get() instanceof IFluidCapProvider f) {
                event.registerItem(Capabilities.Fluid.ITEM, (stack, access) -> f.provideFluidCapability(access), entry.get());
            }
        });
    }
}
