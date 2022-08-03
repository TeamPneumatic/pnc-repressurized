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

package me.desht.pneumaticcraft.common.thirdparty.cofhcore;

import me.desht.pneumaticcraft.api.pressure.IPressurizableItem;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class CoFHCore implements IThirdParty {

    private static boolean cofhVersionOK;

    @Override
    public void preInit() {
        // FIXME bit of a hack here, but we need to be sure we have a compatible version of CoFH Core
        cofhVersionOK = checkForEnchantableItemInterface();

        if (cofhVersionOK) {
            MinecraftForge.EVENT_BUS.register(CapabilityListener.class);

            // CoFH Core doesn't actually register the Holding enchantment itself; it's done by Thermal Expansion
            // we'll call it too, in the case that CoFH Core is present without TE
            // should be safe to call this multiple times (subsequent calls are effectively a no-op)
            HoldingEnchantableProvider.registerEnchantment();
        }
    }

    @Override
    public void init() {
        // note: fuel registration is now done by datapack: see conditional recipes in ModRecipeProvider

        if (cofhVersionOK) {
            // holding enchantment adds another volume multiplier
            HoldingEnchantableProvider.registerVolumeModifier();

            if (ModList.get().isLoaded(ModIds.THERMAL)) {
                ThermalExplosiveLaunching.registerLaunchBehaviour();
            }
        }
    }

    private boolean checkForEnchantableItemInterface() {
        try {
            Class.forName("cofh.lib.capability.IEnchantableItem");
            return true;
        } catch (ClassNotFoundException e) {
            Log.error("CoFH IEnchantableItem interface is not where we expected! Continuing, but PneumaticCraft items won't be able to use the Holding enchantment. Notify the PNC mod author, including the versions of PNC and CoFH Core you're using, if you see this error.");
            return false;
        }
    }

    public static class CapabilityListener {
        @SubscribeEvent
        public static void attachCap(AttachCapabilitiesEvent<ItemStack> event) {
            // allow any pressurizable items to take the CoFH holding enchantment
            if (event.getObject().getItem() instanceof IPressurizableItem) {
                event.addCapability(RL("cofh_enchantable"), new HoldingEnchantableProvider());
            }
        }
    }
}
