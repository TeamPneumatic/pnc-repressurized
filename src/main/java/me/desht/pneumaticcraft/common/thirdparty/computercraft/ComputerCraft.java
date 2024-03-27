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

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.common.thirdparty.ThirdPartyManager;
import me.desht.pneumaticcraft.common.thirdparty.computer_common.ComputerEventManager;
import net.neoforged.bus.api.IEventBus;

public class ComputerCraft implements IThirdParty {
    static boolean available;

    @Override
    public void preInit(IEventBus modBus) {
        available = true;

        modBus.addListener(PneumaticTilePeripheral::attachPeripheralCap);
    }

    @Override
    public void init() {
        ComputerEventManager.getInstance().registerSender((te, name, params) ->
                PneumaticTilePeripheral.getPeripheral(te).ifPresent(handler -> {
                    if (handler instanceof ComputerEventManager.IComputerEventSender sender) {
                        sender.sendEvent(te, name, params);
                    }
                })
        );
    }

    @Override
    public ThirdPartyManager.ModType modType() {
        return ThirdPartyManager.ModType.COMPUTER;
    }
}
