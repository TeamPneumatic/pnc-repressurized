package pneumaticCraft.common.inventory;

import net.minecraft.entity.player.InventoryPlayer;
import pneumaticCraft.common.tileentity.TileEntityAdvancedLiquidCompressor;

public class ContainerAdvancedLiquidCompressor extends ContainerLiquidCompressor{

    public ContainerAdvancedLiquidCompressor(InventoryPlayer inventoryPlayer, TileEntityAdvancedLiquidCompressor te){
        super(inventoryPlayer, te);
    }

    @Override
    protected int getFluidContainerOffset(){
        return 52;
    }

}
