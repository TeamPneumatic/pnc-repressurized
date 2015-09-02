package pneumaticCraft.common;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.common.ai.DroneClaimManager;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.config.AmadronOfferPeriodicConfig;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketServerTickTime;
import pneumaticCraft.common.recipes.AmadronOfferManager;
import pneumaticCraft.common.tileentity.TileEntityElectrostaticCompressor;
import pneumaticCraft.lib.PneumaticValues;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class TickHandlerPneumaticCraft{

    @SubscribeEvent
    public void tickEnd(TickEvent.WorldTickEvent event){
        if(event.phase == TickEvent.Phase.END) {
            World world = event.world;
            checkLightning(world);
            DroneClaimManager.getInstance(world).update();
            if(!event.world.isRemote && event.world.provider.dimensionId == 0 && event.world.getWorldTime() % (24000 / AmadronOfferPeriodicConfig.timesPerDay) == 1) {
                AmadronOfferManager.getInstance().shufflePeriodicOffers();
            }
            if(!event.world.isRemote && event.world.getTotalWorldTime() % 100 == 0) {
                double tickTime = net.minecraft.util.MathHelper.average(MinecraftServer.getServer().tickTimeArray) * 1.0E-6D;//In case world are going to get their own thread: MinecraftServer.getServer().worldTickTimes.get(event.world.provider.dimensionId)
                NetworkHandler.sendToDimension(new PacketServerTickTime(tickTime), event.world.provider.dimensionId);
                if(event.world.getTotalWorldTime() % 600 == 0) AmadronOfferManager.getInstance().tryRestockCustomOffers();
            }
        }
    }

    private void checkLightning(World world){
        if(world.isRemote) return;

        for(int i = 0; i < world.weatherEffects.size(); i++) {
            Entity entity = (Entity)world.weatherEffects.get(i);
            if(entity.ticksExisted == 1 && entity instanceof EntityLightningBolt) {
                handleElectrostaticGeneration(world, entity);
            }
        }
    }

    private void handleElectrostaticGeneration(World world, Entity entity){
        List<int[]> coordList = new ArrayList<int[]>();
        getElectrostaticGrid(coordList, world, (int)Math.round(entity.posX), (int)Math.round(entity.posY), (int)Math.round(entity.posZ));
        List<TileEntityElectrostaticCompressor> compressors = new ArrayList<TileEntityElectrostaticCompressor>();
        for(int[] coord : coordList) {
            if(world.getBlock(coord[0], coord[1], coord[2]) == Blockss.electrostaticCompressor) {
                TileEntity te = world.getTileEntity(coord[0], coord[1], coord[2]);
                if(te instanceof TileEntityElectrostaticCompressor) {
                    compressors.add((TileEntityElectrostaticCompressor)te);
                }
            }
        }
        for(TileEntityElectrostaticCompressor compressor : compressors) {
            compressor.addAir(PneumaticValues.PRODUCTION_ELECTROSTATIC_COMPRESSOR / compressors.size(), ForgeDirection.UNKNOWN);
            compressor.onStruckByLightning();
        }
    }

    /**
     * All Iron bar blocks blocks will be added to the given arraylist of coordinates. This method
     * will be recursively called until the whole grid of iron bars is on the list.
     * @param list
     * @param world
     * @param x
     * @param y
     * @param z
     */
    public static void getElectrostaticGrid(List<int[]> list, World world, int x, int y, int z){
        for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
            if((world.getBlock(x + d.offsetX, y + d.offsetY, z + d.offsetZ) == net.minecraft.init.Blocks.iron_bars || world.getBlock(x + d.offsetX, y + d.offsetY, z + d.offsetZ) == Blockss.electrostaticCompressor) && !listContainsCoord(list, x + d.offsetX, y + d.offsetY, z + d.offsetZ)) {
                int[] coord = {x + d.offsetX, y + d.offsetY, z + d.offsetZ};
                list.add(coord);
                getElectrostaticGrid(list, world, x + d.offsetX, y + d.offsetY, z + d.offsetZ);
            }
        }
    }

    private static boolean listContainsCoord(List<int[]> list, int x, int y, int z){
        for(int i = 0; i < list.size(); i++) {
            int[] coord = list.get(i);
            if(coord[0] == x && coord[1] == y && coord[2] == z) return true;
        }
        return false;
    }

}
