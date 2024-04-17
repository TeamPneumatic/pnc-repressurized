package me.desht.pneumaticcraft.common.thirdparty.ffs;

import dev.ftb.mods.ftbfiltersystem.api.FTBFilterSystemAPI;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class FFSSetup {
    public static void registerCaps(RegisterCapabilitiesEvent event) {
        Item ftbFilterItem = BuiltInRegistries.ITEM.get(new ResourceLocation(ModIds.FFS, "smart_filter"));
        if (ftbFilterItem != Items.AIR) {
            event.registerItem(PNCCapabilities.ITEM_FILTERING, (stack, ctx) ->
                    (filterStack, item) -> FTBFilterSystemAPI.api().doesFilterMatch(filterStack, item),
                    ftbFilterItem);
        }
    }
}
