package pneumaticCraft.common.block.tubes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Facing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import pneumaticCraft.client.model.IBaseModel;
import pneumaticCraft.client.model.tubemodules.ModelAirGrate;
import pneumaticCraft.client.render.RenderRangeLines;
import pneumaticCraft.common.ai.StringFilterEntitySelector;
import pneumaticCraft.common.block.pneumaticPlants.BlockPneumaticPlantBase;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketSpawnParticle;
import pneumaticCraft.common.tileentity.TileEntityHeatSink;
import pneumaticCraft.lib.Names;
import pneumaticCraft.lib.PneumaticValues;
import pneumaticCraft.proxy.CommonProxy.EnumGuiId;

public class ModuleAirGrate extends TubeModule{
    private final IBaseModel model = new ModelAirGrate();
    private int grateRange;
    private boolean vacuum;
    public String entityFilter = "";
    private final Set<TileEntityHeatSink> heatSinks = new HashSet<TileEntityHeatSink>();
    private final RenderRangeLines rangeLineRenderer = new RenderRangeLines(0x55FF0000);

    private int plantCheckX = Integer.MIN_VALUE;
    private int plantCheckZ = Integer.MIN_VALUE;

    public ModuleAirGrate(){
        rangeLineRenderer.resetRendering(1);
    }

    private int getRange(){
        float range = pressureTube.getAirHandler().getPressure(ForgeDirection.UNKNOWN) * 4;
        vacuum = range < 0;
        if(vacuum) range = -range * 4;
        return (int)range;
    }

    @Override
    public double getWidth(){
        return 1;
    }

