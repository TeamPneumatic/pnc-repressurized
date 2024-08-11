package me.desht.pneumaticcraft.common.pneumatic_armor;

import me.desht.pneumaticcraft.common.pneumatic_armor.handlers.*;

public class CommonUpgradeHandlers {
    public static CoreComponentsHandler coreComponentsHandler;
    public static BlockTrackerHandler blockTrackerHandler;
    public static EntityTrackerHandler entityTrackerHandler;
    public static SearchHandler searchHandler;
    public static CoordTrackerHandler coordTrackerHandler;
    public static DroneDebugHandler droneDebugHandler;
    public static NightVisionHandler nightVisionHandler;
    public static ScubaHandler scubaHandler;
    public static HackHandler hackHandler;
    public static ElytraHandler elytraHandler;
    public static EnderVisorHandler enderVisorHandler;
    public static MagnetHandler magnetHandler;
    public static ChargingHandler chargingHandler;
    public static ChestplateLauncherHandler chestplateLauncherHandler;
    public static AirConHandler airConHandler;
    public static ReachDistanceHandler reachDistanceHandler;
    public static RunSpeedHandler runSpeedHandler;
    public static JumpBoostHandler jumpBoostHandler;
    public static JetBootsHandler jetBootsHandler;
    public static StepAssistHandler stepAssistHandler;
    public static KickHandler kickHandler;
    public static StompHandler stompHandler;
    public static FallProtectionHandler fallProtectionHandler;

    public static void init() {
        ArmorUpgradeRegistry r = ArmorUpgradeRegistry.getInstance();

        coreComponentsHandler = r.registerUpgradeHandler(new CoreComponentsHandler());
        blockTrackerHandler = r.registerUpgradeHandler(new BlockTrackerHandler());
        entityTrackerHandler = r.registerUpgradeHandler(new EntityTrackerHandler());
        searchHandler = r.registerUpgradeHandler(new SearchHandler());
        coordTrackerHandler = r.registerUpgradeHandler(new CoordTrackerHandler());
        droneDebugHandler = r.registerUpgradeHandler(new DroneDebugHandler());
        nightVisionHandler = r.registerUpgradeHandler(new NightVisionHandler());
        scubaHandler = r.registerUpgradeHandler(new ScubaHandler());
        hackHandler = r.registerUpgradeHandler(new HackHandler());
        enderVisorHandler = r.registerUpgradeHandler(new EnderVisorHandler());

        magnetHandler = r.registerUpgradeHandler(new MagnetHandler());
        chargingHandler = r.registerUpgradeHandler(new ChargingHandler());
        chestplateLauncherHandler = r.registerUpgradeHandler(new ChestplateLauncherHandler());
//        airConHandler = r.registerUpgradeHandler(new AirConHandler());
        reachDistanceHandler = r.registerUpgradeHandler(new ReachDistanceHandler());
        elytraHandler = r.registerUpgradeHandler(new ElytraHandler());

        runSpeedHandler = r.registerUpgradeHandler(new RunSpeedHandler());
        jumpBoostHandler = r.registerUpgradeHandler(new JumpBoostHandler());

        jetBootsHandler = r.registerUpgradeHandler(new JetBootsHandler());
        stepAssistHandler = r.registerUpgradeHandler(new StepAssistHandler());
        kickHandler = r.registerUpgradeHandler(new KickHandler());
        stompHandler = r.registerUpgradeHandler(new StompHandler());
        fallProtectionHandler = r.registerUpgradeHandler(new FallProtectionHandler());
    }
}
