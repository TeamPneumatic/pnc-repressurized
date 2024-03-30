/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.thirdparty.mekanism;

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.common.block.entity.IHeatExchangingTE;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModHoeHandlers;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.thirdparty.RadiationSourceCheck;
import me.desht.pneumaticcraft.lib.ModIds;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.radiation.capability.IRadiationShielding;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class MekanismIntegration {
    public static final BlockCapability<IHeatHandler, Direction> CAPABILITY_HEAT_HANDLER
            = BlockCapability.createSided(new ResourceLocation(ModIds.MEKANISM, "heat_handler"), IHeatHandler.class);
    public static final ItemCapability<IRadiationShielding,Void> CAPABILITY_RADIATION_SHIELDING
        = ItemCapability.createVoid(new ResourceLocation(ModIds.MEKANISM, "radiation_shielding"), IRadiationShielding.class);

    static void mekSetup() {
        RadiationSourceCheck.INSTANCE.registerRadiationSource((registryAccess, dmgSource)
                -> dmgSource == IRadiationManager.INSTANCE.getRadiationDamageSource(registryAccess));
    }

    static void registerCaps(RegisterCapabilitiesEvent event) {
        for (Supplier<PneumaticArmorItem> item : List.of(ModItems.PNEUMATIC_HELMET, ModItems.PNEUMATIC_CHESTPLATE, ModItems.PNEUMATIC_LEGGINGS, ModItems.PNEUMATIC_BOOTS)) {
            event.registerItem(CAPABILITY_RADIATION_SHIELDING, (stack, ctx) -> new RadiationShieldingProvider(stack, item.get().getType()), item.get());
        }

        ModBlockEntityTypes.streamBlockEntities()
                .filter(be -> be instanceof IHeatExchangingTE)
                .forEach(be -> event.registerBlockEntity(CAPABILITY_HEAT_HANDLER, be.getType(), PNC2MekHeatAdapter::maybe));

        registerMekanismBlockEntityHeat(event, "fuelwood_heater");
        registerMekanismBlockEntityHeat(event, "resistive_heater");
        registerMekanismBlockEntityHeat(event, "basic_thermodynamic_conductor");
        registerMekanismBlockEntityHeat(event, "advanced_thermodynamic_conductor");
        registerMekanismBlockEntityHeat(event, "elite_thermodynamic_conductor");
        registerMekanismBlockEntityHeat(event, "ultimate_thermodynamic_conductor");
    }

    static void registerMekanismBlockEntityHeat(RegisterCapabilitiesEvent event, String id) {
        BlockEntityType<?> type = BuiltInRegistries.BLOCK_ENTITY_TYPE.get(new ResourceLocation(ModIds.MEKANISM, id));
        if (type != null) {
            event.registerBlockEntity(PNCCapabilities.HEAT_EXCHANGER_BLOCK, type, Mek2PNCHeatAdapter::maybe);
        }
    }

    static void registerPaxelHandler(RegisterEvent event) {
        event.register(ModHoeHandlers.HOE_HANDLERS_DEFERRED.getRegistryKey(), RL("mekanism_paxels"), PaxelHandler::new);
    }

    static boolean isMekHeatHandler(BlockEntity blockEntity) {
        return blockEntity instanceof IMekanismHeatHandler;
    }
}
