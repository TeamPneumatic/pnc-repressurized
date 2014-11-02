package pneumaticCraft.common.thirdparty.forestry;

import java.util.Collection;

import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import forestry.api.farming.ICrop;
import forestry.api.farming.IFarmHousing;
import forestry.api.farming.IFarmLogic;

public abstract class FarmLogicWrapper implements IFarmLogic{

    private final IFarmLogic logic;

    public FarmLogicWrapper(IFarmHousing housing) throws Throwable{
        logic = getFarmLogic(housing);
    }

    protected abstract IFarmLogic getFarmLogic(IFarmHousing housing) throws Throwable;

    protected Class<? extends IFarmLogic> getLogicClass(String name) throws Throwable{
        return (Class<? extends IFarmLogic>)Class.forName("forestry.farming.logic." + name);
    }

    @Override
    public int getFertilizerConsumption(){
        return logic.getFertilizerConsumption();
    }

    @Override
    public int getWaterConsumption(float hydrationModifier){
        return logic.getWaterConsumption(hydrationModifier);
    }

    @Override
    public boolean isAcceptedResource(ItemStack itemstack){
        return logic.isAcceptedResource(itemstack);
    }

    @Override
    public boolean isAcceptedGermling(ItemStack itemstack){
        return logic.isAcceptedGermling(itemstack);
    }

    @Override
    public Collection<ItemStack> collect(){
        return logic.collect();
    }

    @Override
    public boolean cultivate(int x, int y, int z, ForgeDirection direction, int extent){
        return logic.cultivate(x, y, z, direction, extent);
    }

    @Override
    public Collection<ICrop> harvest(int x, int y, int z, ForgeDirection direction, int extent){
        return logic.harvest(x, y, z, direction, extent);
    }

    @Override
    public IIcon getIcon(){
        return logic.getIcon();
    }

    @Override
    public ResourceLocation getSpriteSheet(){
        return logic.getSpriteSheet();
    }

    @Override
    public String getName(){
        return logic.getName();
    }

    @Override
    public IFarmLogic setManual(boolean manual){
        return logic.setManual(manual);
    }

}
