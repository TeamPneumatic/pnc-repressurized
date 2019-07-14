package me.desht.pneumaticcraft.common.thirdparty.thaumcraft;

import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;

import java.util.List;
import java.util.Map;

public class BlockTrackEntryThaumcraft implements IBlockTrackEntry {
    @Override
    public boolean shouldTrackWithThisEntry(IBlockAccess world, BlockPos pos, BlockState state, TileEntity te) {
        return te instanceof IAspectContainer;
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
        if (te instanceof IAspectContainer) {
            IAspectContainer container = (IAspectContainer)te;
            AspectList aspects = container.getAspects();
            if (aspects != null && aspects.size() > 0) {
                infoList.add("blockTracker.info.thaumcraft");
                for(Map.Entry<Aspect, Integer> entry : aspects.aspects.entrySet()) {
                    infoList.add("\u2022 " + entry.getValue() + " x " + entry.getKey().getName());
                }
            } else {
                infoList.add(I18n.format("blockTracker.info.thaumcraft.none"));
            }
        }
    }

    @Override
    public String getEntryName() {
        return "blockTracker.module.thaumcraft";
    }
}
