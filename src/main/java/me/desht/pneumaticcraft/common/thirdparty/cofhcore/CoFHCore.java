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

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.item.ItemVolumeModifier;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.api.pressure.IPressurizableItem;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class CoFHCore implements IThirdParty {
    static Enchantment holdingEnchantment = null;

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
            holdingEnchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("cofh_core", "holding"));
            if (holdingEnchantment != null) {
                PneumaticRegistry.getInstance().getItemRegistry().registerPneumaticVolumeModifier(new COFHVolumeModifier());
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

    public static class COFHVolumeModifier implements ItemVolumeModifier {
        @Override
        public int getNewVolume(ItemStack stack, int oldVolume) {
            return oldVolume * (1 + EnchantmentHelper.getItemEnchantmentLevel(holdingEnchantment, stack));
        }

        @Override
        public void addInfo(ItemStack stack, List<Component> text) {
            int nHolding = EnchantmentHelper.getItemEnchantmentLevel(holdingEnchantment, stack);
            if (nHolding > 0) {
                text.add(new TextComponent(Symbols.TRIANGLE_RIGHT + " ").append(holdingEnchantment.getFullname(nHolding)));
            }
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
