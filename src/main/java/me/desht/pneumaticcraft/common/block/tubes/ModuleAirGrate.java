package me.desht.pneumaticcraft.common.block.tubes;

import me.desht.pneumaticcraft.client.render.RenderRangeLines;
import me.desht.pneumaticcraft.common.ai.StringFilterEntitySelector;
import me.desht.pneumaticcraft.common.tileentity.TileEntityHeatSink;
import me.desht.pneumaticcraft.common.util.IOHelper;
import me.desht.pneumaticcraft.lib.Names;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
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
import org.lwjgl.opengl.GL11;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ModuleAirGrate extends TubeModule {
    private int grateRange;
    private boolean vacuum;
    public String entityFilter = "";
    private final Set<TileEntityHeatSink> heatSinks = new HashSet<TileEntityHeatSink>();
    private final RenderRangeLines rangeLineRenderer = new RenderRangeLines(0x55FF0000);

    public ModuleAirGrate() {
        rangeLineRenderer.resetRendering(1);
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
            rangeLineRenderer.update();
            /*  updateParticleTargets(tileVec, grateRange);
              for(Vec3d particleVec : particleTargets) {

                  //if(getWorld().rand.nextInt(10) == 0) {
                  Vec3d motionVec = particleVec.subtract(tileVec);
                  double force = 0.1D;
                  motionVec.getPos().getX() *= force;
                  motionVec.getPos().getY() *= force;
                  motionVec.getPos().getZ() *= force;
                  if(vacuum) {
                      getWorld().spawnParticle("smoke", particleVec.getPos().getX(), particleVec.getPos().getY(), particleVec.getPos().getZ(), -motionVec.getPos().getX(), -motionVec.getPos().getY(), -motionVec.getPos().getZ());
                  } else {
                      getWorld().spawnParticle("smoke", tileVec.getPos().getX(), tileVec.getPos().getY(), tileVec.getPos().getZ(), motionVec.getPos().getX(), motionVec.getPos().getY(), motionVec.getPos().getZ());
                  }
                  //   }

              }*/

        }

        AxisAlignedBB bbBox = new AxisAlignedBB(pos.add(-grateRange, -grateRange, -grateRange), pos.add(grateRange + 1, grateRange + 1, grateRange + 1));
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, bbBox, new StringFilterEntitySelector().setFilter(entityFilter));
        double d0 = grateRange + 0.5D;
        for (Entity entity : entities) {
            if (!entity.world.isRemote && entity.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) < 0.6D && entity instanceof EntityItem && !entity.isDead) {
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
                    Vec3d entityVec = new Vec3d(entity.posX, entity.posY, entity.posZ);
                    RayTraceResult trace = world.rayTraceBlocks(entityVec, tileVec);
                    if (trace != null && trace.getBlockPos().equals(pos)) {
                        double d1 = (entity.posX - pos.getX() - 0.5D) / d0;
                        double d2 = (entity.posY - pos.getY() - 0.5D) / d0;
                        double d3 = (entity.posZ - pos.getZ() - 0.5D) / d0;
                        double d4 = Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
                        double d5 = 1.0D - d4;

                        if (d5 > 0.0D) {
                            d5 *= d5;
                            if (!vacuum) d5 *= -1;
                            entity.motionX -= d1 / d4 * d5 * 0.1D;
                            entity.motionY -= d2 / d4 * d5 * 0.1D;
                            entity.motionZ -= d3 / d4 * d5 * 0.1D;
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
        entityFilter = tag.getString("entityFilter");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("vacuum", vacuum);
        tag.setInteger("grateRange", grateRange);
        tag.setString("entityFilter", entityFilter);
    }

    @Override
    public String getType() {
        return Names.MODULE_AIR_GRATE;
    }

    @Override
    public String getModelName() {
        return "airGrate";
    }

    @Override
    protected void renderModule() {
        GL11.glPushMatrix();
        GL11.glTranslated(0, 0, 2);
        rangeLineRenderer.render();
        if (isFake()) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glColor4d(1, 1, 1, 0.5);
        }
        GL11.glPopMatrix();
        GL11.glRotated(90, 1, 0, 0);
        GL11.glTranslated(0, 1, 1);
    }

    @Override
    public void addInfo(List<String> curInfo) {
        curInfo.add("Status: " + TextFormatting.WHITE + (grateRange == 0 ? "Idle" : vacuum ? "Attracting" : "Repelling"));
        curInfo.add("Range: " + TextFormatting.WHITE + grateRange + " blocks");
        if (!entityFilter.equals(""))
            curInfo.add("Entity Filter: " + TextFormatting.WHITE + "\"" + entityFilter + "\"");
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
}
