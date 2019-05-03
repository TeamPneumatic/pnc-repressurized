package me.desht.pneumaticcraft.common.tileentity;

import net.minecraftforge.fml.common.registry.GameRegistry;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class TileEntityRegistrator {
    public static void init() {
        GameRegistry.registerTileEntity(TileEntityPressureTube.class, RL("PressureTube"));
        GameRegistry.registerTileEntity(TileEntityAdvancedPressureTube.class, RL("advancedPressureTube"));
        GameRegistry.registerTileEntity(TileEntityAirCompressor.class, RL("AirCompressor"));
        GameRegistry.registerTileEntity(TileEntityAdvancedAirCompressor.class, RL("advancedAirCompressor"));
        GameRegistry.registerTileEntity(TileEntityAirCannon.class, RL("AirCannon"));
        GameRegistry.registerTileEntity(TileEntityPressureChamberWall.class, RL("PressureChamberWall"));
        GameRegistry.registerTileEntity(TileEntityPressureChamberValve.class, RL("PressureChamberValve"));
        GameRegistry.registerTileEntity(TileEntityChargingStation.class, RL("ChargingStation"));
        GameRegistry.registerTileEntity(TileEntityElevatorBase.class, RL("ElevatorBase"));
        GameRegistry.registerTileEntity(TileEntityElevatorFrame.class, RL("ElevatorFrame"));
        GameRegistry.registerTileEntity(TileEntityPressureChamberInterface.class, RL("PressureChamberInterface"));
        GameRegistry.registerTileEntity(TileEntityVacuumPump.class, RL("VacuumPump"));
        GameRegistry.registerTileEntity(TileEntityPneumaticDoorBase.class, RL("PneumaticDoorBase"));
        GameRegistry.registerTileEntity(TileEntityPneumaticDoor.class, RL("PneumaticDoor"));
        GameRegistry.registerTileEntity(TileEntityAssemblyIOUnit.class, RL("AssemblyIOUnit"));
        GameRegistry.registerTileEntity(TileEntityAssemblyPlatform.class, RL("AssemblyPlatform"));
        GameRegistry.registerTileEntity(TileEntityAssemblyDrill.class, RL("AssemblyDrill"));
        GameRegistry.registerTileEntity(TileEntityAssemblyLaser.class, RL("AssemblyLaser"));
        GameRegistry.registerTileEntity(TileEntityAssemblyController.class, RL("AssemblyController"));
        GameRegistry.registerTileEntity(TileEntityUVLightBox.class, RL("UVLightBox"));
        GameRegistry.registerTileEntity(TileEntitySecurityStation.class, RL("SecurityStation"));
        GameRegistry.registerTileEntity(TileEntityUniversalSensor.class, RL("UniversalSensor"));
        GameRegistry.registerTileEntity(TileEntityUniversalActuator.class, RL("universalActuator"));
        GameRegistry.registerTileEntity(TileEntityAerialInterface.class, RL("AerialInterface"));
        GameRegistry.registerTileEntity(TileEntityElectrostaticCompressor.class, RL("ElectrostaticCompressor"));
        GameRegistry.registerTileEntity(TileEntityAphorismTile.class, RL("AphorismTile"));
        GameRegistry.registerTileEntity(TileEntityOmnidirectionalHopper.class, RL("OmnidirectionalHopper"));
        GameRegistry.registerTileEntity(TileEntityLiquidHopper.class, RL("liquidHopper"));
        GameRegistry.registerTileEntity(TileEntityElevatorCaller.class, RL("ElevatorCaller"));
        GameRegistry.registerTileEntity(TileEntityProgrammer.class, RL("Programmer"));
        GameRegistry.registerTileEntity(TileEntityCreativeCompressor.class, RL("CreativeCompressor"));
        GameRegistry.registerTileEntity(TileEntityPlasticMixer.class, RL("plasticMixer"));
        GameRegistry.registerTileEntity(TileEntityLiquidCompressor.class, RL("liquidCompressor"));
        GameRegistry.registerTileEntity(TileEntityAdvancedLiquidCompressor.class, RL("advancedLiquidCompressor"));
        GameRegistry.registerTileEntity(TileEntityDroneRedstoneEmitter.class, RL("droneRedstoneEmitter"));
        GameRegistry.registerTileEntity(TileEntityCompressedIronBlock.class, RL("compressedIronBlock"));
        GameRegistry.registerTileEntity(TileEntityHeatSink.class, RL("heatSink"));
        GameRegistry.registerTileEntity(TileEntityVortexTube.class, RL("vortexTube"));
        GameRegistry.registerTileEntity(TileEntityProgrammableController.class, RL("programmableController"));
        GameRegistry.registerTileEntity(TileEntityGasLift.class, RL("gasLift"));
        GameRegistry.registerTileEntity(TileEntityRefinery.class, RL("refinery"));
        GameRegistry.registerTileEntity(TileEntityThermopneumaticProcessingPlant.class, RL("thermopneumaticProcessingPlant"));
        GameRegistry.registerTileEntity(TileEntityKeroseneLamp.class, RL("keroseneLamp"));
        GameRegistry.registerTileEntity(TileEntitySentryTurret.class, RL("sentryTurret"));
        GameRegistry.registerTileEntity(TileEntityFluxCompressor.class, RL("fluxCompressor"));
        GameRegistry.registerTileEntity(TileEntityPneumaticDynamo.class, RL("pneumaticDynamo"));
        GameRegistry.registerTileEntity(TileEntityThermalCompressor.class, RL("thermalCompressor"));
    }

}
