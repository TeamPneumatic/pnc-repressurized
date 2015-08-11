package pneumaticCraft.common.block.tubes;

import pneumaticCraft.client.model.IBaseModel;
import pneumaticCraft.client.model.tubemodules.ModelLogisticsModule;
import pneumaticCraft.lib.Names;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class ModuleLogistics extends TubeModule{
    private static final ModelLogisticsModule model = new ModelLogisticsModule();

    @Override
    public double getWidth(){
        return 13 / 16D;
    }

    @Override
    protected double getHeight(){
        return 4.5D / 16D;
    }

    @Override
    public String getType(){
        return Names.MODULE_LOGISTICS;
    }

    @Override
    public IBaseModel getModel(){
        return model;
    }

    @Override
    protected EnumGuiId getGuiId(){
        return null;
    }

}
