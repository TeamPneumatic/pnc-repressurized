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

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.item.PneumaticArmorItem;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class Mekanism implements IThirdParty {
    @Override
    public void init() {
        MekanismIntegration.mekSetup();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void attachHeatAdapters(AttachCapabilitiesEvent<BlockEntity> event) {
        if (ConfigHelper.common().integration.mekThermalEfficiencyFactor.get() != 0 && MekanismIntegration.CAPABILITY_HEAT_HANDLER != null) {
            if (event.getObject().getType().getRegistryName().getNamespace().equals(Names.MOD_ID)) {
                event.addCapability(RL("pnc2mek_heat_adapter"), new PNC2MekHeatProvider(event.getObject()));
            }
            if (event.getObject().getType().getRegistryName().getNamespace().equals(ModIds.MEKANISM)) {
                event.addCapability(RL("mek2pnc_heat_adapter"), new Mek2PNCHeatProvider(event.getObject()));
            }
        }
    }

    @SubscribeEvent
    public void attachRadiationShield(AttachCapabilitiesEvent<ItemStack> event) {
        if (MekanismIntegration.CAPABILITY_RADIATION_SHIELDING != null) {
            if (event.getObject().getItem() instanceof PneumaticArmorItem armor) {
                event.addCapability(RL("mek_rad_shielding"),
                        new MekRadShieldProvider(event.getObject(), armor.getSlot())
                );
            }
        }
    }
}
