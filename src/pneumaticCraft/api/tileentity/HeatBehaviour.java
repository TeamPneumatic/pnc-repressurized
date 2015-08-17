package pneumaticCraft.api.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import pneumaticCraft.api.IHeatExchangerLogic;

public abstract class HeatBehaviour<Tile extends TileEntity> {

    private IHeatExchangerLogic connectedHeatLogic;
    private World world;
    private int x, y, z;
    private Tile cachedTE;

    public void initialize(IHeatExchangerLogic connectedHeatLogic, World world, int x, int y, int z){
        this.connectedHeatLogic = connectedHeatLogic;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        cachedTE = null;
    }

    public IHeatExchangerLogic getHeatExchanger(){
        return connectedHeatLogic;
    }

    public World getWorld(){
        return world;
    }

    public Tile getTileEntity(){
        if(cachedTE == null || cachedTE.isInvalid()) {
            cachedTE = (Tile)world.getTileEntity(x, y, z);
        }
        return cachedTE;
    }

    public abstract String getId();

    public abstract boolean isApplicable();

    /**
     * Called every tick to update this behaviour.
     */
    public abstract void update();

}
