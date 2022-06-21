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

import me.desht.pneumaticcraft.common.block.entity.IHeatExchangingTE;
import me.desht.pneumaticcraft.common.core.ModHarvestHandlers;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class ImmersiveEngineering implements IThirdParty {
    @SuppressWarnings("unused")
    public static final RegistryObject<HempHarvestHandler> HEMP_HARVEST = ModHarvestHandlers.register("ie_hemp", HempHarvestHandler::new);

    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(ElectricAttackHandler.class);
        MinecraftForge.EVENT_BUS.register(ExternalHeatCapListener.class);
    }

    public static class ExternalHeatCapListener {
        @SubscribeEvent
        public static void attachExternalHeatHandler(AttachCapabilitiesEvent<BlockEntity> event) {
            if (event.getObject() instanceof IHeatExchangingTE) {
                IEHeatHandler.Provider provider = new IEHeatHandler.Provider(event.getObject());
                event.addCapability(RL("ie_external_heatable"), provider);
                event.addListener(provider::invalidate);
            }
        }
    }
}
