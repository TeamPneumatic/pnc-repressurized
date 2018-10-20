package me.desht.pneumaticcraft.common.event;

import me.desht.pneumaticcraft.common.ai.DroneClaimManager;
import me.desht.pneumaticcraft.common.config.AmadronOfferPeriodicConfig;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketServerTickTime;
import me.desht.pneumaticcraft.common.recipes.AmadronOfferManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class TickHandlerPneumaticCraft {

    @SubscribeEvent
    public void onWorldTickEnd(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.world.isRemote) {
            World world = event.world;
//            checkLightning(world);
            DroneClaimManager.getInstance(world).update();
            if (event.world.getTotalWorldTime() % 100 == 0) {
                double tickTime = net.minecraft.util.math.MathHelper.average(FMLCommonHandler.instance().getMinecraftServerInstance().tickTimeArray) * 1.0E-6D;//In case world are going to get their own thread: MinecraftServer.getServer().worldTickTimes.get(event.world.provider.getDimension())
                NetworkHandler.sendToDimension(new PacketServerTickTime(tickTime), event.world.provider.getDimension());
            }
        }
    }

    @SubscribeEvent
    public void onServerTickEnd(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            int ticks = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
            if (ticks % (24000 / AmadronOfferPeriodicConfig.timesPerDay) == 1) {
                AmadronOfferManager.getInstance().shufflePeriodicOffers();
            }
            if (ticks % 600 == 0) {
                AmadronOfferManager.getInstance().tryRestockCustomOffers();
            }
        }
    }

//    private void checkLightning(World world) {
//        if (world.isRemote) return;
//
//        for (int i = 0; i < world.weatherEffects.size(); i++) {
//            Entity entity = world.weatherEffects.get(i);
//            if (entity.ticksExisted == 1 && entity instanceof EntityLightningBolt) {
//                handleElectrostaticGeneration(world, entity);
//            }
//        }
//    }
//
//    //TODO 1.8 test Electrostatic compressor
//    private void handleElectrostaticGeneration(World world, Entity entity) {
//        Set<BlockPos> posSet = new HashSet<>();
//        TileEntityElectrostaticCompressor.getElectrostaticGrid(posSet, world, new BlockPos(Math.round(entity.posX), Math.round(entity.posY), Math.round(entity.posZ)));
//        List<TileEntityElectrostaticCompressor> compressors = new ArrayList<>();
//        for (BlockPos pos : posSet) {
//            if (world.getBlockState(pos).getBlock() == Blockss.ELECTROSTATIC_COMPRESSOR) {
//                TileEntity te = world.getTileEntity(pos);
//                if (te instanceof TileEntityElectrostaticCompressor) {
//                    compressors.add((TileEntityElectrostaticCompressor) te);
//                }
//            }
//        }
//        for (TileEntityElectrostaticCompressor compressor : compressors) {
//            compressor.addAir(PneumaticValues.PRODUCTION_ELECTROSTATIC_COMPRESSOR / compressors.size());
//            compressor.onStruckByLightning();
//        }
//    }

}
