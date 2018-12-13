package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.client.model.module.ModelAirGrate;
import me.desht.pneumaticcraft.client.model.module.ModelModuleBase;
import me.desht.pneumaticcraft.client.render.RenderRangeLines;
import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.tileentity.TileEntityHeatSink;
import me.desht.pneumaticcraft.common.util.EntityFilter;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.EnumCustomParticleType;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

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
        Vec3d tileVec = new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        if (!world.isRemote) {
            int oldGrateRange = grateRange;
            grateRange = getRange();
            pressureTube.getAirHandler(null).addAir((vacuum ? 1 : -1) * grateRange * PneumaticValues.USAGE_AIR_GRATE);
            if (oldGrateRange != grateRange) sendDescriptionPacket();

            coolHeatSinks(world, pos, grateRange);

        } else {
            if (resetRendering) {
                rangeLineRenderer.resetRendering(grateRange);
                resetRendering = false;
            }
            rangeLineRenderer.update();
        }

        pushEntities(world, pos, tileVec);
    }

    private AxisAlignedBB getAffectedAABB() {
        return new AxisAlignedBB(pressureTube.pos()).grow(grateRange);
    }

    private void pushEntities(World world, BlockPos pos, Vec3d tileVec) {
        AxisAlignedBB bbBox = getAffectedAABB();
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, bbBox, entityFilter);
        double d0 = grateRange + 0.5D;
        for (Entity entity : entities) {
            if (!entity.world.isRemote && entity instanceof EntityItem && entity.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) < 0.6D && !entity.isDead) {
                ItemStack leftover = ((EntityItem) entity).getItem();
                for (EnumFacing dir : EnumFacing.VALUES) {
                    TileEntity inv = pressureTube.world().getTileEntity(pos.offset(dir));
                    leftover = IOHelper.insert(inv, leftover, dir.getOpposite(), false);
                    if (leftover.isEmpty()) break;
                }
                if (leftover.isEmpty()) {
                    entity.setDead();
                } else {
                    ((EntityItem) entity).setItem(leftover);
                }
            } else {
                if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).capabilities.isCreativeMode) {
                    Vec3d entityVec = new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
                    RayTraceResult trace = world.rayTraceBlocks(entityVec, tileVec);
                    if (trace != null && trace.getBlockPos().equals(pos)) {
                        double d1 = (entity.posX - pos.getX() - 0.5D) / d0;
                        double d2 = (entity.posY + entity.getEyeHeight() - pos.getY() - 0.5D) / d0;
                        double d3 = (entity.posZ - pos.getZ() - 0.5D) / d0;
                        double d4 = Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
                        double d5 = 1.0D - d4;

                        if (d5 > 0.0D) {
                            d5 *= d5;
                            if (!vacuum) d5 *= -1;
                            entity.motionX -= d1 / d4 * d5 * 0.1D;
                            entity.motionY -= d2 / d4 * d5 * 0.1D;
                            entity.motionZ -= d3 / d4 * d5 * 0.1D;
                            if (world.isRemote && world.rand.nextDouble() * 0.85 > d4) {
                                Vec3d vec = new Vec3d(pos.getX() + 0.5 + d1, pos.getY() + 0.5 + d2, pos.getZ() + 0.5 + d3);
                                PneumaticCraftRepressurized.proxy.playCustomParticle(EnumCustomParticleType.AIR_PARTICLE_DENSE, world,
                                        pos.getX() + 0.5 + d1, pos.getY() + 0.5 + d2, pos.getZ() + 0.5 + d3, d1, d2, d3);
                            }
                        }
                    }
                }
            }
        }
    }

    private void coolHeatSinks(World world, BlockPos pos, int range) {
        if (grateRange > 2) {
            int curTeIndex = (int) (world.getTotalWorldTime() % 27);
            BlockPos curPos = pos.offset(dir, 2).add(-1 + curTeIndex % 3, -1 + curTeIndex / 3 % 3, -1 + curTeIndex / 9 % 3);
            TileEntity te = world.getTileEntity(curPos);
            if (te instanceof TileEntityHeatSink) heatSinks.add((TileEntityHeatSink) te);

            Iterator<TileEntityHeatSink> iterator = heatSinks.iterator();
            while (iterator.hasNext()) {
                TileEntityHeatSink heatSink = iterator.next();
                if (heatSink.isInvalid()) {
                    iterator.remove();
                } else {
                    for (int i = 0; i < 4; i++)
                        heatSink.onFannedByAirGrate();
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        vacuum = tag.getBoolean("vacuum");
        grateRange = tag.getInteger("grateRange");
        String f = tag.getString("entityFilter");
        entityFilter = f.isEmpty() ? null : EntityFilter.fromString(f);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("vacuum", vacuum);
        tag.setInteger("grateRange", grateRange);
        tag.setString("entityFilter", entityFilter == null ? "" : entityFilter.toString());
    }

    @Override
    public String getType() {
        return Names.MODULE_AIR_GRATE;
    }

    @Override
    public void addInfo(List<String> curInfo) {
        curInfo.add("Status: " + TextFormatting.WHITE + (grateRange == 0 ? "Idle" : vacuum ? "Attracting" : "Repelling"));
        curInfo.add("Range: " + TextFormatting.WHITE + grateRange + " blocks");
        if (entityFilter != null)
            curInfo.add("Entity Filter: " + TextFormatting.WHITE + "\"" + entityFilter.toString() + "\"");
    }

    @Override
    public void addItemDescription(List<String> curInfo) {
        curInfo.add(TextFormatting.BLUE + "Formula: Range(blocks) = 4.0 x pressure(bar),");
        curInfo.add(TextFormatting.BLUE + "or -16 x pressure(bar), if vacuum");
        curInfo.add("This module will attract or repel any entity");
        curInfo.add("within range dependant on whether it is in");
        curInfo.add("vacuum or under pressure respectively.");
    }

    @Override
    protected EnumGuiId getGuiId() {
        return EnumGuiId.AIR_GRATE_MODULE;
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
