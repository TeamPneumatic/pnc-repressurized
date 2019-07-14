package me.desht.pneumaticcraft.common.thirdparty.ae2;

import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;

public class AE2 implements IThirdParty {

    @Override
    public void init() {
    	PneumaticRegistry.getInstance().getItemRegistry().registerInventoryItem(new AE2DiskInventoryItemHandler());
    }

}
