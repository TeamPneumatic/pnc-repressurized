package me.desht.pneumaticcraft.common.config;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.IOException;

@Config(modid = Names.MOD_ID)
public class ConfigHandler {
    private static final ISubConfig[] subConfigs = new ISubConfig[] {
            new AmadronOfferSettings(),
            AmadronOfferStaticConfig.INSTANCE,
            AmadronOfferPeriodicConfig.INSTANCE,
            new ProgWidgetConfig(),
            HelmetWidgetDefaults.INSTANCE,
            ThirdPartyConfig.INSTANCE,
            MicromissileDefaults.INSTANCE,
            BlockHeatPropertiesConfig.INSTANCE
    };

    @Config.Name("general")
    @Config.LangKey("gui.config.category.general")
    @Config.Comment("General stuff that doesn't fit anywhere else")
    public static final General general = new General();

    @Config.Name("armor")
    @Config.LangKey("gui.config.category.pneumaticArmor")
    @Config.Comment("Settings affecting Pneumatic Armor operation (other than helmet HUD settings)")
    public static final PneumaticArmor pneumaticArmor = new PneumaticArmor();

    @Config.Name("machine_properties")
    @Config.LangKey("gui.config.category.machine")
    @Config.Comment("Settings affecting the operation of machines")
    public static final MachineProperties machineProperties = new MachineProperties();

    @Config.Name("advanced")
    @Config.LangKey("gui.config.category.advanced")
    @Config.Comment("Advanced settings (shouldn't need to touch under normal circumstances)")
    public static final Advanced advanced = new Advanced();

    @Config.Name("recipes")
    @Config.LangKey("gui.config.category.recipes")
    @Config.Comment("Enable/disable certain recipes (note: vanilla recipes can be also managed via CraftTweaker)")
    public static final Recipes recipes = new Recipes();

    @Config.Name("client")
    @Config.LangKey("gui.config.category.client")
    @Config.Comment("Settings affecting only the client")
    public static final ClientOptions client = new ClientOptions();

    @Config.Name("helmet")
    @Config.LangKey("gui.config.category.helmet")
    @Config.Comment("Pneumatic Helmet HUD settings (note: you can also set this up via helmet GUI)")
    public static final HelmetOptions helmetOptions = new HelmetOptions();

    @Config.Name("minigun")
    @Config.LangKey("gui.config.category.minigun")
    @Config.Comment("Minigun")
    public static final MinigunProperties minigun = new MinigunProperties();

    @Config.Name("micromissile")
    @Config.LangKey("gui.config.category.micromissile")
    @Config.Comment("Micromissiles")
    public static final MicromissileProperties microMissile = new MicromissileProperties();

    @Config.Name("integration")
    @Config.LangKey("gui.config.category.integration")
    @Config.Comment("Mod Integration")
    public static final IntegrationProperties integration = new IntegrationProperties();

    public static void sync() {
        ConfigManager.sync(Names.MOD_ID, Config.Type.INSTANCE);
    }

    public static void onPreInit(File configFile) {
        PneumaticCraftRepressurized.proxy.initConfig();

        for(ISubConfig subConfig : subConfigs) {
            File subFolder = new File(configFile.getAbsolutePath().substring(0, configFile.getAbsolutePath().length() - 4) + File.separator);
            if (subFolder.exists() || subFolder.mkdirs()) {
                File subFile = new File(subFolder, subConfig.getConfigFilename() + ".cfg");
                try {
                    subConfig.preInit(subFile);
                } catch(IOException e) {
                    PneumaticCraftRepressurized.logger.error("Config file " + subConfig.getConfigFilename() + " failed to create! Unexpected things can happen!");
                    e.printStackTrace();
                } catch (ClassCastException e) {
                    PneumaticCraftRepressurized.logger.error("Config file " + subConfig.getConfigFilename() + " appears to be invalid JSON! Unexpected things can happen!");
                    e.printStackTrace();
                }
            }
        }
    }