    @Override
    public void update(){
        super.update();
        World worldObj = pressureTube.world();
        int xCoord = pressureTube.x();
        int yCoord = pressureTube.y();
        int zCoord = pressureTube.z();
        Vec3 tileVec = Vec3.createVectorHelper(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D);
        if(!worldObj.isRemote) {
            int oldGrateRange = grateRange;
            grateRange = getRange();
            pressureTube.getAirHandler().addAir((vacuum ? 1 : -1) * grateRange * PneumaticValues.USAGE_AIR_GRATE, ForgeDirection.UNKNOWN);
            if(oldGrateRange != grateRange) sendDescriptionPacket();

            checkForPlantsAndFarm(worldObj, xCoord, yCoord, zCoord, grateRange);
            coolHeatSinks(worldObj, xCoord, yCoord, zCoord, grateRange);

        } else {
            rangeLineRenderer.update();
            /*  updateParticleTargets(tileVec, grateRange);
              for(Vec3 particleVec : particleTargets) {

                  //if(worldObj.rand.nextInt(10) == 0) {
                  Vec3 motionVec = particleVec.subtract(tileVec);
                  double force = 0.1D;
                  motionVec.xCoord *= force;
                  motionVec.yCoord *= force;
                  motionVec.zCoord *= force;
                  if(vacuum) {
                      worldObj.spawnParticle("smoke", particleVec.xCoord, particleVec.yCoord, particleVec.zCoord, -motionVec.xCoord, -motionVec.yCoord, -motionVec.zCoord);
                  } else {
                      worldObj.spawnParticle("smoke", tileVec.xCoord, tileVec.yCoord, tileVec.zCoord, motionVec.xCoord, motionVec.yCoord, motionVec.zCoord);
                  }
                  //   }

              }*/

        }
        AxisAlignedBB bbBox = AxisAlignedBB.getBoundingBox(xCoord - grateRange, yCoord - grateRange, zCoord - grateRange, xCoord + grateRange + 1, yCoord + grateRange + 1, zCoord + grateRange + 1);
        List<Entity> entities = worldObj.selectEntitiesWithinAABB(Entity.class, bbBox, new StringFilterEntitySelector().setFilter(entityFilter));
        double d0 = grateRange + 0.5D;
        for(Entity entity : entities) {
            if(!entity.worldObj.isRemote && entity.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) < 0.6D && entity instanceof EntityItem && !entity.isDead) {
                List<IInventory> inventories = new ArrayList<IInventory>();
                List<Integer> sides = new ArrayList<Integer>();
                for(int i = 0; i < 6; i++) {
                    IInventory inventory = TileEntityHopper.func_145893_b(worldObj, xCoord + Facing.offsetsXForSide[i], yCoord + Facing.offsetsYForSide[i], zCoord + Facing.offsetsZForSide[i]);
                    if(inventory != null) {
                        inventories.add(inventory);
                        sides.add(i);
                    }
                }
                if(inventories.size() == 0) continue;// if there isn't a
                                                     // inventory attached,
                                                     // stop handling.
                int inventoryIndexSelected = new Random().nextInt(inventories.size());
                IInventory inventory = inventories.get(inventoryIndexSelected);
                int side = sides.get(inventoryIndexSelected);
                side = Facing.oppositeSide[side];

                ItemStack leftoverStack = TileEntityHopper.func_145889_a(inventory, ((EntityItem)entity).getEntityItem(), side);
                if(leftoverStack == null || leftoverStack.stackSize == 0) {
                    entity.setDead();
                }
            } else {
                if(!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).capabilities.isCreativeMode) {
                    Vec3 entityVec = Vec3.createVectorHelper(entity.posX, entity.posY, entity.posZ);
                    MovingObjectPosition trace = worldObj.rayTraceBlocks(entityVec, tileVec);
                    if(trace != null && trace.blockX == xCoord && trace.blockY == yCoord && trace.blockZ == zCoord) {
                        double d1 = (entity.posX - xCoord - 0.5D) / d0;
                        double d2 = (entity.posY - yCoord - 0.5D) / d0;
                        double d3 = (entity.posZ - zCoord - 0.5D) / d0;
                        double d4 = Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
                        double d5 = 1.0D - d4;

                        if(d5 > 0.0D) {
                            d5 *= d5;
                            if(!vacuum) d5 *= -1;
                            entity.motionX -= d1 / d4 * d5 * 0.1D;
                            entity.motionY -= d2 / d4 * d5 * 0.1D;
                            entity.motionZ -= d3 / d4 * d5 * 0.1D;
                        }
                    }
                }
            }
        }
    }

    private void checkForPlantsAndFarm(World worldObj, int x, int y, int z, int plantCheckRange){
        if(grateRange > 0 && worldObj.getTotalWorldTime() % 5 == 0) {
            if(plantCheckX < x - plantCheckRange || plantCheckZ < z - plantCheckRange) {
                plantCheckX = x - plantCheckRange;
                plantCheckZ = z - plantCheckRange;
            }

            if(plantCheckX != x || plantCheckZ != z) { // we know that we're no plant, avoid getBlock
                Block b = worldObj.getBlock(plantCheckX, y, plantCheckZ);
                NetworkHandler.sendToAllAround(new PacketSpawnParticle("reddust", plantCheckX + 0.5, y + 0.5, plantCheckZ + 0.5, 0, 0, 0), worldObj);
                if(b instanceof BlockPneumaticPlantBase) {
                    ((BlockPneumaticPlantBase)b).attemptFarmByAirGrate(worldObj, plantCheckX, y, plantCheckZ);
                }
            }

            if(plantCheckZ++ >= z + plantCheckRange) {
                plantCheckZ = z - plantCheckRange;
                if(plantCheckX++ >= x + plantCheckRange) {
                    plantCheckX = x - plantCheckRange;
                }
            }
        }
    }

    private void coolHeatSinks(World world, int x, int y, int z, int range){
        if(grateRange > 2) {
            int curTeIndex = (int)(world.getTotalWorldTime() % 27);
            x += dir.offsetX * 2;
            y += dir.offsetY * 2;
            z += dir.offsetZ * 2;
            TileEntity te = world.getTileEntity(x - 1 + curTeIndex % 3, y - 1 + curTeIndex / 3 % 3, z - 1 + curTeIndex / 9 % 3);
            if(te instanceof TileEntityHeatSink) heatSinks.add((TileEntityHeatSink)te);

            Iterator<TileEntityHeatSink> iterator = heatSinks.iterator();
            while(iterator.hasNext()) {
                TileEntityHeatSink heatSink = iterator.next();
                if(heatSink.isInvalid()) {
                    iterator.remove();
                } else {
                    for(int i = 0; i < 4; i++)
                        heatSink.onFannedByAirGrate();
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        vacuum = tag.getBoolean("vacuum");
        grateRange = tag.getInteger("grateRange");
        entityFilter = tag.getString("entityFilter");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setBoolean("vacuum", vacuum);
        tag.setInteger("grateRange", grateRange);
        tag.setString("entityFilter", entityFilter);
    }

    @Override
    public String getType(){
        return Names.MODULE_AIR_GRATE;
    }

    @Override
    public IBaseModel getModel(){
        return model;
    }

    @Override
    protected void renderModule(){
        GL11.glPushMatrix();
        GL11.glTranslated(0, 0, 2);
        rangeLineRenderer.render();
        if(isFake()) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glColor4d(1, 1, 1, 0.5);
        }
        GL11.glPopMatrix();
    }

    @Override
    public void addInfo(List<String> curInfo){
        curInfo.add("Status: " + EnumChatFormatting.WHITE + (grateRange == 0 ? "Idle" : vacuum ? "Attracting" : "Repelling"));
        curInfo.add("Range: " + EnumChatFormatting.WHITE + grateRange + " blocks");
        if(!entityFilter.equals("")) curInfo.add("Entity Filter: " + EnumChatFormatting.WHITE + "\"" + entityFilter + "\"");
    }

    @Override
    public void addItemDescription(List<String> curInfo){
        curInfo.add(EnumChatFormatting.BLUE + "Formula: Range(blocks) = 4.0 x pressure(bar),");
        curInfo.add(EnumChatFormatting.BLUE + "or -16 x pressure(bar), if vacuum");
        curInfo.add("This module will attract or repel any entity");
        curInfo.add("within range dependant on whether it is in");
        curInfo.add("vacuum or under pressure respectively.");
    }

    @Override
    protected EnumGuiId getGuiId(){
        return EnumGuiId.AIR_GRATE_MODULE;
    }
}
