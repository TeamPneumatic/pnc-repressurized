package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.ISignEditWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAphorismTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class DroneAIEditSign extends DroneAIBlockInteraction<ProgWidgetAreaItemBase> {

    public DroneAIEditSign(IDroneBase drone, ProgWidgetAreaItemBase widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        TileEntity te = drone.world().getTileEntity(pos);
        if (te instanceof TileEntitySign) {
            TileEntitySign sign = (TileEntitySign) te;
            String[] lines = ((ISignEditWidget) widget).getLines();
            for (int i = 0; i < 4; i++) {
                sign.signText[i] = new TextComponentString(i < lines.length ? lines[i] : ""); //TODO 1.8 test
            }
            IBlockState state = drone.world().getBlockState(pos);
            drone.world().notifyBlockUpdate(pos, state, state, 3);
        } else if (te instanceof TileEntityAphorismTile) {
            TileEntityAphorismTile sign = (TileEntityAphorismTile) te;
            sign.setTextLines(((ISignEditWidget) widget).getLines());
        }
        return false;
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double distToBlock) {
        return false;
    }
}
