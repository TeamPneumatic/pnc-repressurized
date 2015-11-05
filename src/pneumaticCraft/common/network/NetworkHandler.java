/*
 * This file is part of Blue Power.
 *
 *     Blue Power is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Blue Power is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Blue Power.  If not, see <http://www.gnu.org/licenses/>
 */

package pneumaticCraft.common.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class NetworkHandler{

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("PneumaticCraft");
    public static int discriminant;

    /*
     * The integer is the ID of the message, the Side is the side this message will be handled (received) on!
     */
    public static void init(){
        new DescPacketHandler();
        INSTANCE.registerMessage(PacketAddChatMessage.class, PacketAddChatMessage.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketAphorismTileUpdate.class, PacketAphorismTileUpdate.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketChangeGPSToolCoordinate.class, PacketChangeGPSToolCoordinate.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketCoordTrackUpdate.class, PacketCoordTrackUpdate.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketDescription.class, PacketDescription.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketDescriptionPacketRequest.class, PacketDescriptionPacketRequest.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketGuiButton.class, PacketGuiButton.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketPlaySound.class, PacketPlaySound.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketProgrammerUpdate.class, PacketProgrammerUpdate.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketRenderRangeLines.class, PacketRenderRangeLines.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketSecurityStationAddHacker.class, PacketSecurityStationAddHacker.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketSecurityStationAddUser.class, PacketSecurityStationAddUser.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketSecurityStationFailedHack.class, PacketSecurityStationFailedHack.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketSendNBTPacket.class, PacketSendNBTPacket.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketSetMobTarget.class, PacketSetMobTarget.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketShowWireframe.class, PacketShowWireframe.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketSpawnParticle.class, PacketSpawnParticle.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketUpdateEntityFilter.class, PacketUpdateEntityFilter.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketUpdateSearchStack.class, PacketUpdateSearchStack.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketUpdateTextfield.class, PacketUpdateTextfield.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketUseItem.class, PacketUseItem.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketUpdatePressureModule.class, PacketUpdatePressureModule.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketUpdatePressureModule.class, PacketUpdatePressureModule.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketUpdateAirGrateModule.class, PacketUpdateAirGrateModule.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketUpdateAirGrateModule.class, PacketUpdateAirGrateModule.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketUpdateGui.class, PacketUpdateGui.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketUpdateRemoteLayout.class, PacketUpdateRemoteLayout.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketSetGlobalVariable.class, PacketSetGlobalVariable.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketSetGlobalVariable.class, PacketSetGlobalVariable.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketSetSemiBlock.class, PacketSetSemiBlock.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketSetLogisticsFilterStack.class, PacketSetLogisticsFilterStack.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketSetLogisticsFluidFilterStack.class, PacketSetLogisticsFluidFilterStack.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketServerTickTime.class, PacketServerTickTime.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketUpdatePressureBlock.class, PacketUpdatePressureBlock.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketSyncAmadronOffers.class, PacketSyncAmadronOffers.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketAmadronOrderUpdate.class, PacketAmadronOrderUpdate.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketAmadronTradeAdd.class, PacketAmadronTradeAdd.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketAmadronTradeAdd.class, PacketAmadronTradeAdd.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketAmadronTradeNotifyDeal.class, PacketAmadronTradeNotifyDeal.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketAmadronTradeRemoved.class, PacketAmadronTradeRemoved.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketUpdateLogisticModule.class, PacketUpdateLogisticModule.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketCommandGetGlobalVariableOutput.class, PacketCommandGetGlobalVariableOutput.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketNotifyVariablesRemote.class, PacketNotifyVariablesRemote.class, discriminant++, Side.CLIENT);

        INSTANCE.registerMessage(PacketHackingBlockStart.class, PacketHackingBlockStart.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketHackingBlockStart.class, PacketHackingBlockStart.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketHackingBlockFinish.class, PacketHackingBlockFinish.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketHackingEntityStart.class, PacketHackingEntityStart.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketHackingEntityStart.class, PacketHackingEntityStart.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketHackingEntityFinish.class, PacketHackingEntityFinish.class, discriminant++, Side.CLIENT);

        INSTANCE.registerMessage(PacketToggleHelmetFeature.class, PacketToggleHelmetFeature.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketUpdateDebuggingDrone.class, PacketUpdateDebuggingDrone.class, discriminant++, Side.SERVER);
        INSTANCE.registerMessage(PacketSendDroneDebugEntry.class, PacketSendDroneDebugEntry.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketSyncDroneEntityProgWidgets.class, PacketSyncDroneEntityProgWidgets.class, discriminant++, Side.CLIENT);

        INSTANCE.registerMessage(PacketOpenTubeModuleGui.class, PacketOpenTubeModuleGui.class, discriminant++, Side.CLIENT);

        INSTANCE.registerMessage(PacketSpawnRing.class, PacketSpawnRing.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketShowArea.class, PacketShowArea.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketSetEntityMotion.class, PacketSetEntityMotion.class, discriminant++, Side.CLIENT);
        INSTANCE.registerMessage(PacketDebugBlock.class, PacketDebugBlock.class, discriminant++, Side.CLIENT);
    }

    /* public static void INSTANCE.registerMessage(Class<? extends AbstractPacket<? extends IMessage>> clazz){
         INSTANCE.registerMessage(clazz, clazz, discriminant++, Side.SERVER, discriminant++, Side.SERVER);
     }*/

    public static void sendToAll(IMessage message){
        INSTANCE.sendToAll(message);
    }

    public static void sendTo(IMessage message, EntityPlayerMP player){
        INSTANCE.sendTo(message, player);
    }

    public static void sendToAllAround(LocationIntPacket message, World world, double distance){
        sendToAllAround(message, message.getTargetPoint(world, distance));
    }

    public static void sendToAllAround(LocationIntPacket message, World world){
        sendToAllAround(message, message.getTargetPoint(world));
    }

    public static void sendToAllAround(LocationDoublePacket message, World world){
        sendToAllAround(message, message.getTargetPoint(world));
    }

    public static void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point){
        INSTANCE.sendToAllAround(message, point);
    }

    public static void sendToDimension(IMessage message, int dimensionId){
        INSTANCE.sendToDimension(message, dimensionId);
    }

    public static void sendToServer(IMessage message){
        INSTANCE.sendToServer(message);
    }
}
