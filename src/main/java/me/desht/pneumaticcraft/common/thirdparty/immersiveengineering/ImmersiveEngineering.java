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

package me.desht.pneumaticcraft.common.thirdparty.immersiveengineering;

import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import me.desht.pneumaticcraft.common.block.entity.IHeatExchangingTE;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModHarvestHandlers;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Supplier;

public class ImmersiveEngineering implements IThirdParty {
    @SuppressWarnings("unused")
    public static final Supplier<HempHarvestHandler> HEMP_HARVEST
            = ModHarvestHandlers.register("ie_hemp", HempHarvestHandler::new);

    @Override
    public void preInit(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(ElectricAttackHandler::onElectricalAttack);
        NeoForge.EVENT_BUS.addListener(IEHeatHandler::registerCap);
    }

    public static class ExternalHeatCapListener {
        @SubscribeEvent
        public static void registerCap(RegisterCapabilitiesEvent event) {
            ModBlockEntityTypes.streamBlockEntities()
                    .filter(be -> be instanceof IHeatExchangingTE)
                    .forEach(be -> event.registerBlockEntity(ExternalHeaterHandler.CAPABILITY, be.getType(), IEHeatHandler::maybe));
        }

        //        @SubscribeEvent
//        public static void attachExternalHeatHandler(AttachCapabilitiesEvent<BlockEntity> event) {
//            if (event.getObject() instanceof IHeatExchangingTE) {
//                IEHeatHandler.Provider provider = new IEHeatHandler.Provider(event.getObject());
//                event.addCapability(RL("ie_external_heatable"), provider);
//                event.addListener(provider::invalidate);
//            }
//        }
//    }
    }
}
