package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.common.progwidgets.ISignEditWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetAreaItemBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAphorismTile;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

public class DroneAIEditSign extends DroneAIBlockInteraction<ProgWidgetAreaItemBase> {

    public DroneAIEditSign(IDroneBase drone, ProgWidgetAreaItemBase widget) {
        super(drone, widget);
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        TileEntity te = drone.world().getTileEntity(pos);
        if (te instanceof SignTileEntity) {
            SignTileEntity sign = (SignTileEntity) te;
            String[] lines = ((ISignEditWidget) progWidget).getLines();
            for (int i = 0; i < 4; i++) {
                sign.setText(i, new StringTextComponent(i < lines.length ? lines[i] : ""));
            }
            BlockState state = drone.world().getBlockState(pos);
            drone.world().notifyBlockUpdate(pos, state, state, 3);
        } else if (te instanceof TileEntityAphorismTile) {
            TileEntityAphorismTile sign = (TileEntityAphorismTile) te;
            sign.setTextLines(((ISignEditWidget) progWidget).getLines());
        }
        return false;
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double distToBlock) {
        return false;
    }
}
