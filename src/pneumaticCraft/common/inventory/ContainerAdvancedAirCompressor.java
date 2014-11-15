package pneumaticCraft.common.inventory;

import net.minecraft.entity.player.InventoryPlayer;
import pneumaticCraft.common.tileentity.TileEntityAirCompressor;

public class ContainerAdvancedAirCompressor extends ContainerAirCompressor{

    public ContainerAdvancedAirCompressor(InventoryPlayer inventoryPlayer, TileEntityAirCompressor te){
        super(inventoryPlayer, te);
    }

    @Override
    protected int getFuelSlotXOffset(){
        return 69;
    }
}