    public static void onPostInit() {
        for(ISubConfig subConfig : subConfigs) {
            try {
                subConfig.postInit();
            } catch(IOException e) {
                PneumaticCraftRepressurized.logger.error("Config file " + subConfig.getConfigFilename() + " failed to create! Unexpected things can happen!");
                e.printStackTrace();
            } catch (ClassCastException e) {
                PneumaticCraftRepressurized.logger.error("Config file " + subConfig.getConfigFilename() + " appears to be invalid JSON! Unexpected things can happen!");
                e.printStackTrace();
            }
        }
    }

    public static class General {
        @Config.Comment("Chance per chunk in percentage to generate an Oil Lake. Set to 0 for no spawns")
        @Config.RangeDouble(min = 0d, max = 100d)
        @Config.RequiresMcRestart
        public double oilGenerationChance = 15d;
        @Config.Comment("Loss percentage (on average) of Compressed Iron ingots/blocks when exposed to an explosion. Note: this can also be controlled via CraftTweaker.")
        @Config.RangeInt(min = 0, max = 100)
        @Config.RequiresMcRestart
        public int configCompressedIngotLossRate = 20;
        @Config.Comment("Enables the dungeon loot generation of the Nuke Virus and Stop! Worm (not useful in single-player worlds)")
        public boolean enableDungeonLoot = true;
        @Config.Comment("Enable Drone Suffocation Damage")
        public boolean enableDroneSuffocationDamage = true;
        @Config.Comment("Efficiency of fuel buckets as furnace fuel (default 0.05 means 1 bucket of LPG smelts 450 items in a vanilla furnace")
        public float fuelBucketEfficiencyMultiplier = 0.05f;
        @Config.Comment("Maximum number of blocks in the area defined in an Area Programming Puzzle Piece")
        public int maxProgrammingArea = 250000;
        @Config.Comment("Enable/disable explosion crafting (iron->compressed iron).  If you disable this, you'll need another way to get compressed iron initially.")
        public boolean explosionCrafting = true;
        @Config.Comment("Oil worldgen blacklist: add dimension IDs to this list if you don't want oil worldgen to happen there.")
        @Config.RequiresMcRestart
        public int[] oilWorldGenBlacklist = new int[] { 1, -1 };
        @Config.Comment("Fluids as hot or hotter than this temperature (Kelvin) will be auto-registered as Liquid Compressor fuels, the quality being dependent on fluid temperature.")
        @Config.RequiresMcRestart
        public int minimumFluidFuelTemperature = 373; // 100C


        // deprecated stuff
        @Config.Comment("DEPRECATED: use Minigun / baseDamage")
        public double configMinigunDamage = 4.0;
        @Config.Comment("DEPRECATED: use Minigun / potionProcChance")
        public int minigunPotionProcChance = 15;
    }

    public static class PneumaticArmor {
        @Config.Comment("Jetboots air usage in mL/tick (per Jet Boots Upgrade)")
        public int jetbootsAirUsage = PneumaticValues.PNEUMATIC_JET_BOOTS_USAGE;
        @Config.Comment("Armor base startup time in ticks (before Speed Upgrades)")
        public int armorStartupTime = 200;
    }

