package me.desht.pneumaticcraft.client.render.pneumaticArmor.blockTracker;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.block.HackableMobSpawner;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.common.util.Reflections;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class BlockTrackEntryMobSpawner implements IBlockTrackEntry {

    @Override
    public boolean shouldTrackWithThisEntry(IBlockAccess world, BlockPos pos, IBlockState state, TileEntity te) {
        return state.getBlock() == Blocks.MOB_SPAWNER;
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
    public void addInformation(World world, BlockPos pos, TileEntity te, List<String> infoList) {
        if (te instanceof TileEntityMobSpawner) {
            MobSpawnerBaseLogic spawner = ((TileEntityMobSpawner) te).getSpawnerBaseLogic();
            ResourceLocation rl = Reflections.getEntityId(spawner);
//            infoList.add("Spawner Type: " + I18n.format("entity." + spawner.getEntityNameToSpawn() + ".name"));
            infoList.add("Spawner Type: " + (rl == null ? "?" : rl.toString()));
            if (Reflections.isActivated(spawner)) {
                infoList.add("Time until next spawn: " + PneumaticCraftUtils.convertTicksToMinutesAndSeconds(Reflections.getSpawnDelay(spawner), false));
            } else if (HackableMobSpawner.isHacked(world, pos)) {
                infoList.add("Spawner is hacked");
            } else {
                infoList.add("Spawner is standing by");
            }
        }

    }

    @Override
    public String getEntryName() {
        return Blocks.MOB_SPAWNER.getUnlocalizedName() + ".name";
    }
}
