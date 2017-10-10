package me.desht.pneumaticcraft.common.tileentity;

import net.minecraftforge.fml.common.registry.GameRegistry;

public class TileEntityRegistrator {
    public static void init() {
        GameRegistry.registerTileEntity(TileEntityPressureTube.class, "pneumaticcraft:PressureTube");
        GameRegistry.registerTileEntity(TileEntityAirCompressor.class, "pneumaticcraft:AirCompressor");
        GameRegistry.registerTileEntity(TileEntityAdvancedAirCompressor.class, "pneumaticcraft:advancedAirCompressor");
        GameRegistry.registerTileEntity(TileEntityAirCannon.class, "pneumaticcraft:AirCannon");
        GameRegistry.registerTileEntity(TileEntityPressureChamberWall.class, "pneumaticcraft:PressureChamberWall");
        GameRegistry.registerTileEntity(TileEntityPressureChamberValve.class, "pneumaticcraft:PressureChamberValve");
        GameRegistry.registerTileEntity(TileEntityChargingStation.class, "pneumaticcraft:ChargingStation");
        GameRegistry.registerTileEntity(TileEntityElevatorBase.class, "pneumaticcraft:ElevatorBase");
        GameRegistry.registerTileEntity(TileEntityElevatorFrame.class, "pneumaticcraft:ElevatorFrame");
        GameRegistry.registerTileEntity(TileEntityPressureChamberInterface.class, "pneumaticcraft:PressureChamberInterface");
        GameRegistry.registerTileEntity(TileEntityVacuumPump.class, "pneumaticcraft:VacuumPump");
        GameRegistry.registerTileEntity(TileEntityPneumaticDoorBase.class, "pneumaticcraft:PneumaticDoorBase");
        GameRegistry.registerTileEntity(TileEntityPneumaticDoor.class, "pneumaticcraft:PneumaticDoor");
        GameRegistry.registerTileEntity(TileEntityAssemblyIOUnit.class, "pneumaticcraft:AssemblyIOUnit");
        GameRegistry.registerTileEntity(TileEntityAssemblyPlatform.class, "pneumaticcraft:AssemblyPlatform");
        GameRegistry.registerTileEntity(TileEntityAssemblyDrill.class, "pneumaticcraft:AssemblyDrill");
        GameRegistry.registerTileEntity(TileEntityAssemblyLaser.class, "pneumaticcraft:AssemblyLaser");
        GameRegistry.registerTileEntity(TileEntityAssemblyController.class, "pneumaticcraft:AssemblyController");
        GameRegistry.registerTileEntity(TileEntityUVLightBox.class, "pneumaticcraft:UVLightBox");
        GameRegistry.registerTileEntity(TileEntitySecurityStation.class, "pneumaticcraft:SecurityStation");
        GameRegistry.registerTileEntity(TileEntityUniversalSensor.class, "pneumaticcraft:UniversalSensor");
        GameRegistry.registerTileEntity(TileEntityUniversalActuator.class, "pneumaticcraft:universalActuator");
        GameRegistry.registerTileEntity(TileEntityAerialInterface.class, "pneumaticcraft:AerialInterface");
        GameRegistry.registerTileEntity(TileEntityElectrostaticCompressor.class, "pneumaticcraft:ElectrostaticCompressor");
        GameRegistry.registerTileEntity(TileEntityAphorismTile.class, "pneumaticcraft:AphorismTile");
        GameRegistry.registerTileEntity(TileEntityOmnidirectionalHopper.class, "pneumaticcraft:OmnidirectionalHopper");
        GameRegistry.registerTileEntity(TileEntityLiquidHopper.class, "pneumaticcraft:liquidHopper");
        GameRegistry.registerTileEntity(TileEntityElevatorCaller.class, "pneumaticcraft:ElevatorCaller");
        GameRegistry.registerTileEntity(TileEntityProgrammer.class, "pneumaticcraft:Programmer");
        GameRegistry.registerTileEntity(TileEntityCreativeCompressor.class, "pneumaticcraft:CreativeCompressor");
        GameRegistry.registerTileEntity(TileEntityPlasticMixer.class, "pneumaticcraft:plasticMixer");
        GameRegistry.registerTileEntity(TileEntityLiquidCompressor.class, "pneumaticcraft:liquidCompressor");
        GameRegistry.registerTileEntity(TileEntityAdvancedLiquidCompressor.class, "pneumaticcraft:advancedLiquidCompressor");
        GameRegistry.registerTileEntity(TileEntityDroneRedstoneEmitter.class, "pneumaticcraft:droneRedstoneEmitter");
        GameRegistry.registerTileEntity(TileEntityCompressedIronBlock.class, "pneumaticcraft:compressedIronBlock");
        GameRegistry.registerTileEntity(TileEntityHeatSink.class, "pneumaticcraft:heatSink");
        GameRegistry.registerTileEntity(TileEntityVortexTube.class, "pneumaticcraft:vortexTube");
        GameRegistry.registerTileEntity(TileEntityProgrammableController.class, "pneumaticcraft:programmableController");
        GameRegistry.registerTileEntity(TileEntityGasLift.class, "pneumaticcraft:gasLift");
        GameRegistry.registerTileEntity(TileEntityRefinery.class, "pneumaticcraft:refinery");
        GameRegistry.registerTileEntity(TileEntityThermopneumaticProcessingPlant.class, "pneumaticcraft:thermopneumaticProcessingPlant");
        GameRegistry.registerTileEntity(TileEntityKeroseneLamp.class, "pneumaticcraft:keroseneLamp");
        GameRegistry.registerTileEntity(TileEntitySentryTurret.class, "pneumaticcraft:sentryTurret");
        GameRegistry.registerTileEntity(TileEntityFluxCompressor.class, "pneumaticcraft:fluxCompressor");
        GameRegistry.registerTileEntity(TileEntityPneumaticDynamo.class, "pneumaticcraft:pneumaticDynamo");
    }

}
