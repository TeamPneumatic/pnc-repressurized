package me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.FluidTrackEvent;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.util.List;

public class BlockTrackEntryFluid implements IBlockTrackEntry {
    @Override
    public boolean shouldTrackWithThisEntry(IBlockAccess world, BlockPos pos, IBlockState state, TileEntity te) {
        return te != null
                && !TrackerBlacklistManager.isFluidBlacklisted(te)
                && IBlockTrackEntry.hasCapabilityOnAnyFace(te, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
                && !MinecraftForge.EVENT_BUS.post(new FluidTrackEvent(te));
    }

    @Override
    public boolean shouldBeUpdatedFromServer(TileEntity te) {
        return true;
    }

    @Override
    public int spamThreshold() {
        return 10;
    }

    @Override
    public void addInformation(World world, BlockPos pos, TileEntity te, EnumFacing face, List<String> infoList) {
        try {
            IFluidHandler handler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face);

            if (handler != null) {
                int i = 1;
                for (IFluidTankProperties tank : handler.getTankProperties()) {
                    FluidStack stack = tank.getContents();
                    if (stack != null) {
                        infoList.add(I18n.format("blockTracker.info.fluids.tankFull", i, stack.amount, tank.getCapacity(), stack.getLocalizedName()));
                    } else {
                        infoList.add(I18n.format("blockTracker.info.fluids.tankEmpty", i, tank.getCapacity()));
                    }
                    i++;
                }
            }
        } catch (Throwable e) {
            TrackerBlacklistManager.addFluidTEToBlacklist(te, e);
        }
    }

    @Override
    public String getEntryName() {
        return "blockTracker.module.fluids";
    }
}
