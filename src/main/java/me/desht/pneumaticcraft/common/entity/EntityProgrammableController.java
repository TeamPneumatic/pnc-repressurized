package me.desht.pneumaticcraft.common.entity;

import me.desht.pneumaticcraft.common.entity.living.EntityDroneBase;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammableController;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityProgrammableController extends EntityDroneBase {
    private TileEntityProgrammableController controller;

//    public static EntityProgrammableController createProgrammableController(EntityType<EntityProgrammableController> type, World world) {
//        return new EntityProgrammableController(type, world);
//    }

    public EntityProgrammableController(EntityType<EntityProgrammableController> type, World world) {
        super(type, world);

        this.preventEntitySpawning = false;
    }

//    public EntityProgrammableController(World world, TileEntityProgrammableController controller) {
//        super(ModEntities.DRONE.get(), world);
//
//        this.preventEntitySpawning = false;
//        this.controller = controller;
//    }

    public void setController(TileEntityProgrammableController controller) {
        this.controller = controller;
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public void tick() {
        if (controller.isRemoved()) remove();
        oldPropRotation = propRotation;
        propRotation += 1;
    }

    @Override
    public double getLaserOffsetY() {
        return 0.45;
    }

    @Override
    public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_) {
        return false;
    }

    @Override
    public BlockPos getDugBlock() {
        return controller.getDugPosition();
    }

    @Override
    public ItemStack getDroneHeldItem() {
        return controller.getFakePlayer().getHeldItemMainhand();
    }

}
