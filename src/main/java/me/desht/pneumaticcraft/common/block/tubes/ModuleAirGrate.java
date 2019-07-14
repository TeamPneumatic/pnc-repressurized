package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.client.model.module.ModelAirGrate;
import me.desht.pneumaticcraft.client.model.module.ModelModuleBase;
import me.desht.pneumaticcraft.client.particle.AirParticleData;
import me.desht.pneumaticcraft.client.render.RenderRangeLines;
import me.desht.pneumaticcraft.common.tileentity.TileEntityHeatSink;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ModuleAirGrate extends TubeModule {
    private int grateRange;
    private boolean vacuum;
    private final Set<TileEntityHeatSink> heatSinks = new HashSet<>();
    private final RenderRangeLines rangeLineRenderer = new RenderRangeLines(0x5500FF00);
    private boolean resetRendering = true;
    private EntityFilter entityFilter = null;
    private TileEntity adjacentInsertionTE = null;
    private Direction adjacentInsertionSide;

    public ModuleAirGrate() {
    }

    private int getRange() {
        float range = pressureTube.getAirHandler(null).getPressure() * 4;
        vacuum = range < 0;
        if (vacuum) range = -range * 4;
        return (int) range;
    }

    @Override
    public double getWidth() {
        return 1;
    }

    @Override
    public void update() {
        super.update();

        World world = pressureTube.world();
        BlockPos pos = pressureTube.pos();

        if (!world.isRemote) {
            int oldGrateRange = grateRange;
            grateRange = getRange();
            pressureTube.getAirHandler(null).addAir((vacuum ? 1 : -1) * grateRange * PneumaticValues.USAGE_AIR_GRATE);
            if (oldGrateRange != grateRange) sendDescriptionPacket();

            coolHeatSinks();
        } else {
            if (resetRendering) {
                rangeLineRenderer.resetRendering(grateRange);
                resetRendering = false;
            }
            rangeLineRenderer.update();
        }

        pushEntities(world, pos, new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D));
    }

    private AxisAlignedBB getAffectedAABB() {
        return new AxisAlignedBB(pressureTube.pos()).grow(grateRange);
    }

    private void pushEntities(World world, BlockPos pos, Vec3d tileVec) {
        AxisAlignedBB bbBox = getAffectedAABB();
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, bbBox, entityFilter);
        double d0 = grateRange + 0.5D;
        for (Entity entity : entities) {
            if (!entity.world.isRemote && entity instanceof ItemEntity && entity.isAlive()
                    && entity.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) < 1D) {
                tryItemInsertion((ItemEntity) entity);
            } else if (!(entity instanceof PlayerEntity) || !((PlayerEntity) entity).isCreative()) {
                Vec3d entityVec = new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
                RayTraceContext ctx = new RayTraceContext(entityVec, tileVec, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity);
                BlockRayTraceResult trace = world.rayTraceBlocks(ctx);
                if (trace != null && trace.getPos().equals(pos)) {
                    double x = (entity.posX - pos.getX() - 0.5D) / d0;
                    double y = (entity.posY + entity.getEyeHeight() - pos.getY() - 0.5D) / d0;
                    y -= 0.08;  // kludge: avoid entities getting stuck on edges, e.g. farmland->full block
                    double z = (entity.posZ - pos.getZ() - 0.5D) / d0;
                    double d4 = Math.sqrt(x * x + y * y + z * z);
                    double d5 = 1.0D - d4;

                    if (d5 > 0.0D) {
                        d5 *= d5;
                        if (!vacuum) d5 *= -1;
                        entity.setMotion(x / d4 * d5 * 0.1, y / d4 * d5 * 0.1, z / d4 * d5 * 0.1);
                        if (world.isRemote && world.rand.nextDouble() * 0.85 > d4) {
                            if (vacuum) {
                                world.addParticle(AirParticleData.DENSE, entity.posX, entity.posY, entity.posZ, -x, -y, -z);
                            } else {
                                world.addParticle(AirParticleData.DENSE, pos.getX() + 0.5 + x, pos.getY() + 0.5 + y, pos.getZ() + 0.5 + z, x, y, z);
                            }
                        }
                    }
                }
            }
        }
    }

    private void tryItemInsertion(ItemEntity entity) {
        if (getAdjacentInventory() != null) {
            ItemStack stack = entity.getItem();
            ItemStack excess = IOHelper.insert(getAdjacentInventory(), stack, adjacentInsertionSide, false);
            if (excess.isEmpty()) {
                entity.remove();
            } else {
                entity.setItem(excess);
            }
        }
    }

    private TileEntity getAdjacentInventory() {
        if (adjacentInsertionTE != null && !adjacentInsertionTE.isRemoved()) {
            return adjacentInsertionTE;
        }

        adjacentInsertionTE = null;
        for (Direction dir : Direction.VALUES) {
            TileEntity inv = pressureTube.world().getTileEntity(pressureTube.pos().offset(dir));
            if (inv != null && inv.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite()).isPresent()) {
                adjacentInsertionTE = inv;
                adjacentInsertionSide = dir.getOpposite();
                break;
            }
        }
        return adjacentInsertionTE;
    }

    private void coolHeatSinks() {
        if (grateRange > 2) {
            int curTeIndex = (int) (pressureTube.world().getGameTime() % 27);
            BlockPos curPos = pressureTube.pos().offset(dir, 2).add(-1 + curTeIndex % 3, -1 + curTeIndex / 3 % 3, -1 + curTeIndex / 9 % 3);
            TileEntity te = pressureTube.world().getTileEntity(curPos);
            if (te instanceof TileEntityHeatSink) heatSinks.add((TileEntityHeatSink) te);

            Iterator<TileEntityHeatSink> iterator = heatSinks.iterator();
            while (iterator.hasNext()) {
                TileEntityHeatSink heatSink = iterator.next();
                if (heatSink.isRemoved()) {
                    iterator.remove();
                } else {
                    for (int i = 0; i < 4; i++)
                        heatSink.onFannedByAirGrate();
                }
            }
        }
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        vacuum = tag.getBoolean("vacuum");
        grateRange = tag.getInt("grateRange");
        String f = tag.getString("entityFilter");
        entityFilter = f.isEmpty() ? null : EntityFilter.fromString(f);
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.putBoolean("vacuum", vacuum);
        tag.putInt("grateRange", grateRange);
        tag.putString("entityFilter", entityFilter == null ? "" : entityFilter.toString());
    }

    @Override
    public String getType() {
        return Names.MODULE_AIR_GRATE;
    }

    @Override
    public void addInfo(List<String> curInfo) {
        super.addInfo(curInfo);
        curInfo.add("Status: " + TextFormatting.WHITE + (grateRange == 0 ? "Idle" : vacuum ? "Attracting" : "Repelling"));
        curInfo.add("Range: " + TextFormatting.WHITE + grateRange + " blocks");
        if (entityFilter != null)
            curInfo.add("Entity Filter: " + TextFormatting.WHITE + "\"" + entityFilter.toString() + "\"");
    }

    @Override
    public boolean hasGui() {
        return true;
    }

    @Override
    public Class<? extends ModelModuleBase> getModelClass() {
        return ModelAirGrate.class;
    }

    @Override
    public void doExtraRendering() {
        rangeLineRenderer.render();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return getAffectedAABB();
    }

    public String getEntityFilterString() {
        return entityFilter == null ? "" : entityFilter.toString();
    }

    public void setEntityFilter(String filter) {
        entityFilter = EntityFilter.fromString(filter);
    }

}
