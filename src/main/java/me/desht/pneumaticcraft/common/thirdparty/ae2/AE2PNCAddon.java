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
