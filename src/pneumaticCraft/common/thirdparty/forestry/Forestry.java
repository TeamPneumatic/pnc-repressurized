package pneumaticCraft.common.thirdparty.forestry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.thirdparty.IThirdParty;
import pneumaticCraft.lib.Log;
import cpw.mods.fml.relauncher.ReflectionHelper;
import forestry.api.circuits.ChipsetManager;
import forestry.api.circuits.ICircuit;
import forestry.api.circuits.ICircuitLayout;
import forestry.api.farming.Farmables;
import forestry.api.farming.IFarmHousing;
import forestry.api.recipes.RecipeManagers;

public class Forestry implements IThirdParty{

    public static HashSet<Block> farmStructureBlocks;
    public static Item plasticElectronTube;
    private static Class[] logics = new Class[]{FarmLogicSquid.class, FarmLogicFire.class, FarmLogicCreeper.class, FarmLogicSlime.class, FarmLogicRain.class, FarmLogicEnder.class, FarmLogicLightning.class, null, FarmLogicBurst.class, FarmLogicPotion.class, FarmLogicRepulsion.class, FarmLogicHelium.class, FarmLogicChopper.class, null, FarmLogicPropulsion.class, FarmLogicFlying.class};

    @Override
    public void preInit(){
        plasticElectronTube = new ItemPlasticElectronTube("plasticElectronTube");
        Itemss.registerItem(plasticElectronTube);

        Collection col = Farmables.farmables.get("farmVegetables");
        // if(col != null) {
        ICircuitLayout layoutManaged = ChipsetManager.circuitRegistry.getLayout("forestry.farms.managed");

        try {
            Field field = ReflectionHelper.findField(Class.forName("forestry.farming.gadgets.StructureLogicFarm"), "bricks");
            farmStructureBlocks = (HashSet<Block>)field.get(null);
        } catch(Throwable e) {
            Log.warning("Failed on getting Forestry's farm blocks, using defaults");
            farmStructureBlocks = new HashSet<Block>();
            farmStructureBlocks.add(Blocks.brick_block);
            farmStructureBlocks.add(Blocks.stonebrick);
            farmStructureBlocks.add(Blocks.sandstone);
            farmStructureBlocks.add(Blocks.nether_brick);
            farmStructureBlocks.add(Blocks.quartz_block);
        }

        try {
            Constructor c = Class.forName("forestry.farming.circuits.CircuitFarmLogic").getConstructor(String.class, Class.class);
            for(ItemStack stack : ((ItemPlasticElectronTube)plasticElectronTube).getSubItems()) {
                int meta = stack.getItemDamage();
                if(logics[meta] != null) {
                    ChipsetManager.solderManager.addRecipe(layoutManaged, new ItemStack(plasticElectronTube, 1, meta), (ICircuit)c.newInstance("plasticPlant" + meta, logics[meta]));
                }
            }
        } catch(Throwable e) {
            Log.error("Something happened when trying to register forestry farm logic");
            e.printStackTrace();
        }

        /*  } else {
              Log.info("Forestry was found, but the 'farmVegetables' farmable wasn't. Is this forestry plugin loaded?");
          }*/
    }

    @Override
    public void init(){
        PneumaticRegistry.getInstance().registerFuel(FluidRegistry.getFluid("biomass"), 500000);
        PneumaticRegistry.getInstance().registerFuel(FluidRegistry.getFluid("bioethanol"), 500000);

        for(ItemStack stack : ((ItemPlasticElectronTube)plasticElectronTube).getSubItems()) {
            RecipeManagers.fabricatorManager.addRecipe(null, FluidRegistry.getFluidStack("glass", 500), stack.copy(), new Object[]{" X ", "#X#", "XXX", '#', Items.redstone, 'X', new ItemStack(Itemss.plastic, 1, stack.getItemDamage())});
        }

    }

    @Override
    public void postInit(){

    }

    @Override
    public void clientSide(){}

    @Override
    public void clientInit(){}

    public static class FarmLogicFire extends FarmLogicPlasticCustomEarth{
        public FarmLogicFire(IFarmHousing housing) throws Throwable{
            super(housing);
        }

        @Override
        protected Block getBlock(){
            return Blockss.fireFlower;
        }

        @Override
        protected ItemStack getEarth(){
            return new ItemStack(Blocks.netherrack);
        }
    }

    public static class FarmLogicCreeper extends FarmLogicPlasticNormal{

        public FarmLogicCreeper(IFarmHousing housing) throws Throwable{
            super(housing);
        }

        @Override
        protected Block getBlock(){
            return Blockss.creeperPlant;
        }

    }

    public static class FarmLogicSlime extends FarmLogicPlasticNormal{

        public FarmLogicSlime(IFarmHousing housing) throws Throwable{
            super(housing);
        }

        @Override
        protected Block getBlock(){
            return Blockss.slimePlant;
        }

    }

    public static class FarmLogicRain extends FarmLogicPlasticNormal{

        public FarmLogicRain(IFarmHousing housing) throws Throwable{
            super(housing);
        }

        @Override
        protected Block getBlock(){
            return Blockss.rainPlant;
        }

    }

    public static class FarmLogicEnder extends FarmLogicPlasticCustomEarth{
        public FarmLogicEnder(IFarmHousing housing) throws Throwable{
            super(housing);
        }

        @Override
        protected Block getBlock(){
            return Blockss.enderPlant;
        }

        @Override
        protected ItemStack getEarth(){
            return new ItemStack(Blocks.end_stone);
        }
    }

    public static class FarmLogicLightning extends FarmLogicPlasticNormal{

        public FarmLogicLightning(IFarmHousing housing) throws Throwable{
            super(housing);
        }

        @Override
        protected Block getBlock(){
            return Blockss.lightningPlant;
        }

    }

    public static class FarmLogicBurst extends FarmLogicPlasticNormal{

        public FarmLogicBurst(IFarmHousing housing) throws Throwable{
            super(housing);
        }

        @Override
        protected Block getBlock(){
            return Blockss.burstPlant;
        }

    }

    public static class FarmLogicPotion extends FarmLogicPlasticNormal{

        public FarmLogicPotion(IFarmHousing housing) throws Throwable{
            super(housing);
        }

        @Override
        protected Block getBlock(){
            return Blockss.potionPlant;
        }

    }

    public static class FarmLogicRepulsion extends FarmLogicPlasticNormal{

        public FarmLogicRepulsion(IFarmHousing housing) throws Throwable{
            super(housing);
        }

        @Override
        protected Block getBlock(){
            return Blockss.repulsionPlant;
        }

    }

    public static class FarmLogicChopper extends FarmLogicPlasticNormal{

        public FarmLogicChopper(IFarmHousing housing) throws Throwable{
            super(housing);
        }

        @Override
        protected Block getBlock(){
            return Blockss.chopperPlant;
        }

    }

    public static class FarmLogicPropulsion extends FarmLogicPlasticNormal{

        public FarmLogicPropulsion(IFarmHousing housing) throws Throwable{
            super(housing);
        }

        @Override
        protected Block getBlock(){
            return Blockss.propulsionPlant;
        }

    }

    public static class FarmLogicFlying extends FarmLogicPlasticNormal{

        public FarmLogicFlying(IFarmHousing housing) throws Throwable{
            super(housing);
        }

        @Override
        protected Block getBlock(){
            return Blockss.flyingFlower;
        }

    }

}
