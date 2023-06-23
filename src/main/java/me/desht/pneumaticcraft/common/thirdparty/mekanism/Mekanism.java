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

import me.desht.pneumaticcraft.common.block.entity.IHeatExchangingTE;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.core.ModHoeHandlers;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class Mekanism implements IThirdParty {
    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().register(ModBusListener.class);
    }

    @Override
    public void init() {
        MekanismIntegration.mekSetup();
    }

    @SubscribeEvent
    public void attachHeatAdapters(AttachCapabilitiesEvent<BlockEntity> event) {
        if (ConfigHelper.common().integration.mekThermalEfficiencyFactor.get() != 0) {
            if (event.getObject() instanceof IHeatExchangingTE) {
                event.addCapability(RL("pnc2mek_heat_adapter"), new PNC2MekHeatProvider(event.getObject()));
            }
            if (MekanismIntegration.isMekHeatHandler(event.getObject())) {
                event.addCapability(RL("mek2pnc_heat_adapter"), new Mek2PNCHeatProvider(event.getObject()));
            }
        }
    }

    @SubscribeEvent
    public void attachRadiationShield(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() instanceof PneumaticArmorItem armor) {
            event.addCapability(RL("mek_rad_shielding"), new MekRadShieldProvider(event.getObject(), armor.getType().getSlot()));
        }
    }

    public static class ModBusListener {
        @SubscribeEvent
        public static void registerPaxelHandler(RegisterEvent event) {
            event.register(ModHoeHandlers.HOE_HANDLERS_DEFERRED.getRegistryKey(), RL("mekanism_paxels"), PaxelHandler::new);
        }
    }
}