    public static class MachineProperties {
        @Config.Comment("Changing this value will alter the pressurized air usage of the Pneumatic Generator. The output, EU, will stay the same.")
        public int pneumaticGeneratorEfficiency = 40;
        @Config.Comment("Changing this value will alter the pressurized air production of the Electric Compressor. The input, EU, will stay the same")
        public int electricCompressorEfficiency = 40;
//        @Config.Comment("Changing this value will alter the pressurized air usage of the Pneumatic Engine. The output, MJ, will stay the same")
//        public int pneumaticEngineEfficiency = 40;
//        @Config.Comment("Changing this value will alter the pressurized air production of the Kinetic Compressor. The input, MJ, will stay the same")
//        public int kineticCompressorEfficiency = 40;
//        @Config.Comment("Changing this value will alter the hydraulic bar production of the Pneumatic Pump. The input, air, will stay the same")
//        public int pneumaticPumpEfficiency = 40;
        @Config.Comment("Changing this value will alter the pressurized air production of the Flux Compressor. The input, RF, will stay the same")
        public int fluxCompressorEfficiency = 40;
        @Config.Comment("Changing this value will alter the pressurized air usage of the Pneumatic Dynamo. The output, RF, will stay the same")
        public int pneumaticDynamoEfficiency = 40;
        @Config.Comment("The max height of an elevator per stacked Elevator Base.")
        @Config.RangeInt(min = 1, max = 256)
        public int elevatorBaseBlocksPerBase = 4;
        @Config.Comment("Can the Kerosene Lamp burn any kind of fuel?  If false, only Kerosene can be burnt")
        public boolean keroseneLampCanUseAnyFuel = true;
        @Config.Comment("Kerosene Lamp fuel efficiency: higher values mean fuel will last longer in the lamp")
        public float keroseneLampFuelEfficiency = 1.0f;
        @Config.Comment("Speed multiplier per speed upgrade: speed mult = speedUpgradeSpeedMultiplier ^ num_of_speed_upgrades")
        @Config.RangeDouble(min = 1.0, max = 2.0)
        public double speedUpgradeSpeedMultiplier = PneumaticValues.DEF_SPEED_UPGRADE_MULTIPLIER;
        @Config.Comment("Fuel usage / heat gen multiplier per speed upgrade: usage mult = speedUpgradeUsageMultiplier ^ num_of_speed_upgrades")
        @Config.RangeDouble(min = 1.0, max = 2.0)
        public double speedUpgradeUsageMultiplier = PneumaticValues.DEF_SPEED_UPGRADE_USAGE_MULTIPLIER;
        @Config.Comment("Base chance (1/x) per tick of a lightning strike on/around the Electrostatic Generator")
        public int electrostaticLightningChance = 100000;
        @Config.Comment("Can the Liquid Hopper absorb/dispense fluids into the world with a Dispenser Upgrade?")
        @Config.RequiresWorldRestart
        public boolean liquidHopperDispenser = true;
        @Config.Comment("Can the Omnidirectional Hopper dispense items into the world with a Dispenser Upgrade?")
        @Config.RequiresWorldRestart
        public boolean omniHopperDispenser = true;
        @Config.Comment("Aerial Interface backwards compat: allow pre-0.8.0 behaviour of getting player's armor inventory from top face, even with Dispenser Upgrade installed")
        public boolean aerialInterfaceArmorCompat = true;
        @Config.Comment("The ratio of liquid plastic to solid plastic sheets in the Plastic Mixer, in mB per sheet")
        @Config.RangeInt(min = 1)
        public int plasticMixerPlasticRatio = 1000;
    }

    public static class Advanced {
        @Config.Comment("When set to true, Drones will not execute any program. This is useful to set to true when due to a bug Drones are lagging your server or crashing it. Please report the bug if you encounter it.")
        public boolean stopDroneAI = false;
//        @Config.Comment("ONLY SET TO TRUE WHEN YOU KNOW WHAT YOU'RE DOING. When set to true, this will convert any Pressure Tube in the world that was a FMP to its block variant. Handy when you're about to remove FMP from the instance. This will remove any other parts from the block like covers. Exception are tube modules.")
//        public boolean convertMultipartsTBlock = false;
        @Config.Comment("When set to true, the Kerosene Lamp's fake air blocks won't be registered and therefore removed from the world. Useful if this causes trouble (it shouldn't though)")
        public boolean disableKeroseneLampFakeAirBlock = false;
        @Config.Comment("The amount by which any liquid tank's contents must change, as a proportion of the tank's total capacity, to trigger an update to clients. Larger values mean fewer updates but less granularity in client-side fluid rendering.")
        @Config.RangeDouble(min = 0.0001, max = 1)
        @Config.RequiresWorldRestart
        public double liquidTankUpdateThreshold = 0.01;
    }

