package me.desht.pneumaticcraft.common.thirdparty.ic2;

import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class BlockTrackEntryIC2 implements IBlockTrackEntry {
    @Override
    public boolean shouldTrackWithThisEntry(IBlockAccess world, BlockPos pos, BlockState state, TileEntity te) {
        return te instanceof IEnergyTile;
    }

    public boolean getServerUpdatePositions(TileEntity te) {
        return false;
    }

    @Override
    public int spamThreshold() {
        return 8;
    }

    @Override
    public void addInformation(World world, BlockPos pos, TileEntity te, Direction face, List<String> infoList) {
        infoList.add("blockTracker.info.ic2");
        if (te instanceof IEnergySource) {
            infoList.add("Providing Tier " + ((IEnergySource) te).getSourceTier());
        }
        if (te instanceof IEnergySink) {
            infoList.add("Accepting Tier " + ((IEnergySink) te).getSinkTier());
        }
    }

    @Override
    public String getEntryName() {
        return "blockTracker.module.ic2";
    }
}
