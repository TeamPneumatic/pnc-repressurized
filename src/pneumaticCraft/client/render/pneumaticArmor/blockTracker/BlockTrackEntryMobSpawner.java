package pneumaticCraft.client.render.pneumaticArmor.blockTracker;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import pneumaticCraft.api.client.pneumaticHelmet.IBlockTrackEntry;
import pneumaticCraft.client.render.pneumaticArmor.hacking.block.HackableMobSpawner;
import pneumaticCraft.common.util.PneumaticCraftUtils;

public class BlockTrackEntryMobSpawner implements IBlockTrackEntry{

    @Override
    public boolean shouldTrackWithThisEntry(IBlockAccess world, int x, int y, int z, Block block, TileEntity te){
        return block == Blocks.mob_spawner;
    }

    @Override
    public boolean shouldBeUpdatedFromServer(TileEntity te){
        return true;
    }

    @Override
    public int spamThreshold(){
        return 10;
    }

    @Override
    public void addInformation(World world, int x, int y, int z, TileEntity te, List<String> infoList){
        if(te instanceof TileEntityMobSpawner) {
            MobSpawnerBaseLogic spawner = ((TileEntityMobSpawner)te).func_145881_a();
            infoList.add("Spawner Type: " + StatCollector.translateToLocal("entity." + spawner.getEntityNameToSpawn() + ".name"));
            if(spawner.isActivated()) {
                infoList.add("Time until next spawn: " + PneumaticCraftUtils.convertTicksToMinutesAndSeconds(spawner.spawnDelay, false));
            } else if(HackableMobSpawner.isHacked(world, x, y, z)) {
                infoList.add("Spawner is hacked");
            } else {
                infoList.add("Spawner is standing by");
            }
        }

    }

    @Override
    public String getEntryName(){
        return Blocks.mob_spawner.getUnlocalizedName() + ".name";
    }
}
