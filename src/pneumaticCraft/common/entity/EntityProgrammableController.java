package pneumaticCraft.common.entity;

import net.minecraft.util.DamageSource;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import pneumaticCraft.common.entity.living.EntityDroneBase;
import pneumaticCraft.common.tileentity.TileEntityProgrammableController;

public class EntityProgrammableController extends EntityDroneBase{
    private final TileEntityProgrammableController controller;

    public EntityProgrammableController(World world){
        super(world);
        controller = null;
    }

    public EntityProgrammableController(World world, TileEntityProgrammableController controller){
        super(world);
        preventEntitySpawning = false;
        this.controller = controller;
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    @Override
    public boolean canBeCollidedWith(){
        return false;
    }

    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
    @Override
    public boolean canBePushed(){
        return false;
    }

    @Override
    public void onUpdate(){
        if(controller.isInvalid()) setDead();
        if(digLaser != null) digLaser.update();
        oldPropRotation = propRotation;
        propRotation += 1;
    }

    @Override
    protected double getLaserOffsetY(){
        return 0.45;
    }

    @Override
    public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_){
        return false;
    }

    @Override
    protected ChunkPosition getDugBlock(){
        return controller.getDugPosition();
    }
}
