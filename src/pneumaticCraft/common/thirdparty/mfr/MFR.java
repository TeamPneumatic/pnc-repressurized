package pneumaticCraft.common.thirdparty.mfr;

import net.minecraft.block.Block;
import net.minecraftforge.fluids.FluidRegistry;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.thirdparty.IThirdParty;
import powercrystals.minefactoryreloaded.api.IFactoryFertilizable;
import powercrystals.minefactoryreloaded.api.IFactoryHarvestable;
import powercrystals.minefactoryreloaded.api.IFactoryPlantable;

public class MFR implements IThirdParty{
    private Class registryClass;

    @Override
    public void preInit(){
        try {
            for(Block block : ItemPlasticPlants.getBlockToSeedMap().keySet()) {
                register("registerHarvestable", IFactoryHarvestable.class, new PlasticHarvester(block));
                register("registerFertilizable", IFactoryFertilizable.class, new PlasticFertilizer(block));
            }
            register("registerPlantable", IFactoryPlantable.class, new PlasticPlanter());
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(){
        PneumaticRegistry.getInstance().registerXPLiquid(FluidRegistry.getFluid("mobessence"), 77);
    }

    @Override
    public void postInit(){

    }

    @Override
    public void clientSide(){

    }

    private void register(String methodName, Class parameterType, Object parameter) throws Throwable{
        if(registryClass == null) {
            registryClass = Class.forName("powercrystals.minefactoryreloaded.MFRRegistry");
        }
        registryClass.getMethod(methodName, parameterType).invoke(null, parameter);
    }

    @Override
    public void clientInit(){}

}
