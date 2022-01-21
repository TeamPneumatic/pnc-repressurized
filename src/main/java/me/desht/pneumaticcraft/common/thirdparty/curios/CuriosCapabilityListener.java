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

package me.desht.pneumaticcraft.common.thirdparty.curios;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.item.ItemMemoryStick;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Names.MOD_ID)
public class CuriosCapabilityListener {
    @SubscribeEvent
    public static void attachCurioInvTicker(AttachCapabilitiesEvent<ItemStack> event) {
        if (Curios.available && event.getObject().getItem() instanceof ItemMemoryStick) {
            CuriosTickerCapability.addCuriosCap(event);
        }
    }
}
