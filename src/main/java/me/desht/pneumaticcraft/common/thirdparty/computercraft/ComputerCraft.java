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

package me.desht.pneumaticcraft.common.thirdparty.computercraft;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.block.entity.ILuaMethodProvider;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.ComputerEventManager;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

@Mod.EventBusSubscriber(modid = Names.MOD_ID)
public class ComputerCraft implements IThirdParty {
    private static boolean available;

    @Override
    public void preInit() {
        available = true;
    }

    @Override
    public void init() {
        ComputerEventManager.getInstance().registerSender((te, name, params) -> te.getCapability(PneumaticTilePeripheral.PERIPHERAL_CAPABILITY).ifPresent(handler -> {
            if (handler instanceof ComputerEventManager.IComputerEventSender) {
                ((ComputerEventManager.IComputerEventSender) handler).sendEvent(te, name, params);
            }
        }));
    }

    @SubscribeEvent
    public static void attachPeripheralCap(AttachCapabilitiesEvent<BlockEntity> event) {
        if (available && event.getObject() instanceof ILuaMethodProvider) {
            event.addCapability(RL(ModIds.COMPUTERCRAFT), new PneumaticPeripheralProvider((ILuaMethodProvider) event.getObject()));
        }
    }

    @Override
    public ThirdPartyManager.ModType modType() {
        return ThirdPartyManager.ModType.COMPUTER;
    }
}
