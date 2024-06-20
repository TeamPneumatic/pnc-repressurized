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

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.common.item.ClassifyFilterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.EnumSet;

@EventBusSubscriber(modid = PneumaticRegistry.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

	@SubscribeEvent
	public static void register(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar(PneumaticRegistry.MOD_ID)
				.versioned(PROTOCOL_VERSION);

		// misc serverbound
		registrar.playToServer(PacketAphorismTileUpdate.TYPE, PacketAphorismTileUpdate.STREAM_CODEC,
				PacketAphorismTileUpdate::handle);
		registrar.playToServer(PacketChangeGPSToolCoordinate.TYPE, PacketChangeGPSToolCoordinate.STREAM_CODEC,
				PacketChangeGPSToolCoordinate::handle);
		registrar.playToServer(PacketUpdateGPSAreaTool.TYPE, PacketUpdateGPSAreaTool.STREAM_CODEC,
				PacketUpdateGPSAreaTool::handle);
		registrar.playToServer(PacketDescriptionPacketRequest.TYPE, PacketDescriptionPacketRequest.STREAM_CODEC,
				PacketDescriptionPacketRequest::handle);
		registrar.playToServer(PacketGuiButton.TYPE, PacketGuiButton.STREAM_CODEC,
				PacketGuiButton::handle);
		registrar.playToServer(PacketUpdateTextfield.TYPE, PacketUpdateTextfield.STREAM_CODEC,
				PacketUpdateTextfield::handle);
		registrar.playToServer(PacketUpdateSearchItem.TYPE, PacketUpdateSearchItem.STREAM_CODEC,
				PacketUpdateSearchItem::handle);
		registrar.playToServer(PacketUpdateRemoteLayout.TYPE, PacketUpdateRemoteLayout.STREAM_CODEC,
				PacketUpdateRemoteLayout::handle);
		registrar.playToServer(PacketUpdateMicromissileSettings.TYPE, PacketUpdateMicromissileSettings.STREAM_CODEC,
				PacketUpdateMicromissileSettings::handle);
		registrar.playToServer(PacketModWrenchBlock.TYPE, PacketModWrenchBlock.STREAM_CODEC,
				PacketModWrenchBlock::handle);
		registrar.playToServer(PacketLeftClickEmpty.TYPE, PacketLeftClickEmpty.STREAM_CODEC,
				PacketLeftClickEmpty::handle);
		registrar.playToServer(PacketShiftScrollWheel.TYPE, PacketShiftScrollWheel.STREAM_CODEC,
				PacketShiftScrollWheel::handle);
		registrar.playToServer(PacketSyncClassifyFilter.TYPE, PacketSyncClassifyFilter.STREAM_CODEC,
				PacketSyncClassifyFilter::handle);
		registrar.playToServer(PacketTeleportCommand.TYPE, PacketTeleportCommand.STREAM_CODEC,
				PacketTeleportCommand::handle);

		// misc clientbound
		registrar.playToClient(PacketDescription.TYPE, PacketDescription.STREAM_CODEC,
				PacketDescription::handle);
		registrar.playToClient(PacketPlaySound.TYPE, PacketPlaySound.STREAM_CODEC,
				PacketPlaySound::handle);
		registrar.playToClient(PacketSendNBTPacket.TYPE, PacketSendNBTPacket.STREAM_CODEC,
				PacketSendNBTPacket::handle);
		registrar.playToClient(PacketSpawnParticle.TYPE, PacketSpawnParticle.STREAM_CODEC,
				PacketSpawnParticle::handle);
		registrar.playToClient(PacketSpawnParticleTrail.TYPE, PacketSpawnParticleTrail.STREAM_CODEC,
				PacketSpawnParticleTrail::handle);
		registrar.playToClient(PacketSpawnIndicatorParticles.TYPE, PacketSpawnIndicatorParticles.STREAM_CODEC,
				PacketSpawnIndicatorParticles::handle);
		registrar.playToClient(PacketUpdatePressureBlock.TYPE, PacketUpdatePressureBlock.STREAM_CODEC,
				PacketUpdatePressureBlock::handle);
		registrar.playToClient(PacketUpdateGui.TYPE, PacketUpdateGui.STREAM_CODEC,
				PacketUpdateGui::handle);
		registrar.playToClient(PacketServerTickTime.TYPE, PacketServerTickTime.STREAM_CODEC,
				PacketServerTickTime::handle);
		registrar.playToClient(PacketSpawnRing.TYPE, PacketSpawnRing.STREAM_CODEC,
				PacketSpawnRing::handle);
		registrar.playToClient(PacketShowArea.TYPE, PacketShowArea.STREAM_CODEC,
				PacketShowArea::handle);
		registrar.playToClient(PacketSetEntityMotion.TYPE, PacketSetEntityMotion.STREAM_CODEC,
				PacketSetEntityMotion::handle);
		registrar.playToClient(PacketDebugBlock.TYPE, PacketDebugBlock.STREAM_CODEC,
				PacketDebugBlock::handle);
		registrar.playToClient(PacketPlayMovingSound.TYPE, PacketPlayMovingSound.STREAM_CODEC,
				PacketPlayMovingSound::handle);
		registrar.playToClient(PacketNotifyBlockUpdate.TYPE, PacketNotifyBlockUpdate.STREAM_CODEC,
				PacketNotifyBlockUpdate::handle);
		registrar.playToClient(PacketMinigunStop.TYPE, PacketMinigunStop.STREAM_CODEC,
				PacketMinigunStop::handle);
		registrar.playToClient(PacketClearRecipeCache.TYPE, PacketClearRecipeCache.STREAM_CODEC,
				PacketClearRecipeCache::handle);
		
		// misc bi-directional
		registrar.playBidirectional(PacketSetGlobalVariable.TYPE, PacketSetGlobalVariable.STREAM_CODEC,
				PacketSetGlobalVariable::handle);
		registrar.playBidirectional(PacketSyncSemiblock.TYPE, PacketSyncSemiblock.STREAM_CODEC,
				PacketSyncSemiblock::handle);
		registrar.playBidirectional(PacketSyncSmartChest.TYPE, PacketSyncSmartChest.STREAM_CODEC,
				PacketSyncSmartChest::handle);

		// tube modules
		registrar.playToServer(PacketUpdatePressureModule.TYPE, PacketUpdatePressureModule.STREAM_CODEC,
				TubeModulePacket::handle);
		registrar.playToServer(PacketUpdateAirGrateModule.TYPE, PacketUpdateAirGrateModule.STREAM_CODEC,
				TubeModulePacket::handle);
		registrar.playToServer(PacketTubeModuleColor.TYPE, PacketTubeModuleColor.STREAM_CODEC,
				TubeModulePacket::handle);
		registrar.playToServer(PacketSyncRedstoneModuleToServer.TYPE, PacketSyncRedstoneModuleToServer.STREAM_CODEC,
				TubeModulePacket::handle);
		registrar.playToClient(PacketUpdateLogisticsModule.TYPE, PacketUpdateLogisticsModule.STREAM_CODEC,
				TubeModulePacket::handle);
		registrar.playToClient(PacketSyncRedstoneModuleToClient.TYPE, PacketSyncRedstoneModuleToClient.STREAM_CODEC,
				TubeModulePacket::handle);
		registrar.playToClient(PacketSyncThermostatModuleToClient.TYPE, PacketSyncThermostatModuleToClient.STREAM_CODEC,
				TubeModulePacket::handle);
		registrar.playToServer(PacketSyncThermostatModuleToServer.TYPE, PacketSyncThermostatModuleToServer.STREAM_CODEC,
				TubeModulePacket::handle);

		// amadron
		registrar.playBidirectional(PacketSyncAmadronOffers.TYPE, PacketSyncAmadronOffers.STREAM_CODEC,
				PacketSyncAmadronOffers::handle);
		registrar.playToClient(PacketAmadronOrderResponse.TYPE, PacketAmadronOrderResponse.STREAM_CODEC,
				PacketAmadronOrderResponse::handle);
		registrar.playToServer(PacketAmadronOrderUpdate.TYPE, PacketAmadronOrderUpdate.STREAM_CODEC,
				PacketAmadronOrderUpdate::handle);
		registrar.playToClient(PacketAmadronStockUpdate.TYPE, PacketAmadronStockUpdate.STREAM_CODEC,
				PacketAmadronStockUpdate::handle);
		registrar.playToClient(PacketAmadronTradeNotifyDeal.TYPE, PacketAmadronTradeNotifyDeal.STREAM_CODEC,
				PacketAmadronTradeNotifyDeal::handle);
		registrar.playToClient(PacketAmadronTradeRemoved.TYPE, PacketAmadronTradeRemoved.STREAM_CODEC,
				PacketAmadronTradeRemoved::handle);
		registrar.playBidirectional(PacketAmadronTradeAddCustom.TYPE, PacketAmadronTradeAddCustom.STREAM_CODEC,
				PacketAmadronTradeAddCustom::handle);

		// hacking
		registrar.playBidirectional(PacketHackingBlockStart.TYPE, PacketHackingBlockStart.STREAM_CODEC,
				PacketHackingBlockStart::handle);
		registrar.playBidirectional(PacketHackingEntityStart.TYPE, PacketHackingEntityStart.STREAM_CODEC,
				PacketHackingEntityStart::handle);
		registrar.playToClient(PacketHackingBlockFinish.TYPE, PacketHackingBlockFinish.STREAM_CODEC,
				PacketHackingBlockFinish::handle);
		registrar.playToClient(PacketHackingEntityFinish.TYPE, PacketHackingEntityFinish.STREAM_CODEC,
				PacketHackingEntityFinish::handle);
		registrar.playToClient(PacketSyncHackSimulationUpdate.TYPE, PacketSyncHackSimulationUpdate.STREAM_CODEC,
				PacketSyncHackSimulationUpdate::handle);
		registrar.playToClient(PacketSyncEntityHacks.TYPE, PacketSyncEntityHacks.STREAM_CODEC,
				PacketSyncEntityHacks::handle);

		// pneumatic armor
		registrar.playBidirectional(PacketToggleArmorFeature.TYPE, PacketToggleArmorFeature.STREAM_CODEC,
				PacketToggleArmorFeature::handle);
		registrar.playToServer(PacketToggleArmorFeatureBulk.TYPE, PacketToggleArmorFeatureBulk.STREAM_CODEC,
				PacketToggleArmorFeatureBulk::handle);
		registrar.playToServer(PacketPneumaticKick.TYPE, PacketPneumaticKick.STREAM_CODEC,
				PacketPneumaticKick::handle);
		registrar.playToServer(PacketJetBootsActivate.TYPE, PacketJetBootsActivate.STREAM_CODEC,
				PacketJetBootsActivate::handle);
		registrar.playToServer(PacketChestplateLauncher.TYPE, PacketChestplateLauncher.STREAM_CODEC,
				PacketChestplateLauncher::handle);
		registrar.playToServer(PacketUpdateArmorColors.TYPE, PacketUpdateArmorColors.STREAM_CODEC,
				PacketUpdateArmorColors::handle);
		registrar.playToServer(PacketUpdateArmorExtraData.TYPE, PacketUpdateArmorExtraData.STREAM_CODEC,
				PacketUpdateArmorExtraData::handle);
		registrar.playToClient(PacketJetBootsStateSync.TYPE, PacketJetBootsStateSync.STREAM_CODEC,
				PacketJetBootsStateSync::handle);
		registrar.playToClient(PacketSendArmorHUDMessage.TYPE, PacketSendArmorHUDMessage.STREAM_CODEC,
				PacketSendArmorHUDMessage::handle);

		// drones
		registrar.playToServer(PacketUpdateDebuggingDrone.TYPE, PacketUpdateDebuggingDrone.STREAM_CODEC,
				DronePacket::handle);
		registrar.playToClient(PacketSendDroneDebugEntry.TYPE, PacketSendDroneDebugEntry.STREAM_CODEC,
				DronePacket::handle);
		registrar.playToClient(PacketSyncDroneProgWidgets.TYPE, PacketSyncDroneProgWidgets.STREAM_CODEC,
				DronePacket::handle);
		registrar.playToClient(PacketShowWireframe.TYPE, PacketShowWireframe.STREAM_CODEC,
				PacketShowWireframe::handle);
		registrar.playBidirectional(PacketProgrammerSync.TYPE, PacketProgrammerSync.STREAM_CODEC,
				 PacketProgrammerSync::handle);
	}

    public static void sendToAll(CustomPacketPayload message) {
		PacketDistributor.sendToAllPlayers(message);
    }

    public static void sendToPlayer(CustomPacketPayload message, ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, message);
    }

	public static void sendToAllTracking(CustomPacketPayload message, Entity entity) {
		PacketDistributor.sendToPlayersTrackingEntity(entity, message);
	}

	public static void sendToAllTracking(CustomPacketPayload message, Level level, BlockPos pos) {
		if (level instanceof ServerLevel serverLevel) {
			PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(pos), message);
		}
	}

	public static void sendToAllTracking(CustomPacketPayload message, BlockEntity te) {
    	if (te.getLevel() != null) {
    		sendToAllTracking(message, te.getLevel(), te.getBlockPos());
		}
    }

	public static void sendToDimension(CustomPacketPayload message, ServerLevel level) {
		PacketDistributor.sendToPlayersInDimension(level, message);
    }

    public static void sendToServer(CustomPacketPayload message) {
		PacketDistributor.sendToServer(message);
    }

	/**
	 * Send a packet to all non-local players, which is everyone for a dedicated server, and everyone except the
	 * server owner for an integrated server.
	 * @param packet the packet to send
	 */
	public static void sendNonLocal(CustomPacketPayload packet) {
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
	public static void sendNonLocal(ServerPlayer player, CustomPacketPayload packet) {
		if (!player.server.isSingleplayerOwner(player.getGameProfile())) {
			sendToPlayer(packet, player);
		}
	}

//
//	/**
//	 * Send a packet to the player, unless the player is local (i.e. player owner of the integrated server)
//	 * @param player the player
//	 * @param packet the packet to send
//	 */
//	public static void sendNonLocal(ServerPlayer player, CustomPacketPayload packet) {
//		if (!player.server.isSingleplayerOwner(player.getGameProfile())) {
//			sendToPlayer(packet, player);
//		}
//	}
//
//	private static void sendMessage(CustomPacketPayload message, Consumer<CustomPacketPayload> consumer) {
//		if (message instanceof ILargePayload large) {
//			// see PacketMultiHeader#receivePayload for message reassembly
//			RegistryFriendlyByteBuf buf = large.dumpToBuffer();
//			if (buf.writerIndex() < ILargePayload.MAX_PAYLOAD_SIZE) {
//				consumer.accept(message);
//			} else {
//				List<CustomPacketPayload> messageParts = new ArrayList<>();
//				messageParts.add(new PacketMultiHeader(buf.writerIndex(), message.getClass().getName()));
//				int offset = 0;
//				byte[] bytes = buf.array();
//				while (offset < buf.writerIndex()) {
//					messageParts.add(new PacketMultiPart(Arrays.copyOfRange(bytes, offset, Math.min(offset + ILargePayload.MAX_PAYLOAD_SIZE, buf.writerIndex()))));
//					offset += ILargePayload.MAX_PAYLOAD_SIZE;
//				}
//				messageParts.forEach(consumer);
//			}
//		} else {
//			consumer.accept(message);
//		}
//	}
}
