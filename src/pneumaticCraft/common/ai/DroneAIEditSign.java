package pneumaticCraft.common.ai;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.common.progwidgets.ISignEditWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;
import pneumaticCraft.common.tileentity.TileEntityAphorismTile;

public class DroneAIEditSign extends DroneAIBlockInteraction<ProgWidgetAreaItemBase>{

    public DroneAIEditSign(IDroneBase drone, ProgWidgetAreaItemBase widget){
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(ChunkPosition pos){
        TileEntity te = drone.getWorld().getTileEntity(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
        if(te instanceof TileEntitySign) {
            TileEntitySign sign = (TileEntitySign)te;
            String[] lines = ((ISignEditWidget)widget).getLines();
            for(int i = 0; i < 4; i++) {
                sign.signText[i] = i < lines.length ? lines[i] : "";
            }
            drone.getWorld().markBlockForUpdate(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
        } else if(te instanceof TileEntityAphorismTile) {
            TileEntityAphorismTile sign = (TileEntityAphorismTile)te;
            sign.setTextLines(((ISignEditWidget)widget).getLines());
        }
        return false;
    }

    @Override
    protected boolean doBlockInteraction(ChunkPosition pos, double distToBlock){
        return false;
    }
}
