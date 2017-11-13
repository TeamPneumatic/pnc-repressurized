package me.desht.pneumaticcraft.common.thirdparty.ae2;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.thirdparty.IThirdParty;

public class AE2 implements IThirdParty {

    @Override
    public void preInit() {
    }

    @Override
    public void init() {
    	PneumaticRegistry.getInstance().getItemRegistry().registerInventoryItem(new AE2DiskInventoryItemHandler());
    }

    @Override
    public void postInit() {
    }

    @Override
    public void clientSide() {
    }

    @Override
    public void clientInit() {
    }

}
