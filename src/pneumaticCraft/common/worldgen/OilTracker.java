package pneumaticCraft.common.worldgen;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

public class OilTracker extends WorldSavedData{
    public static final String DATA_KEY = "PneumaticCraftOilReserves";
    private final Set<Point> oilRichChunks = new HashSet<Point>();

    public OilTracker(String key){
        super(key);
    }

    public static void setContainingReserves(World world, int chunkX, int chunkZ, boolean contains){
        OilTracker instance = getInstance(world);
        Point p = new Point(chunkX, chunkZ);
        if(contains) {
            instance.oilRichChunks.add(p);
        } else {
            instance.oilRichChunks.remove(p);
        }
        instance.markDirty();
    }

    public static boolean containsReserves(World world, int x, int z){
        OilTracker instance = getInstance(world);
        if(instance.oilRichChunks.contains(new Point(x >> 4, z >> 4))) {
            return true;
        } else {
            return false;
        }
    }

    private static OilTracker getInstance(World world){
        OilTracker instance = (OilTracker)world.loadItemData(OilTracker.class, DATA_KEY);
        if(instance == null) {
            instance = new OilTracker(DATA_KEY);
            world.setItemData(DATA_KEY, instance);
        }
        return instance;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        oilRichChunks.clear();
        NBTTagList list = tag.getTagList("chunks", 10);
        for(int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound t = list.getCompoundTagAt(i);
            oilRichChunks.add(new Point(t.getInteger("x"), t.getInteger("y")));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        NBTTagList list = new NBTTagList();
        for(Point p : oilRichChunks) {
            NBTTagCompound t = new NBTTagCompound();
            t.setInteger("x", p.x);
            t.setInteger("y", p.y);
            list.appendTag(t);
        }
        tag.setTag("chunks", list);
    }

}
