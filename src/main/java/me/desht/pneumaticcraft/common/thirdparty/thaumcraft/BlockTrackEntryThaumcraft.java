package me.desht.pneumaticcraft.common.thirdparty.thaumcraft;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class BlockTrackEntryThaumcraft implements IBlockTrackEntry {
    @Override
    public boolean shouldTrackWithThisEntry(IBlockReader world, BlockPos pos, BlockState state, TileEntity te) {
        return false;
        // return te instance IAspectContainer
    }

    @Override
    public List<BlockPos> getServerUpdatePositions(TileEntity te) {
        return Collections.emptyList();
    }

    @Override
    public int spamThreshold() {
        return 8;
    }

    @Override
    public void addInformation(World world, BlockPos pos, TileEntity te, Direction face, List<String> infoList) {
//        if (te instanceof IAspectContainer) {
//            IAspectContainer container = (IAspectContainer)te;
//            AspectList aspects = container.getAspects();
//            if (aspects != null && aspects.size() > 0) {
//                infoList.add("pneumaticcraft.blockTracker.info.thaumcraft");
//                for(Map.Entry<Aspect, Integer> entry : aspects.aspects.entrySet()) {
//                    infoList.add("\u2022 " + entry.getValue() + " x " + entry.getKey().getName());
//                }
//            } else {
//                infoList.add(I18n.format("pneumaticcraft.blockTracker.info.thaumcraft.none"));
//            }
//        }
    }

    @Override
    public ResourceLocation getEntryID() {
        return RL("block_tracker_module_thaumcraft");
    }
}
