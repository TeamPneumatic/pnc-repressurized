/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

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

package me.desht.pneumaticcraft.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;
import static net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT;
import static net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "9";
    private static final SimpleChannel NETWORK = NetworkRegistry.ChannelBuilder
            .named(RL("main_channel"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();
    private static int det = 0;

    private static int nextId() {
        return det++;
    }

    public static void init() {
		registerMessage(PacketAphorismTileUpdate.class,
				PacketAphorismTileUpdate::toBytes, PacketAphorismTileUpdate::new, PacketAphorismTileUpdate::handle);
		registerMessage(PacketChangeGPSToolCoordinate.class,
				PacketChangeGPSToolCoordinate::toBytes, PacketChangeGPSToolCoordinate::new, PacketChangeGPSToolCoordinate::handle, PLAY_TO_SERVER);
		registerMessage(PacketUpdateGPSAreaTool.class,
				PacketUpdateGPSAreaTool::toBytes, PacketUpdateGPSAreaTool::new, PacketUpdateGPSAreaTool::handle, PLAY_TO_SERVER);
		registerMessage(PacketDescription.class,
				PacketDescription::toBytes, PacketDescription::new, PacketDescription::process, PLAY_TO_CLIENT);
		registerMessage(PacketDescriptionPacketRequest.class,
				PacketDescriptionPacketRequest::toBytes, PacketDescriptionPacketRequest::new, PacketDescriptionPacketRequest::handle, PLAY_TO_SERVER);
		registerMessage(PacketGuiButton.class,
				PacketGuiButton::toBytes, PacketGuiButton::new, PacketGuiButton::handle, PLAY_TO_SERVER);
		registerMessage(PacketPlaySound.class,
				PacketPlaySound::toBytes, PacketPlaySound::new, PacketPlaySound::handle, PLAY_TO_CLIENT);
		registerMessage(PacketProgrammerUpdate.class,
				PacketProgrammerUpdate::toBytes, PacketProgrammerUpdate::new, PacketProgrammerUpdate::handle);
		registerMessage(PacketSendNBTPacket.class,
				PacketSendNBTPacket::toBytes, PacketSendNBTPacket::new, PacketSendNBTPacket::handle, PLAY_TO_CLIENT);
		registerMessage(PacketShowWireframe.class,
				PacketShowWireframe::toBytes, PacketShowWireframe::new, PacketShowWireframe::handle);
		registerMessage(PacketSpawnParticle.class,
				PacketSpawnParticle::toBytes, PacketSpawnParticle::new, PacketSpawnParticle::handle, PLAY_TO_CLIENT);
		registerMessage(PacketSpawnParticleTrail.class,
				PacketSpawnParticleTrail::toBytes, PacketSpawnParticleTrail::new, PacketSpawnParticleTrail::handle, PLAY_TO_CLIENT);
		registerMessage(PacketSpawnIndicatorParticles.class,
				PacketSpawnIndicatorParticles::toBytes, PacketSpawnIndicatorParticles::new, PacketSpawnIndicatorParticles::handle, PLAY_TO_CLIENT);
		registerMessage(PacketUpdateSearchItem.class,
				PacketUpdateSearchItem::toBytes, PacketUpdateSearchItem::new, PacketUpdateSearchItem::handle, PLAY_TO_SERVER);
		registerMessage(PacketUpdateTextfield.class,
				PacketUpdateTextfield::toBytes, PacketUpdateTextfield::new, PacketUpdateTextfield::handle, PLAY_TO_SERVER);
		registerMessage(PacketUpdatePressureModule.class,
				PacketUpdatePressureModule::toBytes, PacketUpdatePressureModule::new, PacketUpdatePressureModule::handle, PLAY_TO_SERVER);
		registerMessage(PacketUpdateAirGrateModule.class,
				PacketUpdateAirGrateModule::toBytes, PacketUpdateAirGrateModule::new, PacketUpdateAirGrateModule::handle, PLAY_TO_SERVER);
		registerMessage(PacketUpdateGui.class,
				PacketUpdateGui::toBytes, PacketUpdateGui::new, PacketUpdateGui::handle, PLAY_TO_CLIENT);
		registerMessage(PacketUpdateRemoteLayout.class,
				PacketUpdateRemoteLayout::toBytes, PacketUpdateRemoteLayout::new, PacketUpdateRemoteLayout::handle, PLAY_TO_SERVER);
		registerMessage(PacketSetGlobalVariable.class,
				PacketSetGlobalVariable::toBytes, PacketSetGlobalVariable::new, PacketSetGlobalVariable::handle);
		registerMessage(PacketServerTickTime.class,
				PacketServerTickTime::toBytes, PacketServerTickTime::new, PacketServerTickTime::handle, PLAY_TO_CLIENT);
		registerMessage(PacketUpdatePressureBlock.class,
				PacketUpdatePressureBlock::toBytes, PacketUpdatePressureBlock::new, PacketUpdatePressureBlock::handle, PLAY_TO_CLIENT);
		registerMessage(PacketSyncAmadronOffers.class,
				PacketSyncAmadronOffers::toBytes, PacketSyncAmadronOffers::new, PacketSyncAmadronOffers::handle, PLAY_TO_CLIENT);
		registerMessage(PacketAmadronOrderResponse.class,
				PacketAmadronOrderResponse::toBytes, PacketAmadronOrderResponse::new, PacketAmadronOrderResponse::handle, PLAY_TO_CLIENT);
		registerMessage(PacketAmadronOrderUpdate.class,
				PacketAmadronOrderUpdate::toBytes, PacketAmadronOrderUpdate::new, PacketAmadronOrderUpdate::handle, PLAY_TO_SERVER);
		registerMessage(PacketAmadronStockUpdate.class,
				PacketAmadronStockUpdate::toBytes, PacketAmadronStockUpdate::new, PacketAmadronStockUpdate::handle, PLAY_TO_CLIENT);
		registerMessage(PacketAmadronTradeAddCustom.class,
				PacketAmadronTradeAddCustom::toBytes, PacketAmadronTradeAddCustom::new, PacketAmadronTradeAddCustom::handle);
		registerMessage(PacketAmadronTradeNotifyDeal.class,
				PacketAmadronTradeNotifyDeal::toBytes, PacketAmadronTradeNotifyDeal::new, PacketAmadronTradeNotifyDeal::handle, PLAY_TO_CLIENT);
		registerMessage(PacketAmadronTradeRemoved.class,
				PacketAmadronTradeRemoved::toBytes, PacketAmadronTradeRemoved::new, PacketAmadronTradeRemoved::handle, PLAY_TO_CLIENT);
		registerMessage(PacketUpdateLogisticsModule.class,
				PacketUpdateLogisticsModule::toBytes, PacketUpdateLogisticsModule::new, PacketUpdateLogisticsModule::handle, PLAY_TO_CLIENT);
		registerMessage(PacketTubeModuleColor.class,
				PacketTubeModuleColor::toBytes, PacketTubeModuleColor::new, PacketTubeModuleColor::handle, PLAY_TO_SERVER);
		registerMessage(PacketSyncRedstoneModuleToClient.class,
				PacketSyncRedstoneModuleToClient::toBytes, PacketSyncRedstoneModuleToClient::new, PacketSyncRedstoneModuleToClient::handle, PLAY_TO_CLIENT);
		registerMessage(PacketSyncRedstoneModuleToServer.class,
				PacketSyncRedstoneModuleToServer::toBytes, PacketSyncRedstoneModuleToServer::new, PacketSyncRedstoneModuleToServer::handle, PLAY_TO_SERVER);
		registerMessage(PacketSyncThermostatModuleToClient.class,
				PacketSyncThermostatModuleToClient::toBytes, PacketSyncThermostatModuleToClient::new, PacketSyncThermostatModuleToClient::handle, PLAY_TO_CLIENT);
		registerMessage(PacketSyncThermostatModuleToServer.class,
				PacketSyncThermostatModuleToServer::toBytes, PacketSyncThermostatModuleToServer::new, PacketSyncThermostatModuleToServer::handle, PLAY_TO_SERVER);
		registerMessage(PacketHackingBlockStart.class,
				PacketHackingBlockStart::toBytes, PacketHackingBlockStart::new, PacketHackingBlockStart::handle);
		registerMessage(PacketHackingBlockFinish.class,
				PacketHackingBlockFinish::toBytes, PacketHackingBlockFinish::new, PacketHackingBlockFinish::handle, PLAY_TO_CLIENT);
		registerMessage(PacketHackingEntityStart.class,
				PacketHackingEntityStart::toBytes, PacketHackingEntityStart::new, PacketHackingEntityStart::handle);
		registerMessage(PacketHackingEntityFinish.class,
				PacketHackingEntityFinish::toBytes, PacketHackingEntityFinish::new, PacketHackingEntityFinish::handle, PLAY_TO_CLIENT);
		registerMessage(PacketToggleArmorFeature.class,
				PacketToggleArmorFeature::toBytes, PacketToggleArmorFeature::new, PacketToggleArmorFeature::handle);
		registerMessage(PacketToggleArmorFeatureBulk.class,
				PacketToggleArmorFeatureBulk::toBytes, PacketToggleArmorFeatureBulk::new, PacketToggleArmorFeatureBulk::handle, PLAY_TO_SERVER);
		registerMessage(PacketUpdateDebuggingDrone.class,
				PacketUpdateDebuggingDrone::toBytes, PacketUpdateDebuggingDrone::new, PacketUpdateDebuggingDrone::handle, PLAY_TO_SERVER);
		registerMessage(PacketSendDroneDebugEntry.class,
				PacketSendDroneDebugEntry::toBytes, PacketSendDroneDebugEntry::new, PacketSendDroneDebugEntry::handle, PLAY_TO_CLIENT);
		registerMessage(PacketSyncDroneEntityProgWidgets.class,
				PacketSyncDroneEntityProgWidgets::toBytes, PacketSyncDroneEntityProgWidgets::new, PacketSyncDroneEntityProgWidgets::handle, PLAY_TO_CLIENT);
		registerMessage(PacketSpawnRing.class,
				PacketSpawnRing::toBytes, PacketSpawnRing::new, PacketSpawnRing::handle, PLAY_TO_CLIENT);
		registerMessage(PacketShowArea.class,
				PacketShowArea::toBytes, PacketShowArea::new, PacketShowArea::handle, PLAY_TO_CLIENT);
		registerMessage(PacketSetEntityMotion.class,
				PacketSetEntityMotion::toBytes, PacketSetEntityMotion::new, PacketSetEntityMotion::handle, PLAY_TO_CLIENT);
		registerMessage(PacketDebugBlock.class,
				PacketDebugBlock::toBytes, PacketDebugBlock::new, PacketDebugBlock::handle, PLAY_TO_CLIENT);
		registerMessage(PacketMultiHeader.class,
				PacketMultiHeader::toBytes, PacketMultiHeader::new, PacketMultiHeader::handle);
		registerMessage(PacketMultiPart.class,
				PacketMultiPart::toBytes, PacketMultiPart::new, PacketMultiPart::handle);
		registerMessage(PacketPneumaticKick.class,
				PacketPneumaticKick::toBytes, PacketPneumaticKick::new, PacketPneumaticKick::handle, PLAY_TO_SERVER);
		registerMessage(PacketJetBootsActivate.class,
				PacketJetBootsActivate::toBytes, PacketJetBootsActivate::new, PacketJetBootsActivate::handle, PLAY_TO_SERVER);
		registerMessage(PacketPlayMovingSound.class,
				PacketPlayMovingSound::toBytes, PacketPlayMovingSound::new, PacketPlayMovingSound::handle, PLAY_TO_CLIENT);
		registerMessage(PacketJetBootsStateSync.class,
				PacketJetBootsStateSync::toBytes, PacketJetBootsStateSync::new, PacketJetBootsStateSync::handle, PLAY_TO_CLIENT);
		registerMessage(PacketModWrenchBlock.class,
				PacketModWrenchBlock::toBytes, PacketModWrenchBlock::new, PacketModWrenchBlock::handle, PLAY_TO_SERVER);
		registerMessage(PacketUpdateArmorExtraData.class,
				PacketUpdateArmorExtraData::toBytes, PacketUpdateArmorExtraData::new, PacketUpdateArmorExtraData::handle, PLAY_TO_SERVER);
		registerMessage(PacketUpdateMicromissileSettings.class,
				PacketUpdateMicromissileSettings::toBytes, PacketUpdateMicromissileSettings::new, PacketUpdateMicromissileSettings::handle, PLAY_TO_SERVER);
		registerMessage(PacketSendArmorHUDMessage.class,
				PacketSendArmorHUDMessage::toBytes, PacketSendArmorHUDMessage::new, PacketSendArmorHUDMessage::handle, PLAY_TO_CLIENT);
		registerMessage(PacketChestplateLauncher.class,
				PacketChestplateLauncher::toBytes, PacketChestplateLauncher::new, PacketChestplateLauncher::handle, PLAY_TO_SERVER);
		registerMessage(PacketSyncSemiblock.class,
				PacketSyncSemiblock::toBytes, PacketSyncSemiblock::new, PacketSyncSemiblock::handle);
		registerMessage(PacketSyncSmartChest.class,
				PacketSyncSmartChest::toBytes, PacketSyncSmartChest::new, PacketSyncSmartChest::handle);
		registerMessage(PacketClearRecipeCache.class,
				PacketClearRecipeCache::toBytes, PacketClearRecipeCache::new, PacketClearRecipeCache::handle, PLAY_TO_CLIENT);
		registerMessage(PacketLeftClickEmpty.class,
				PacketLeftClickEmpty::toBytes, PacketLeftClickEmpty::new, PacketLeftClickEmpty::handle, PLAY_TO_SERVER);
		registerMessage(PacketShiftScrollWheel.class,
				PacketShiftScrollWheel::toBytes, PacketShiftScrollWheel::new, PacketShiftScrollWheel::handle, PLAY_TO_SERVER);
		registerMessage(PacketNotifyBlockUpdate.class,
				PacketNotifyBlockUpdate::toBytes, PacketNotifyBlockUpdate::new, PacketNotifyBlockUpdate::handle, PLAY_TO_CLIENT);
		registerMessage(PacketSyncHackSimulationUpdate.class,
				PacketSyncHackSimulationUpdate::toBytes, PacketSyncHackSimulationUpdate::new, PacketSyncHackSimulationUpdate::handle, PLAY_TO_CLIENT);
		registerMessage(PacketUpdateArmorColors.class,
				PacketUpdateArmorColors::toBytes, PacketUpdateArmorColors::new, PacketUpdateArmorColors::handle, PLAY_TO_SERVER);
		registerMessage(PacketMinigunStop.class,
				PacketMinigunStop::toBytes, PacketMinigunStop::new, PacketMinigunStop::handle, PLAY_TO_CLIENT);
		registerMessage(PacketSyncClassifyFilter.class,
				PacketSyncClassifyFilter::toBytes, PacketSyncClassifyFilter::new, PacketSyncClassifyFilter::handle, PLAY_TO_SERVER);
		registerMessage(PacketSyncEntityHacks.class,
				PacketSyncEntityHacks::toBytes, PacketSyncEntityHacks::new, PacketSyncEntityHacks::handle, PLAY_TO_CLIENT);
		registerMessage(PacketTeleportCommand.class,
				PacketTeleportCommand::toBytes, PacketTeleportCommand::new, PacketTeleportCommand::handle, PLAY_TO_SERVER);
    }

	public static <MSG> void registerMessage(Class<MSG> messageType, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
		NETWORK.registerMessage(nextId(), messageType, encoder, decoder, messageConsumer);
	}

	public static <MSG> void registerMessage(Class<MSG> messageType, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer, NetworkDirection direction) {
		NETWORK.registerMessage(nextId(), messageType, encoder, decoder, messageConsumer, Optional.of(direction));
	}

    public static void sendToAll(Object message) {
		sendMessage(message, msg -> NETWORK.send(PacketDistributor.ALL.noArg(), msg));
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
		sendMessage(message, msg -> NETWORK.send(PacketDistributor.PLAYER.with(() -> player), msg));
    }

	public static void sendToAllTracking(Object message, Entity entity) {
    	sendMessage(message, msg -> NETWORK.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), msg));
	}

	public static void sendToAllTracking(Object message, Level world, BlockPos pos) {
		sendMessage(message, msg -> NETWORK.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)), msg));
	}

	public static void sendToAllTracking(Object message, BlockEntity te) {
    	if (te.getLevel() != null) {
    		sendToAllTracking(message, te.getLevel(), te.getBlockPos());
		}
    }

	public static void sendToDimension(Object message, ResourceKey<Level> world) {
		sendMessage(message, msg -> NETWORK.send(PacketDistributor.DIMENSION.with(() -> world), msg));
    }

    public static void sendToServer(Object message) {
		sendMessage(message, NETWORK::sendToServer);
    }

	/**
	 * Send a packet to all non-local players, which is everyone for a dedicated server, and everyone except the
	 * server owner for an integrated server.
	 * @param packet the packet to send
	 */
	public static void sendNonLocal(Object packet) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server != null) {
			if (server.isDedicatedServer()) {
				sendToAll(packet);
			} else {
				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					if (!player.server.isSingleplayerOwner(player.getGameProfile())) {
						sendToPlayer(packet, player);
					}
				}
			}
		}
	}

	/**
	 * Send a packet to the player, unless the player is local (i.e. player owner of the integrated server)
	 * @param player the player
	 * @param packet the packet to send
	 */
	public static void sendNonLocal(ServerPlayer player, Object packet) {
		if (!player.server.isSingleplayerOwner(player.getGameProfile())) {
			sendToPlayer(packet, player);
		}
	}

	private static void sendMessage(Object message, Consumer<Object> consumer) {
		if (message instanceof ILargePayload) {
			// see PacketMultiHeader#receivePayload for message reassembly
			FriendlyByteBuf buf = ((ILargePayload) message).dumpToBuffer();
			if (buf.writerIndex() < ILargePayload.MAX_PAYLOAD_SIZE) {
				consumer.accept(message);
			} else {
				List<Object> messageParts = new ArrayList<>();
				messageParts.add(new PacketMultiHeader(buf.writerIndex(), message.getClass().getName()));
				int offset = 0;
				byte[] bytes = buf.array();
				while (offset < buf.writerIndex()) {
					messageParts.add(new PacketMultiPart(Arrays.copyOfRange(bytes, offset, Math.min(offset + ILargePayload.MAX_PAYLOAD_SIZE, buf.writerIndex()))));
					offset += ILargePayload.MAX_PAYLOAD_SIZE;
				}
				messageParts.forEach(consumer);
			}
		} else {
			consumer.accept(message);
		}
	}
}
