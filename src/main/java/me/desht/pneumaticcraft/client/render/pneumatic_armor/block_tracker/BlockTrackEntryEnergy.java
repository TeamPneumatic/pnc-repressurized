package me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;

public class BlockTrackEntryEnergy implements IBlockTrackEntry {
    @Override
    public boolean shouldTrackWithThisEntry(IBlockAccess world, BlockPos pos, IBlockState state, TileEntity te) {
        if (te == null || TrackerBlacklistManager.isEnergyBlacklisted(te)) {
            return false;
        }
        if (te.hasCapability(CapabilityEnergy.ENERGY, null)) {
            return true;
        }
        for (EnumFacing face: EnumFacing.VALUES) {
            if (te.hasCapability(CapabilityEnergy.ENERGY, face)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldBeUpdatedFromServer(TileEntity te) {
        return true;
    }

    @Override
    public int spamThreshold() {
        return 8;
    }

    @Override
    public void addInformation(World world, BlockPos pos, TileEntity te, List<String> infoList) {
        try {
            infoList.add("blockTracker.info.rf");
            // TODO: getting capabilities client-side is not a reliable way to do this
            // Need a more formal framework for sync'ing server-side data to the client
            if (te.hasCapability(CapabilityEnergy.ENERGY, null)) {
                IEnergyStorage storage = te.getCapability(CapabilityEnergy.ENERGY, null);
                infoList.add(storage.getEnergyStored() + " / " + storage.getMaxEnergyStored() + " RF");
            }
        } catch (Throwable e) {
            TrackerBlacklistManager.addEnergyTEToBlacklist(te, e);
        }
    }

    @Override
    public String getEntryName() {
        return "blockTracker.module.rf";
    }
}
