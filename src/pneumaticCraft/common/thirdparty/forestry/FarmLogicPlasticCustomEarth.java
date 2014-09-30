package pneumaticCraft.common.thirdparty.forestry;

import java.lang.reflect.Constructor;

import net.minecraft.item.ItemStack;
import forestry.api.farming.IFarmHousing;
import forestry.api.farming.IFarmLogic;
import forestry.api.farming.IFarmable;

public abstract class FarmLogicPlasticCustomEarth extends FarmLogicPlasticNormal{

    public FarmLogicPlasticCustomEarth(IFarmHousing housing) throws Throwable{
        super(housing);
    }

    @Override
    protected IFarmLogic getFarmLogic(IFarmHousing housing) throws Throwable{
        ItemStack[] resources = getEarth();
        Constructor c = getLogicClass("FarmLogicArboreal").getConstructor(IFarmHousing.class, ItemStack[].class, ItemStack[].class, ItemStack[].class, IFarmable[].class);
        return (IFarmLogic)c.newInstance(housing, resources, resources, new ItemStack[0], new IFarmable[]{new FarmablePlastic(getBlock())});
    }

    protected abstract ItemStack[] getEarth();

}
