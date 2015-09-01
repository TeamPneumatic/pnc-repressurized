package pneumaticCraft.common.thirdparty.ic2;

import ic2.api.item.IC2Items;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.Recipes;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.client.model.BaseModel;
import pneumaticCraft.client.model.ModelThirdPartyCompressor;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.config.Config;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.thirdparty.IThirdParty;
import pneumaticCraft.proxy.ClientProxy;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.GameRegistry;

public class IC2 implements IThirdParty, IGuiHandler{

    public static Block electricCompressor;
    public static Block pneumaticGenerator;

    @Override
    public void preInit(){
        pneumaticGenerator = new BlockPneumaticGenerator(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("pneumaticGenerator");
        electricCompressor = new BlockElectricCompressor(Material.iron).setHardness(3.0F).setResistance(10.0F).setBlockName("electricCompressor");
        Blockss.registerBlock(pneumaticGenerator);
        Blockss.registerBlock(electricCompressor);
        GameRegistry.registerTileEntity(TileEntityPneumaticGenerator.class, "TileEntityPneumaticGenerator");
        GameRegistry.registerTileEntity(TileEntityElectricCompressor.class, "TileEntityElectricCompressor");
        PneumaticRegistry.getInstance().registerBlockTrackEntry(new BlockTrackEntryIC2());
    }

    @Override
    public void init(){
        ItemStack advancedCircuit = IC2Items.getItem("advancedCircuit");
        ItemStack glassFibreCable = IC2Items.getItem("glassFiberCableItem");
        ItemStack advancedAlloy = IC2Items.getItem("advancedAlloy");
        ItemStack generator = IC2Items.getItem("generator");

        if(Config.enablePneumaticGeneratorRecipe) GameRegistry.addRecipe(new ItemStack(pneumaticGenerator), "pca", "trg", "pca", 'p', Itemss.printedCircuitBoard, 'c', advancedCircuit, 'a', advancedAlloy, 't', new ItemStack(Blockss.advancedPressureTube, 1, 0), 'r', Itemss.turbineRotor, 'g', glassFibreCable);
        if(Config.enableElectricCompressorRecipe) GameRegistry.addRecipe(new ItemStack(electricCompressor), "acp", "frt", "agp", 'p', Itemss.printedCircuitBoard, 'c', advancedCircuit, 'a', advancedAlloy, 't', new ItemStack(Blockss.advancedPressureTube, 1, 0), 'r', Itemss.turbineRotor, 'f', glassFibreCable, 'g', generator);
        try {
            if(Class.forName("ic2.api.recipe.Recipes") != null && Recipes.class.getField("macerator") != null && IMachineRecipeManager.class.getMethod("addRecipe", IRecipeInput.class, NBTTagCompound.class, ItemStack[].class) != null) {
                if(Config.enableCreeperPlantMaceratorRecipe) {
                    Recipes.macerator.addRecipe(new IC2RecipeInput(new ItemStack(Itemss.plasticPlant, 1, ItemPlasticPlants.CREEPER_PLANT_DAMAGE)), null, new ItemStack(Items.gunpowder));
                    Recipes.macerator.addRecipe(new IC2RecipeInput(new ItemStack(Itemss.plasticPlant, 1, ItemPlasticPlants.CREEPER_PLANT_DAMAGE + 16)), null, new ItemStack(Items.gunpowder));
                }
                if(Config.enableHeliumPlantMaceratorRecipe) {
                    Recipes.macerator.addRecipe(new IC2RecipeInput(new ItemStack(Itemss.plasticPlant, 1, ItemPlasticPlants.HELIUM_PLANT_DAMAGE)), null, new ItemStack(Items.glowstone_dust));
                    Recipes.macerator.addRecipe(new IC2RecipeInput(new ItemStack(Itemss.plasticPlant, 1, ItemPlasticPlants.HELIUM_PLANT_DAMAGE + 16)), null, new ItemStack(Items.glowstone_dust));
                }
                if(Config.enableFlyingFlowerExtractorRecipe) {
                    Recipes.extractor.addRecipe(new IC2RecipeInput(new ItemStack(Itemss.plasticPlant, 1, ItemPlasticPlants.FLYING_FLOWER_DAMAGE)), null, new ItemStack(Items.feather));
                    Recipes.extractor.addRecipe(new IC2RecipeInput(new ItemStack(Itemss.plasticPlant, 1, ItemPlasticPlants.FLYING_FLOWER_DAMAGE + 16)), null, new ItemStack(Items.feather));
                }
                if(Config.enablePropulsionPlantExtractorRecipe) {
                    Recipes.extractor.addRecipe(new IC2RecipeInput(new ItemStack(Itemss.plasticPlant, 1, ItemPlasticPlants.PROPULSION_PLANT_DAMAGE)), null, new ItemStack(Items.sugar, 2, 1));
                    Recipes.extractor.addRecipe(new IC2RecipeInput(new ItemStack(Itemss.plasticPlant, 1, ItemPlasticPlants.PROPULSION_PLANT_DAMAGE + 16)), null, new ItemStack(Items.sugar, 2, 1));
                }
            }
        } catch(Exception e) {
            System.err.println("[PneumaticCraft] Failed to load IC2's macerator, extractor and compressor recipes!");
            e.printStackTrace();
        }
    }

    @Override
    public void postInit(){}

    @Override
    public void clientSide(){
        ClientProxy.registerBaseModelRenderer(electricCompressor, TileEntityElectricCompressor.class, new ModelThirdPartyCompressor(ModelThirdPartyCompressor.Type.EU));
        ClientProxy.registerBaseModelRenderer(pneumaticGenerator, TileEntityPneumaticGenerator.class, new BaseModel("electricCompressor.obj"));
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
        switch(EnumGuiId.values()[ID]){
            case PNEUMATIC_GENERATOR:
                return new ContainerPneumaticGenerator(player.inventory, (TileEntityPneumaticGenerator)world.getTileEntity(x, y, z));
            case ELECTRIC_COMPRESSOR:
                return new ContainerElectricCompressor(player.inventory, (TileEntityElectricCompressor)world.getTileEntity(x, y, z));
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
        switch(EnumGuiId.values()[ID]){
            case PNEUMATIC_GENERATOR:
                return new GuiPneumaticGenerator(player.inventory, (TileEntityPneumaticGenerator)world.getTileEntity(x, y, z));
            case ELECTRIC_COMPRESSOR:
                return new GuiElectricCompressor(player.inventory, (TileEntityElectricCompressor)world.getTileEntity(x, y, z));
        }
        return null;
    }

    @Override
    public void clientInit(){}

}
