package me.desht.pneumaticcraft.common.thirdparty.ffs;

import dev.ftb.mods.ftbfiltersystem.api.FTBFilterSystemAPI;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class FFSSetup {
    public static void registerCaps(RegisterCapabilitiesEvent event) {
        Item ftbFilterItem = FTBFilterSystemAPI.api().filterItem();
        if (ftbFilterItem != Items.AIR) {
            event.registerItem(PNCCapabilities.ITEM_FILTERING, (stack, ctx) ->
                    (filterStack, item) -> FTBFilterSystemAPI.api().doesFilterMatch(filterStack, item),
                    ftbFilterItem);
        }
    }
}
