package me.desht.pneumaticcraft.client.render.pneumatic_armor.block_tracker;

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.common.hacking.block.HackableMobSpawner;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockSpawnerAgitator;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.Reflections;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;

import java.util.Collections;
import java.util.List;

public class BlockTrackEntryMobSpawner implements IBlockTrackEntry {

    @Override
    public boolean shouldTrackWithThisEntry(IBlockReader world, BlockPos pos, BlockState state, TileEntity te) {
        return state.getBlock() == Blocks.SPAWNER;
    }

    @Override
    public List<BlockPos> getServerUpdatePositions(TileEntity te) {
        return Collections.singletonList(te.getPos());
    }

    @Override
    public int spamThreshold() {
        return 10;
    }

    @Override
    public void addInformation(World world, BlockPos pos, TileEntity te, Direction face, List<String> infoList) {
        if (te instanceof MobSpawnerTileEntity) {
            AbstractSpawner spawner = ((MobSpawnerTileEntity) te).getSpawnerBaseLogic();
            Entity e = spawner.getCachedEntity();
            infoList.add("Spawner Type: " + TextFormatting.AQUA + e.getName());
            if (Reflections.isActivated(spawner) || SemiBlockManager.getInstance(world).getSemiBlock(SemiBlockSpawnerAgitator.class, world, pos) != null) {
                infoList.add("Time until next spawn: " + PneumaticCraftUtils.convertTicksToMinutesAndSeconds(spawner.spawnDelay, false));
            } else if (HackableMobSpawner.isHacked(world, pos)) {
                infoList.add("Spawner is hacked");
            } else {
                infoList.add("Spawner is standing by");
            }
        }

    }

    @Override
    public String getEntryName() {
        return Blocks.SPAWNER.getTranslationKey() + ".name";
    }
}
