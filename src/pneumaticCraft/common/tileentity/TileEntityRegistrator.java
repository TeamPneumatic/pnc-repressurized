package pneumaticCraft.common.tileentity;

import cpw.mods.fml.common.registry.GameRegistry;

public class TileEntityRegistrator{
    public static void init(){
        GameRegistry.registerTileEntity(TileEntityPressureTube.class, "TileEntityPressureTube");
        GameRegistry.registerTileEntity(TileEntityAirCompressor.class, "TileEntityAirCompressor");
        GameRegistry.registerTileEntity(TileEntityAdvancedAirCompressor.class, "PneumaticCraft_advancedAirCompressor");
        GameRegistry.registerTileEntity(TileEntityAirCannon.class, "TileEntityAirCannon");
        GameRegistry.registerTileEntity(TileEntityPressureChamberWall.class, "TileEntityPressureChamberWall");
        GameRegistry.registerTileEntity(TileEntityPressureChamberValve.class, "TileEntityPressureChamberValve");
        GameRegistry.registerTileEntity(TileEntityChargingStation.class, "TileEntityChargingStation");
        GameRegistry.registerTileEntity(TileEntityElevatorBase.class, "TileEntityElevatorBase");
        GameRegistry.registerTileEntity(TileEntityElevatorFrame.class, "TileEntityElevatorFrame");
        GameRegistry.registerTileEntity(TileEntityPressureChamberInterface.class, "TileEntityPressureChamberInterface");
        GameRegistry.registerTileEntity(TileEntityVacuumPump.class, "TileEntityVacuumPump");
        GameRegistry.registerTileEntity(TileEntityPneumaticDoorBase.class, "TileEntityPneumaticDoorBase");
        GameRegistry.registerTileEntity(TileEntityPneumaticDoor.class, "TileEntityPneumaticDoor");
        GameRegistry.registerTileEntity(TileEntityAssemblyIOUnit.class, "TileEntityAssemblyIOUnit");
        GameRegistry.registerTileEntity(TileEntityAssemblyPlatform.class, "TileEntityAssemblyPlatform");
        GameRegistry.registerTileEntity(TileEntityAssemblyDrill.class, "TileEntityAssemblyDrill");
        GameRegistry.registerTileEntity(TileEntityAssemblyLaser.class, "TileEntityAssemblyLaser");
        GameRegistry.registerTileEntity(TileEntityAssemblyController.class, "TileEntityAssemblyController");
        GameRegistry.registerTileEntity(TileEntityUVLightBox.class, "TileEntityUVLightBox");
        GameRegistry.registerTileEntity(TileEntitySecurityStation.class, "TileEntitySecurityStation");
        GameRegistry.registerTileEntity(TileEntityUniversalSensor.class, "TileEntityUniversalSensor");
        GameRegistry.registerTileEntity(TileEntityUniversalActuator.class, "PneumaticCraft_universalSensor");
        GameRegistry.registerTileEntity(TileEntityAerialInterface.class, "TileEntityAerialInterface");
        GameRegistry.registerTileEntity(TileEntityElectrostaticCompressor.class, "TileEntityElectrostaticCompressor");
        GameRegistry.registerTileEntity(TileEntityAphorismTile.class, "TileEntityAphorismTile");
        GameRegistry.registerTileEntity(TileEntityOmnidirectionalHopper.class, "TileEntityOmnidirectionalHopper");
        GameRegistry.registerTileEntity(TileEntityLiquidHopper.class, "PneumaticCraft_liquidHopper");
        GameRegistry.registerTileEntity(TileEntityElevatorCaller.class, "TileEntityElevatorCaller");
        GameRegistry.registerTileEntity(TileEntityProgrammer.class, "TileEntityProgrammer");
        GameRegistry.registerTileEntity(TileEntityCreativeCompressor.class, "TileEntityCreativeCompressor");
        GameRegistry.registerTileEntity(TileEntityPlasticMixer.class, "PneumaticCraft_plasticMixer");
        GameRegistry.registerTileEntity(TileEntityLiquidCompressor.class, "PneumaticCraft_liquidCompressor");
        GameRegistry.registerTileEntity(TileEntityAdvancedLiquidCompressor.class, "PneumaticCraft_advancedLiquidCompressor");
        GameRegistry.registerTileEntity(TileEntityDroneRedstoneEmitter.class, "PneumaticCraft_droneRedstoneEmitter");
        GameRegistry.registerTileEntity(TileEntityCompressedIronBlock.class, "PneumaticCraft_compressedIronBlock");
        GameRegistry.registerTileEntity(TileEntityHeatSink.class, "PneumaticCraft_heatSink");
        GameRegistry.registerTileEntity(TileEntityVortexTube.class, "PneumaticCraft_vortexTube");
        GameRegistry.registerTileEntity(TileEntityProgrammableController.class, "PneumaticCraft_programmableController");
        GameRegistry.registerTileEntity(TileEntityGasLift.class, "PneumaticCraft_gasLift");
        GameRegistry.registerTileEntity(TileEntityRefinery.class, "PneumaticCraft_refinery");
        GameRegistry.registerTileEntity(TileEntityThermopneumaticProcessingPlant.class, "PneumaticCraft_thermopneumaticProcessingPlant");
        GameRegistry.registerTileEntity(TileEntityKeroseneLamp.class, "PneumaticCraft_keroseneLamp");
        GameRegistry.registerTileEntity(TileEntitySentryTurret.class, "PneumaticCraft_sentryTurret");
    }

}
