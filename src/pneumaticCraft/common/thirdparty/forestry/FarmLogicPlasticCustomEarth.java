package pneumaticCraft.common.thirdparty.forestry;

import java.lang.reflect.Constructor;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import forestry.api.farming.IFarmHousing;
import forestry.api.farming.IFarmLogic;
import forestry.api.farming.IFarmable;

public abstract class FarmLogicPlasticCustomEarth extends FarmLogicPlasticNormal{

    public FarmLogicPlasticCustomEarth(IFarmHousing housing) throws Throwable{
        super(housing);
    }

    @Override
    protected IFarmLogic getFarmLogic(IFarmHousing housing) throws Throwable{
        ItemStack[] resources = new ItemStack[]{getEarth()};
        Constructor c = getLogicClass("FarmLogicArboreal").getConstructor(IFarmHousing.class, ItemStack[].class, ItemStack.class, IFarmable[].class);
        return (IFarmLogic)c.newInstance(housing, resources, resources[0], new IFarmable[]{new FarmablePlastic(getBlock())});
    }

    protected abstract ItemStack getEarth();

    @Override
    public ResourceLocation getSpriteSheet(){
        return TextureMap.locationItemsTexture;
    }
}
