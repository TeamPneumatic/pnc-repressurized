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

package me.desht.pneumaticcraft.common.thirdparty.ae2;

import appeng.api.AEAddon;
import appeng.api.IAEAddon;
import appeng.api.IAppEngApi;
import appeng.api.util.AEColor;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import net.minecraft.item.Item;

@AEAddon
public class AE2PNCAddon implements IAEAddon {
    static IAppEngApi api;

    public static Item glassCable() {
        return api.definitions().parts().cableGlass().item(AEColor.TRANSPARENT);
    }

    @Override
    public void onAPIAvailable(IAppEngApi iAppEngApi) {
        AE2Integration.setAvailable();
        api = iAppEngApi;

        PneumaticRegistry.getInstance().getItemRegistry().registerInventoryItem(new AE2DiskInventoryItemHandler());
    }
}
