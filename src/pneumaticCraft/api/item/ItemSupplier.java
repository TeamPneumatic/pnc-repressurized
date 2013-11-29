package pneumaticCraft.api.item;

import net.minecraft.item.Item;

public class ItemSupplier{
    private static Class itemClass;

    public static Item getItem(String itemName){
        try {
            if(itemClass == null) itemClass = Class.forName("pneumaticCraft.common.item.Items");
            return (Item)itemClass.getField(itemName).get(null);
        } catch(Exception e) {
            System.err.println("[PneumaticCraft API] Block supply failed for block: " + itemName);
            return null;
        }
    }

    /*
        The following is a list of all the item names that can be passed as argument in getItem(String) to get a PneumaticCraft item.
     
        GPSTool                   Currently tracked coordinated is stored in NBT, with 'x', 'y', 'z' being the tag names for the x,y and z positions respectively.
        machineUpgrade            damage value = upgrade type.
        ingotIronCompressed
        pressureGauge
        stoneBase
        cannonBarrel
        turbineBlade
        plasticPlant              damage value = plant type. Mapped the same as Vanilla dye in terms of color.
        plastic                   damage value = plastic type. Mapped the same as Vanilla dye in terms of color.
        airCanister               implements IPressurizable
        vortexCannon              implements IPressurizable
        pneumaticCilinder
        pneumaticHelmet           implements IPressurizable
        manometer                 implements IPressurizable
        turbineRotor
        assemblyProgram           damage value = program type.
        emptyPCB
        unassembledPCB
        PCBBlueprint
        bucketEtchingAcid
        transistor
        capacitor
        printedCircuitBoard
        failedPCB
        networkComponent          damage value = network component type.
        stopWorm
        nukeVirus
        compressedIronGear
        pneumaticWrench           implements IPressurizable

     
     
     */
}
