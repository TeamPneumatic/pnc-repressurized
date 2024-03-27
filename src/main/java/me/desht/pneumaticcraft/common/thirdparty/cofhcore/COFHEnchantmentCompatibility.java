package me.desht.pneumaticcraft.common.thirdparty.cofhcore;

import me.desht.pneumaticcraft.api.pressure.IPressurizableItem;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.thirdparty.PneumaticcraftIMC;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.fml.InterModComms;

import java.util.function.Supplier;

public class COFHEnchantmentCompatibility {
    /**
     * Makes all pressurizable items compatible with the COFH Holding enchantment
     */
    public static void makeHoldingCompatible() {
        // Gets if Holding is enabled in the config
        boolean holdingEnabled = ConfigHelper.common().integration.cofhHoldingMultiplier.get() > 0;

        // Goes through all items in the item registry
        for (Item item : BuiltInRegistries.ITEM) {
            // Allow any pressurizable items to take the CoFH holding enchantment
            if (item instanceof IPressurizableItem) {
                Log.info("Making item: " + item + " compatible with Holding enchantment.");

                // Sends IMC to CoFH core to add Holding compatibility to item
                Supplier<Item> sItem = () -> item;
                PneumaticcraftIMC.addIMCMessageToCache(new InterModComms.IMCMessage(null,
                        ModIds.COFH_CORE, "add_holding_compatibility", sItem));
            }
        }
    }
}
