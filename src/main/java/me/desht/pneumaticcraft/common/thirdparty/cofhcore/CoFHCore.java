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

import cofh.core.init.CoreEnchantments;
import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.api.pressure.IPressurizableItem;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

public class CoFHCore implements IThirdParty {
    static Enchantment holdingEnchantment = null;
    private static boolean versionOK;

    @Override
    public void preInit() {
        // FIXME bit of a hack here, but we need to be sure we have a compatible version of CoFH Core
        // TODO 1.18 should be able to get rid of this once CoFH Core has a public API
        versionOK = versionOK();

        if (versionOK) {
            // CoFH Core doesn't actually register the Holding enchantment itself; it's done by Thermal Expansion
            // we'll call it too, in the case that CoFH Core is present without TE
            // should be safe to call this multiple times (subsequent calls are effectively a no-op)
            CoreEnchantments.registerHoldingEnchantment();
        }
    }

    @Override
    public void init() {
        // note: fuel registration is now by datapack: all done by conditional recipes in ModRecipeProvider

        if (versionOK) {
            // holding enchantment adds another volume multiplier
            holdingEnchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("cofh_core", "holding"));
            if (holdingEnchantment != null) {
                PneumaticRegistry.getInstance().getItemRegistry().registerPneumaticVolumeModifier(
                        (stack, oldVolume) -> oldVolume * (1 + EnchantmentHelper.getItemEnchantmentLevel(holdingEnchantment, stack))
                );
            }
        }
    }

    public static int getHoldingUpgrades(ItemStack stack) {
        return holdingEnchantment == null ? 0 : EnchantmentHelper.getItemEnchantmentLevel(holdingEnchantment, stack);
    }

    public static ITextComponent holdingEnchantmentName(int level) {
        return holdingEnchantment == null ? StringTextComponent.EMPTY : holdingEnchantment.getFullname(level);
    }

    private static boolean versionOK() {
        try {
            Class.forName("cofh.lib.capability.IEnchantableItem");
            return true;
        } catch (ClassNotFoundException e) {
            Log.error("PneumaticCraft: Repressurized found an older (pre-1.2.0) release of CoFH Core. Continuing, but PneumaticCraft items won't be able to use the Holding enchantment. Upgrade to CoFH Core 1.2.0 or later if possible.");
            return false;
        }
    }

    @Mod.EventBusSubscriber(modid = Names.MOD_ID)
    public static class Listener {
        @SubscribeEvent
        public static void attachCap(AttachCapabilitiesEvent<ItemStack> event) {
            // potentially allow any pressurizable items to take the CoFH holding enchantment
            if (versionOK && holdingEnchantment != null && HoldingEnchantableProvider.CAPABILITY_ENCHANTABLE_ITEM != null
                    && event.getObject().getItem() instanceof IPressurizableItem) {
                event.addCapability(RL("cofh_enchantable"), new HoldingEnchantableProvider());
            }
        }

    }
}
