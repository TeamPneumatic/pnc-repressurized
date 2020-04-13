package me.desht.pneumaticcraft.common.entity.projectile;

import net.minecraft.block.Block;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.WebBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityVortex extends ThrowableEntity {
    private int hitCounter = 0;

    // clientside: rendering X offset of vortex, depends on which hand the vortex was fired from
    private float renderOffsetX = -Float.MAX_VALUE;

    public EntityVortex(EntityType<? extends EntityVortex> type, World world) {
        super(type, world);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        super.tick();
        setMotion(getMotion().scale(0.95));
        if (getMotion().lengthSquared() < 0.1D) {
            remove();
        }
    }

    public boolean hasRenderOffsetX() {
        return renderOffsetX > -Float.MAX_VALUE;
    }

    public float getRenderOffsetX() {
        return renderOffsetX;
    }

    public void setRenderOffsetX(float renderOffsetX) {
        this.renderOffsetX = renderOffsetX;
    }

    private boolean tryCutPlants(BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        if (vortexBreakable(block)) {
            world.destroyBlock(pos, true);
            return true;
        }
        return false;
    }

    @Override
    public float getGravityVelocity() {
        return 0;
    }

    @Override
    protected void onImpact(RayTraceResult rtr) {
        if (rtr.getType() == RayTraceResult.Type.ENTITY) {
            Entity entity = ((EntityRayTraceResult) rtr).getEntity();
            entity.setMotion(entity.getMotion().add(this.getMotion().add(0, 0.4, 0)));
            ItemStack shears = new ItemStack(Items.SHEARS);
            if (entity instanceof LivingEntity) {
                shears.getItem().itemInteractionForEntity(shears, null, (LivingEntity) entity, Hand.MAIN_HAND);
            }
        } else if (rtr.getType() == RayTraceResult.Type.BLOCK) {
            BlockPos pos = ((BlockRayTraceResult) rtr).getPos();
            Block block = world.getBlockState(pos).getBlock();
            if (vortexBreakable(block)) {
                if (!world.isRemote) {
                    BlockPos.Mutable mPos = new BlockPos.Mutable(pos);
                    if (tryCutPlants(pos)) {
                        int plantsCut = 1;
                        for (int x = -2; x <= 2; x++) {
                            for (int y = -2; y <= 2; y++) {
                                for (int z = -2; z <= 2; z++) {
                                    if (x == 0 && y == 0 && z == 0) continue;
                                    mPos.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                                    if (tryCutPlants(mPos)) plantsCut++;
                                }
                            }
                        }
                        // slow the vortex down for each plant it broke
                        double mult = Math.pow(0.85D, plantsCut);
                        setMotion(getMotion().scale(mult));
                    }
                }
            } else {
                remove();
            }
        }
        hitCounter++;
        if (hitCounter > 20) remove();
    }

    private boolean vortexBreakable(Block block) {
        return block instanceof IPlantable || block instanceof LeavesBlock || block instanceof WebBlock;
    }

    @Override
    protected void registerData() {

    }
}