    public static class Recipes {
        @Config.Comment("Electric Compressor for IC2")
        @Config.RequiresMcRestart
        public boolean enableElectricCompressorRecipe = true;
        @Config.Comment("Pneumatic Generator for IC2")
        @Config.RequiresMcRestart
        public boolean enablePneumaticGeneratorRecipe = true;
//        @Config.Comment("Pneumatic Pump")
//        public boolean enablePneumaticPumpRecipe = true;
        @Config.Comment("8 Block of Coal --> 1 Diamond (Pressure Chamber)")
        @Config.RequiresMcRestart
        public boolean enableCoalToDiamondsRecipe = true;
    }

    public static class ClientOptions {
        @Config.Comment("Enable Aphorism Tile Drama!  http://mc-drama.herokuapp.com/")
        public boolean aphorismDrama = true;
        @Config.Comment("When true, the Pneumatic Helmet will be a model. Warning: this model looks far too good to be in MC (currently ignored)")
        public boolean useHelmetModel = false;
        @Config.Comment("Defines the default difficulty of the Programmer shown to the user. 0 = easy, 1 = medium, 2 = advanced")
        @Config.RangeInt(min = 0, max = 2)
        public int programmerDifficulty = 0;
        @Config.Comment("Show tank fluids with the The One Probe.  Note that TOP also has support for showing tanks, which may or may not be enabled.")
        public boolean topShowsFluids = true;
        @Config.Comment("Tint Logistics configuration GUI backgrounds according to the colour of the logistics frame you are configuring")
        public boolean logisticsGUITint = true;
        @Config.Comment("Drones render their held item (the item in slot 0 of their inventory) ?")
        public boolean dronesRenderHeldItem = true;
        @Config.Comment("Use block lighting for semiblocks (logistics frames, heat frames...). May cause occasional lighting issues - semiblocks appearing unlit - disable this if that's a problem.")
        public boolean semiBlockLighting = true;
        @Config.Comment("Intensity of the FOV modification when using Pneumatic Leggings speed boost: 0.0 for no FOV modification, higher values zoom out more.  Note: non-zero values may cause FOV clashes with other mods.")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double leggingsFOVfactor = 0.0;
        @Config.Comment("Should GUI side tabs be shown with a beveled edge?  Setting to false uses a plain black edge, as in earlier versions of the mod")
        public boolean guiBevel = true;
        @Config.Comment("Remote Editor GUI: should GUI controls be snapped to 4x4 grid?")
        public boolean guiRemoteGridSnap;
        @Config.Comment("Always show the pressure durability bar for pressurizable items, even when full?")
        public boolean alwaysShowPressureDurabilityBar = true;
    }

    public static class HelmetOptions {
        @Config.Comment("Pneumatic helmet power readout X")
        public int powerX = -1;
        @Config.Comment("Pneumatic helmet power readout Y")
        public int powerY = 2;
        @Config.Comment("Pneumatic helmet power readout on left?")
        public boolean powerLeft = false;
        @Config.Comment("Pneumatic helmet message readout X")
        public int messageX = 2;
        @Config.Comment("Pneumatic helmet message readout Y")
        public int messageY = 2;
        @Config.Comment("Pneumatic helmet message readout on left?")
        public boolean messageLeft = false;
        @Config.Comment("Pneumatic helmet block tracker X")
        public int blockTrackerX = -1;
        @Config.Comment("Pneumatic helmet block tracker Y")
        public int blockTrackerY = 46;
        @Config.Comment("Pneumatic helmet block tracker on left?")
        public boolean blockTrackerLeft = true;
        @Config.Comment("Pneumatic helmet entity tracker X")
        public int entityTrackerX = -1;
        @Config.Comment("Pneumatic helmet entity tracker Y")
        public int entityTrackerY = 90;
        @Config.Comment("Pneumatic helmet entity tracker on left?")
        public boolean entityTrackerLeft = true;
        @Config.Comment("Pneumatic helmet item search X")
        public int itemSearchX = -1;
        @Config.Comment("Pneumatic helmet item search Y")
        public int itemSearchY = 17;
        @Config.Comment("Pneumatic helmet item search on left?")
        public boolean itemSearchLeft = true;
        @Config.Comment("Pneumatic helmet Path Enabled")
        public boolean pathEnabled = true;
        @Config.Comment("Pneumatic helmet Wire Path")
        public boolean wirePath = true;
        @Config.Comment("Pneumatic helmet X-Ray")
        public boolean xRayEnabled = false;
        @Config.Comment("Pneumatic helmet Path Update Rate")
        public int pathUpdateSetting = 1;
    }

