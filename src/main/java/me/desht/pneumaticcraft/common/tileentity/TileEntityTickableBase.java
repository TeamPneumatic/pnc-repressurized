package me.desht.pneumaticcraft.common.tileentity;

import me.desht.pneumaticcraft.lib.ModIds;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.common.Optional;

@Optional.InterfaceList({@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = ModIds.COMPUTERCRAFT)})
public class TileEntityTickableBase extends TileEntityBase implements ITickable {

    public TileEntityTickableBase() {
        this(0);
    }

    public TileEntityTickableBase(int upgradeSize) {
        super(upgradeSize);
    }

    /* TODO IC2 dep @Optional.Method(modid = ModIds.INDUSTRIALCRAFT)
     protected int getIC2Upgrades(String ic2ItemKey, int[] upgradeSlots){
         ItemStack itemStack = IC2Items.getItem(ic2ItemKey);
         if(itemStack == null) return 0;
         int upgrades = 0;
         if(this instanceof IInventory) {// this always should be true.
             IInventory inv = (IInventory)this;
             for(int i : upgradeSlots) {
                 if(inv.getStackInSlot(i) != null && inv.getStackInSlot(i).getItem() == itemStack.getItem()) {
                     upgrades += inv.getStackInSlot(i).stackSize;
                 }
             }
         }
         return upgrades;
     }*/

    /*
     * COMPUTERCRAFT API 
     */

}
