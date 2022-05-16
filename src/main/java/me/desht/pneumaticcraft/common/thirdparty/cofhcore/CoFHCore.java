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
import me.desht.pneumaticcraft.api.lib.Names;
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
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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
            // CoFH Core doesn't actually register the Holding enchantment itself; it's done by Thermal Expansion
            // we'll call it too, in the case that CoFH Core is present without TE
            // should be safe to call this multiple times (subsequent calls are effectively a no-op)
            HoldingEnchantableProvider.registerEnchantment();
        }
    }

    @Override
    public void init() {
        // note: fuel registration is now by datapack: all done by conditional recipes in ModRecipeProvider

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
            Log.error("CoFH IEnchantableItem interface is not where we expected! Continuing, but PneumaticCraft items won't be able to use the Holding enchantment. Report this to the PNC mod author if you see it.");
            return false;
        }
    }

    public static class COFHVolumeModifier implements ItemVolumeModifier {
        @Override
        public int getNewVolume(ItemStack stack, int oldVolume) {
            if (holdingEnchantment == null) return oldVolume;

            return oldVolume * (1 + EnchantmentHelper.getItemEnchantmentLevel(holdingEnchantment, stack));
        }

        @Override
        public void addInfo(ItemStack stack, List<Component> text) {
            if (holdingEnchantment == null) return;

            int nHolding = EnchantmentHelper.getItemEnchantmentLevel(holdingEnchantment, stack);
            if (nHolding > 0) {
                text.add(new TextComponent(Symbols.TRIANGLE_RIGHT + " ").append(holdingEnchantment.getFullname(nHolding)));
            }
        }
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void attachCap(AttachCapabilitiesEvent<ItemStack> event) {
            // potentially allow any pressurizable items to take the CoFH holding enchantment
            if (holdingEnchantment != null && event.getObject().getItem() instanceof IPressurizableItem) {
                event.addCapability(RL("cofh_enchantable"), new HoldingEnchantableProvider());
            }
        }
    }
}