    public static class MinigunProperties {
        @Config.Comment("Base bullet damage of the Sentry Gun, Handheld Minigun, and Drone Minigun, before ammo bonuses are considered")
        public float baseDamage = 4f;
        @Config.Comment("Base range of Minigun, before Range Upgrades are considered")
        @Config.RangeInt(min = 5, max = 100)
        public int baseRange = 50;
        @Config.Comment("Percentage chance per shot of potion-tipped ammo proc'ing the potion effect, before Dispenser Upgrades are considered")
        @Config.RangeInt(min = 1, max = 100)
        public int potionProcChance = 7;
        @Config.Comment("Incendiary ammo fire duration on target entities (seconds)")
        public int incendiaryAmmoFireDuration = 8;
        @Config.Comment("Incendiary ammo base percentage chance to ignite entities")
        @Config.RangeInt(min = 1, max = 100)
        public int incendiaryAmmoEntityIgniteChance = 100;
        @Config.Comment("Incendiary ammo base percentage chance to ignite blocks")
        @Config.RangeInt(min = 1, max = 100)
        public int incendiaryAmmoBlockIgniteChance = 20;
        @Config.Comment("Armor Piercing Ammo percentage chance to ignore target's armor")
        @Config.RangeInt(min = 1, max = 100)
        public int apAmmoIgnoreArmorChance = 100;
        @Config.Comment("Armor Piercing Ammo damage multiplier (relative to standard ammo)")
        public float apAmmoDamageMultiplier = 1.25f;
        @Config.Comment("Weighted Ammo damage multiplier (relative to standard ammo)")
        public float weightedAmmoDamageMultiplier = 2.5f;
        @Config.Comment("Weighted Ammo range multiplier (relative to standard ammo)")
        public float weightedAmmoRangeMultiplier = 0.2f;
        @Config.Comment("Weighted Ammo air usage multiplier (relative to standard ammo)")
        public float weightedAmmoAirUsageMultiplier = 8.0f;
        @Config.Comment("Minigun Explosive Ammo explosion power (ref: 2 = creeper, 4 = TNT")
        public float explosiveAmmoExplosionPower = 1.5f;
        @Config.Comment("Minigun Explosive Ammo damage multiplier (relative to standard ammo)")
        public float explosiveAmmoDamageMultiplier = 0.2f;
        @Config.Comment("Does Minigun Explosive Ammo damage terrain?")
        public boolean explosiveAmmoTerrainDamage = false;
        @Config.Comment("Standard Ammo cartridge size")
        @Config.RangeInt(min = 1, max = 30000)
        public int standardAmmoCartridgeSize = 1000;
        @Config.Comment("Armor Piercing Ammo cartridge size")
        @Config.RangeInt(min = 1, max = 30000)
        public int armorPiercingAmmoCartridgeSize = 250;
        @Config.Comment("Weighted Ammo cartridge size")
        @Config.RangeInt(min = 1, max = 30000)
        public int weightedAmmoCartridgeSize = 250;
        @Config.Comment("Incendiary Ammo cartridge size")
        @Config.RangeInt(min = 1, max = 30000)
        public int incendiaryAmmoCartridgeSize = 500;
        @Config.Comment("Explosive Ammo cartridge size")
        @Config.RangeInt(min = 1, max = 30000)
        public int explosiveAmmoCartridgeSize = 125;
        @Config.Comment("Freezing Ammo base percentage chance to form ice on entities which have been hit")
        @Config.RangeInt(min = 0, max = 100)
        public int freezingAmmoEntityIceChance = 20;
        @Config.Comment("Freezing Ammo base percentage chance to form ice or snow on blocks which have been hit")
        @Config.RangeInt(min = 0, max = 100)
        public int freezingAmmoBlockIceChance = 20;
        @Config.Comment("Freezing Ammo cartridge size")
        public int freezingAmmoCartridgeSize = 500;
        @Config.Comment("Explosive Ammo base percentage chance to cause an explosion")
        public int explosiveAmmoExplosionChance = 50;
        @Config.Comment("Damage done to entities within the fake 'ice' blocks cause by freezing ammo")
        public float freezingAmmoFakeIceDamage = 1f;

    }

