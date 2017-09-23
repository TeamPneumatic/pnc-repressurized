package me.desht.pneumaticcraft.common;

import me.desht.pneumaticcraft.common.ai.DroneClaimManager;
import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.config.AmadronOfferPeriodicConfig;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketServerTickTime;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElectrostaticCompressor;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TickHandlerPneumaticCraft {

    @SubscribeEvent
    public void tickEnd(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            World world = event.world;
            checkLightning(world);
            DroneClaimManager.getInstance(world).update();
            if (!event.world.isRemote && event.world.provider.getDimension() == 0 && event.world.getWorldTime() % (24000 / AmadronOfferPeriodicConfig.timesPerDay) == 1) {
                AmadronOfferManager.getInstance().shufflePeriodicOffers();
            }
            if (!event.world.isRemote && event.world.getTotalWorldTime() % 100 == 0) {
                double tickTime = net.minecraft.util.math.MathHelper.average(FMLCommonHandler.instance().getMinecraftServerInstance().tickTimeArray) * 1.0E-6D;//In case world are going to get their own thread: MinecraftServer.getServer().worldTickTimes.get(event.world.provider.getDimension())
                NetworkHandler.sendToDimension(new PacketServerTickTime(tickTime), event.world.provider.getDimension());
                if (event.world.getTotalWorldTime() % 600 == 0)
                    AmadronOfferManager.getInstance().tryRestockCustomOffers();
            }
        }
    }

    private void checkLightning(World world) {
        if (world.isRemote) return;

        for (int i = 0; i < world.weatherEffects.size(); i++) {
            Entity entity = world.weatherEffects.get(i);
            if (entity.ticksExisted == 1 && entity instanceof EntityLightningBolt) {
                handleElectrostaticGeneration(world, entity);
            }
        }
    }

    //TODO 1.8 test Electrostatic compressor
    private void handleElectrostaticGeneration(World world, Entity entity) {
        Set<BlockPos> posList = new HashSet<BlockPos>();
        getElectrostaticGrid(posList, world, new BlockPos(Math.round(entity.posX), Math.round(entity.posY), Math.round(entity.posZ)));
        List<TileEntityElectrostaticCompressor> compressors = new ArrayList<TileEntityElectrostaticCompressor>();
        for (BlockPos pos : posList) {
            if (world.getBlockState(pos).getBlock() == Blockss.ELECTROSTATIC_COMPRESSOR) {
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof TileEntityElectrostaticCompressor) {
                    compressors.add((TileEntityElectrostaticCompressor) te);
                }
            }
        }
        for (TileEntityElectrostaticCompressor compressor : compressors) {
            compressor.addAir(PneumaticValues.PRODUCTION_ELECTROSTATIC_COMPRESSOR / compressors.size());
            compressor.onStruckByLightning();
        }
    }

    /**
     * All Iron bar blocks blocks will be added to the given arraylist of coordinates. This method
     * will be recursively called until the whole grid of iron bars is on the list.
     *
     * @param set
     * @param world
     * @param pos
     */
    public static void getElectrostaticGrid(Set<BlockPos> set, World world, BlockPos pos) {
        for (EnumFacing d : EnumFacing.VALUES) {
            BlockPos newPos = pos.offset(d);
            Block block = world.getBlockState(newPos).getBlock();
            if ((block == Blocks.IRON_BARS || block == Blockss.ELECTROSTATIC_COMPRESSOR) && set.add(newPos)) {
                getElectrostaticGrid(set, world, newPos);
            }
        }
    }
}
