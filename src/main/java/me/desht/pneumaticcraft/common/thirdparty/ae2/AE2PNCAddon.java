package me.desht.pneumaticcraft.common.thirdparty.ae2;

import appeng.api.AEAddon;
import appeng.api.IAEAddon;
import appeng.api.IAppEngApi;
import me.desht.pneumaticcraft.api.PneumaticRegistry;

@AEAddon
public class AE2PNCAddon implements IAEAddon {
    static IAppEngApi api;

    @Override
    public void onAPIAvailable(IAppEngApi iAppEngApi) {
        AE2Integration.setAvailable();
        api = iAppEngApi;

        PneumaticRegistry.getInstance().getItemRegistry().registerInventoryItem(new AE2DiskInventoryItemHandler());
    }
}