    public static class MicromissileProperties {
        @Config.Comment("Number of micromissiles per pod")
        public int missilePodSize = 100;
        @Config.Comment("Do micromissile explosions cause terrain damage?")
        public boolean damageTerrain = false;
        @Config.Comment("Base explosion damage (modified by missile setup)")
        public float baseExplosionDamage = 1f;
        @Config.Comment("Base missile lifetime in ticks (modified by missile setup)")
        public int lifetime = 300;
        @Config.Comment("Cooldown for missile firing in ticks")
        public int launchCooldown = 15;
    }

    public static class IntegrationProperties {
        @Config.Comment("Immersive Engineering: External Heater RF/t. Set to 0 to disable External Heater integration entirely.")
        @Config.RangeInt(min = 0)
        public int ieExternalHeaterRFperTick = 100;
        @Config.Comment("Immersive Engineering: External Heater heat/RF.  The amount of PneumaticCraft heat added by 1 RF.")
        @Config.RangeDouble(min = 0.0)
        public double ieExternalHeaterHeatPerRF = 0.01;
        @Config.Comment("Mekanism thermal resistance multiplier. Larger values mean slower heat transfer.")
        @Config.RangeDouble(min = 1.0)
        public double mekThermalResistanceMult = 100.0;
        @Config.Comment("Mekanism heat conversion efficiency.  Smaller values mean Mekanism heat is worth less PneumaticCraft heat. Set to 0.0 to disable Mekanism heat integration entirely.")
        @Config.RangeDouble(min = 0.0)
        public double mekHeatEfficiency = 0.4;
        @Config.Comment("Tough As Nails temperature divider; smaller values make PneumaticCraft heat sources have a more pronounced effect on your temperature. Set to 0 to ignore PneumaticCraft heat sources.")
        public float tanHeatDivider = 10.0f;
        @Config.Comment("Interval in ticks with which to refresh heat information from PneumaticCraft heat sources to Tough As Nails. A larger interval is kinder to the server but will provide less precise temperature data to TAN.")
        @Config.RangeInt(min = 1, max = 100)
        public int tanRefreshInterval = 20;
    }

    public static void setProgrammerDifficulty(int difficulty) {
        client.programmerDifficulty = difficulty;
        sync();
    }

    public static int getProgrammerDifficulty() {
        return client.programmerDifficulty;
    }
    @Mod.EventBusSubscriber
    public static class ConfigSyncHandler
    {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
        {
            if (event.getModID().equals(Names.MOD_ID)) {
                ConfigManager.sync(Names.MOD_ID, Config.Type.INSTANCE);
                PneumaticCraftRepressurized.logger.info("Configuration has been saved.");
            }
        }

    }

}
