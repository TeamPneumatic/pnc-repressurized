package pneumaticCraft.common;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import pneumaticCraft.PneumaticCraft;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Names;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class Config{
    public static Configuration config;

    public static int villagerMechanicID;

    public static boolean[] configPlantFullGrownEffect = new boolean[16];
    public static double[] configPlantGenerationChance = new double[16];
    public static boolean includePlantsOnBonemeal, allowDirtBonemealing;

    public static int configCompressedIngotLossRate;
    public static int elevatorBaseBlocksPerBase;
    public static boolean rotateUseEnergy;
    public static boolean useHelmetModel;

    public static boolean enableUpdateChecker;
    public static boolean convertMultipartsToBlocks;

    public static int pneumaticGeneratorEfficiency;
    public static int electricCompressorEfficiency;
    public static int pneumaticEngineEfficiency;
    public static int kineticCompressorEfficiency;
    public static int pneumaticPumpEfficiency;
    public static int fluxCompressorEfficiency;
    public static int pneumaticDynamoEfficiency;
    public static boolean enableElectricCompressorRecipe;
    public static boolean enablePneumaticGeneratorRecipe;
    public static boolean enablePneumaticPumpRecipe;
    public static boolean enableCreeperPlantMaceratorRecipe;
    public static boolean enableHeliumPlantMaceratorRecipe;
    public static boolean enableFlyingFlowerExtractorRecipe;
    public static boolean enablePropulsionPlantExtractorRecipe;

    public static boolean enableCoalToDiamondsRecipe;

    public static boolean enableDroneSuffocationDamage;
    public static boolean enableCreeperDropExplosion;
    public static boolean enableSlimeSeedDrop, enableCreeperSeedDrop, enableSquidSeedDrop, enableEndermanSeedDrop;

    public static final String[] CATEGORIES = new String[]{Configuration.CATEGORY_GENERAL, "plant_full-grown_effects", "plant_generation_options", "machine_properties", "advanced", "recipe_enabling", "third_party_enabling"};
    public static List<String> NO_MC_RESTART_CATS = Arrays.asList(new String[]{"plant_full-grown_effects", "plant_generation_options", "machine_properties"});

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs){
        if(eventArgs.modID.equals(Names.MOD_ID)) {
            init(null);
        }
    }

    public static void init(File configFile){
        if(configFile != null) {
            File oldConfig = new File(configFile.getAbsolutePath().replace("PneumaticCraft", "Minemaarten_PneumaticCraft"));//TODO remove legacy.
            if(oldConfig.exists()) configFile = oldConfig;

            config = new Configuration(configFile);
            config.load(); // get the actual data from the file.
        }

        config.addCustomCategoryComment("plant_full-grown_effects", "When true, the plant is allowed to execute its full-grown effect. (When false bonemeal still works though)");
        for(int i = 0; i < 16; i++) {
            configPlantFullGrownEffect[i] = config.get("plant_full-grown_effects", ItemPlasticPlants.PLANT_NAMES[i], true).getBoolean(false);
        }
        config.addCustomCategoryComment("plant_generation_options", "The percentage chance of plant groups to spawn per chunk. 0 for no spawns, 100 for a group for every chunk");
        for(int i = 0; i < 16; i++) {
            if(!ItemPlasticPlants.NEEDS_GENERATION[i]) continue;
            double defaultValue = i == ItemPlasticPlants.HELIUM_PLANT_DAMAGE || i == ItemPlasticPlants.FIRE_FLOWER_DAMAGE ? 15.0 : 2.0;
            configPlantGenerationChance[i] = config.get("plant_generation_options", ItemPlasticPlants.PLANT_NAMES[i], defaultValue).getDouble() / 100;
        }

        Property property = config.get(Configuration.CATEGORY_GENERAL, "Compressed Iron Loss Percentage", 20);
        property.comment = "Loss percentage (on average) of Compressed Iron ingots/blocks when exposed to an explosion.";
        configCompressedIngotLossRate = property.getInt();

        useHelmetModel = config.getBoolean("Use Pneumatic Helmet model", Configuration.CATEGORY_GENERAL, false, "When true, the Pneumatic Helmet will be a model. Warning: this model looks far too good to be in MC");

        property = config.get(Configuration.CATEGORY_GENERAL, "Villager Mechanic ID", 125);
        property.comment = "Villager ID used for the Mechanic Villager. Change when ID collides with an other mod which adds villagers.";
        villagerMechanicID = property.getInt();

        property = config.get("Machine_Properties", "Pneumatic Generator (PneumaticCraft --> IC2) efficiency", 40);
        property.comment = "Changing this value will alter the pressurized air usage of the Pneumatic Generator. The output, EU, will stay the same.";
        pneumaticGeneratorEfficiency = property.getInt();

        property = config.get("machine_properties", "Electric Compressor (IC2 --> PneumaticCraft) efficiency", 40);
        property.comment = "Changing this value will alter the pressurized air production of the Electric Compressor. The input, EU, will stay the same.";
        electricCompressorEfficiency = property.getInt();

        property = config.get("machine_properties", "Pneumatic Engine (PneumaticCraft --> Buildcraft) efficiency", 40);
        property.comment = "Changing this value will alter the pressurized air usage of the Pneumatic Engine. The output, MJ, will stay the same.";
        pneumaticEngineEfficiency = property.getInt();

        property = config.get("machine_properties", "Kinetic Compressor (Buildcraft --> PneumaticCraft) efficiency", 40);
        property.comment = "Changing this value will alter the pressurized air production of the Kinetic Compressor. The input, MJ, will stay the same.";
        kineticCompressorEfficiency = property.getInt();

        property = config.get("machine_properties", "Pneumatic Dynamo (PneumaticCraft --> RF) efficiency", 40);
        property.comment = "Changing this value will alter the pressurized air usage of the Pneumatic Dynamo. The output, RF, will stay the same.";
        pneumaticDynamoEfficiency = property.getInt();

        property = config.get("machine_properties", "Flux Compressor (RF --> PneumaticCraft) efficiency", 40);
        property.comment = "Changing this value will alter the pressurized air production of the Flux Compressor. The input, RF, will stay the same.";
        fluxCompressorEfficiency = property.getInt();

        property = config.get("machine_properties", "Pneumatic Pump (PneumaticCraft --> Hydraulicraft) efficiency", 40);
        property.comment = "Changing this value will alter the hydraulic bar production of the Pneumatic Pump. The input, air, will stay the same.";
        pneumaticPumpEfficiency = property.getInt();

        elevatorBaseBlocksPerBase = config.getInt("Height per Elevator Base", "machine_properties", 4, 1, 256, "The max height of an elevator per stacked Elevator Base.");

        property = config.get(Configuration.CATEGORY_GENERAL, "Block rotate use energy", true);
        property.comment = "When set to false rotating PneumaticCraft blocks doesn't use any energy. This means that the Pneumatic Wrench doesn't use air and that all blocks can be rotated with a Buildcraft Wrench.";
        rotateUseEnergy = property.getBoolean(true);

        property = config.get("advanced", "Convert Multiparts to Blocks", false);
        property.comment = "ONLY SET TO TRUE WHEN YOU KNOW WHAT YOU'RE DOING. When set to true, this will convert any Pressure Tube in the world that was a FMP to its block variant. Handy when you're about to remove FMP from the instance. This will remove any other parts from the block like covers. Exception are tube modules.";
        convertMultipartsToBlocks = property.getBoolean(true);

        enableUpdateChecker = config.get(Configuration.CATEGORY_GENERAL, "Enable Update Checker", true).getBoolean(true);

        enableDroneSuffocationDamage = config.get(Configuration.CATEGORY_GENERAL, "Enable Drone Suffocation Damage", true).getBoolean(true);
        enableCreeperDropExplosion = config.getBoolean("Enable Creeper Explosions on seed drop", Configuration.CATEGORY_GENERAL, true, "When true, Creepers when dropping a Creeper Plant Seed will create a tiny explosion.");
        enableCreeperSeedDrop = config.get(Configuration.CATEGORY_GENERAL, "Enable Creeper Seed Drops", true).getBoolean(true);
        enableSlimeSeedDrop = config.get(Configuration.CATEGORY_GENERAL, "Enable Slime Seed Drops", true).getBoolean(true);
        enableEndermanSeedDrop = config.get(Configuration.CATEGORY_GENERAL, "Enable Enderman Seed Drops", true).getBoolean(true);
        enableSquidSeedDrop = config.get(Configuration.CATEGORY_GENERAL, "Enable Squid Seed Drops", true).getBoolean(true);
        includePlantsOnBonemeal = config.get(Configuration.CATEGORY_GENERAL, "Include Plastic Plants on bonemealing", true).getBoolean(true);
        allowDirtBonemealing = config.get(Configuration.CATEGORY_GENERAL, "Allow dirt to be bonemealed for plastic plants", true).getBoolean(true);

        enableCoalToDiamondsRecipe = config.get("recipe_enabling", "8 Block of Coal --> 1 Diamond (Pressure Chamber)", true).getBoolean(true);
        enableElectricCompressorRecipe = config.get("recipe_enabling", "Electric Compressor", true).getBoolean(true);
        enablePneumaticGeneratorRecipe = config.get("recipe_enabling", "Pneumatic Generator", true).getBoolean(true);
        enablePneumaticPumpRecipe = config.get("recipe_enabling", "Pneumatic Pump", true).getBoolean(true);
        enableCreeperPlantMaceratorRecipe = config.get("recipe_enabling", "Creeper Plant Seeds --> Gunpowder (IC2 Macerator)", true).getBoolean(true);
        enableHeliumPlantMaceratorRecipe = config.get("recipe_enabling", "Helium Plant Seeds --> Glowstone (IC2 Macerator)", true).getBoolean(true);
        enableFlyingFlowerExtractorRecipe = config.get("recipe_enabling", "Flying Flower Seeds --> Feather (IC2 Extractor)", true).getBoolean(true);
        enablePropulsionPlantExtractorRecipe = config.get("recipe_enabling", "Propulsion Plant Seeds --> Sugar (IC2 Extractor)", true).getBoolean(true);

        PneumaticCraft.proxy.initConfig(config);

        config.save();// save the configuration file
    }

}
