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
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = PneumaticRegistry.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

	@SubscribeEvent
	public static void register(final RegisterPayloadHandlerEvent event) {
		final IPayloadRegistrar registrar = event.registrar(PneumaticRegistry.MOD_ID)
				.versioned(PROTOCOL_VERSION);

		// misc serverbound
		registrar.play(PacketAphorismTileUpdate.ID, PacketAphorismTileUpdate::fromNetwork,
				handler -> handler.server(PacketAphorismTileUpdate::handle));
		registrar.play(PacketChangeGPSToolCoordinate.ID, PacketChangeGPSToolCoordinate::new,
				handler -> handler.server(PacketChangeGPSToolCoordinate::handle));
		registrar.play(PacketUpdateGPSAreaTool.ID, PacketUpdateGPSAreaTool::new,
				handler -> handler.server(PacketUpdateGPSAreaTool::handle));
		registrar.play(PacketDescriptionPacketRequest.ID, PacketDescriptionPacketRequest::new,
				handler -> handler.server(PacketDescriptionPacketRequest::handle));
		registrar.play(PacketGuiButton.ID, PacketGuiButton::new,
				handler -> handler.server(PacketGuiButton::handle));
		registrar.play(PacketUpdateTextfield.ID, PacketUpdateTextfield::fromNetwork,
				handler -> handler.server(PacketUpdateTextfield::handle));
		registrar.play(PacketUpdateSearchItem.ID, PacketUpdateSearchItem::fromNetwork,
				handler -> handler.server(PacketUpdateSearchItem::handle));
		registrar.play(PacketUpdateRemoteLayout.ID, PacketUpdateRemoteLayout::fromNetwork,
				handler -> handler.server(PacketUpdateRemoteLayout::handle));
		registrar.play(PacketUpdateMicromissileSettings.ID, PacketUpdateMicromissileSettings::fromNetwork,
				handler -> handler.server(PacketUpdateMicromissileSettings::handle));
		registrar.play(PacketModWrenchBlock.ID, PacketModWrenchBlock::fromNetwork,
				handler -> handler.server(PacketModWrenchBlock::handle));
		registrar.play(PacketLeftClickEmpty.ID, PacketLeftClickEmpty::fromNetwork,
				handler -> handler.server(PacketLeftClickEmpty::handle));
		registrar.play(PacketShiftScrollWheel.ID, PacketShiftScrollWheel::fromNetwork,
				handler -> handler.server(PacketShiftScrollWheel::handle));
		registrar.play(PacketSyncClassifyFilter.ID, PacketSyncClassifyFilter::fromNetwork,
				handler -> handler.server(PacketSyncClassifyFilter::handle));
		registrar.play(PacketTeleportCommand.ID, PacketTeleportCommand::fromNetwork,
				handler -> handler.server(PacketTeleportCommand::handle));

		// misc clientbound
		registrar.play(PacketDescription.ID, PacketDescription::fromNetwork,
				handler -> handler.client(PacketDescription::handle));
		registrar.play(PacketPlaySound.ID, PacketPlaySound::new,
				handler -> handler.client(PacketPlaySound::handle));
		registrar.play(PacketSendNBTPacket.ID, PacketSendNBTPacket::fromNetwork,
				handler -> handler.client(PacketSendNBTPacket::handle));
		registrar.play(PacketSpawnParticle.ID, PacketSpawnParticle::fromNetwork,
				handler -> handler.client(PacketSpawnParticle::handle));
		registrar.play(PacketSpawnParticleTrail.ID, PacketSpawnParticleTrail::fromNetwork,
				handler -> handler.client(PacketSpawnParticleTrail::handle));
		registrar.play(PacketSpawnIndicatorParticles.ID, PacketSpawnIndicatorParticles::fromNetwork,
				handler -> handler.client(PacketSpawnIndicatorParticles::handle));
		registrar.play(PacketUpdatePressureBlock.ID, PacketUpdatePressureBlock::fromNetwork,
				handler -> handler.client(PacketUpdatePressureBlock::handle));
		registrar.play(PacketUpdateGui.ID, PacketUpdateGui::fromNetwork,
				handler -> handler.client(PacketUpdateGui::handle));
		registrar.play(PacketServerTickTime.ID, PacketServerTickTime::fromNetwork,
				handler -> handler.client(PacketServerTickTime::handle));
		registrar.play(PacketSpawnRing.ID, PacketSpawnRing::fromNetwork,
				handler -> handler.client(PacketSpawnRing::handle));
		registrar.play(PacketShowArea.ID, PacketShowArea::fromNetwork,
				handler -> handler.client(PacketShowArea::handle));
		registrar.play(PacketSetEntityMotion.ID, PacketSetEntityMotion::fromNetwork,
				handler -> handler.client(PacketSetEntityMotion::handle));
		registrar.play(PacketDebugBlock.ID, PacketDebugBlock::fromNetwork,
				handler -> handler.client(PacketDebugBlock::handle));
		registrar.play(PacketPlayMovingSound.ID, PacketPlayMovingSound::fromNetwork,
				handler -> handler.client(PacketPlayMovingSound::handle));
		registrar.play(PacketNotifyBlockUpdate.ID, PacketNotifyBlockUpdate::fromNetwork,
				handler -> handler.client(PacketNotifyBlockUpdate::handle));
		registrar.play(PacketMinigunStop.ID, PacketMinigunStop::fromNetwork,
				handler -> handler.client(PacketMinigunStop::handle));
		registrar.play(PacketClearRecipeCache.ID, PacketClearRecipeCache::fromNetwork,
				handler -> handler.client(PacketClearRecipeCache::handle));


		// misc bi-directional
		registrar.play(PacketSetGlobalVariable.ID, PacketSetGlobalVariable::fromNetwork,
				handler -> handler.client(PacketSetGlobalVariable::handle).server(PacketSetGlobalVariable::handle));
		registrar.play(PacketSyncSemiblock.ID, PacketSyncSemiblock::fromNetwork,
				handler -> handler.client(PacketSyncSemiblock::handle).server(PacketSyncSemiblock::handle));
		registrar.play(PacketSyncSmartChest.ID, PacketSyncSmartChest::fromNetwork,
				handler -> handler.client(PacketSyncSmartChest::handle).server(PacketSyncSmartChest::handle));

		// tube modules
		registrar.play(PacketUpdatePressureModule.ID, PacketUpdatePressureModule::fromNetwork,
				handler -> handler.server(TubeModulePacket::handle));
		registrar.play(PacketUpdateAirGrateModule.ID, PacketUpdateAirGrateModule::fromNetwork,
				handler -> handler.server(TubeModulePacket::handle));
		registrar.play(PacketTubeModuleColor.ID, PacketTubeModuleColor::fromNetwork,
				handler -> handler.server(TubeModulePacket::handle));
		registrar.play(PacketSyncRedstoneModuleToServer.ID, PacketSyncRedstoneModuleToServer::fromNetwork,
				handler -> handler.server(TubeModulePacket::handle));
		registrar.play(PacketUpdateLogisticsModule.ID, PacketUpdateLogisticsModule::fromNetwork,
				handler -> handler.client(TubeModulePacket::handle));
		registrar.play(PacketSyncRedstoneModuleToClient.ID, PacketSyncRedstoneModuleToClient::fromNetwork,
				handler -> handler.client(TubeModulePacket::handle));

		// amadron
		registrar.play(PacketSyncAmadronOffers.ID, PacketSyncAmadronOffers::fromNetwork,
				handler -> handler.client(PacketSyncAmadronOffers::handle));
		registrar.play(PacketAmadronOrderResponse.ID, PacketAmadronOrderResponse::fromNetwork,
				handler -> handler.client(PacketAmadronOrderResponse::handle));
		registrar.play(PacketAmadronOrderUpdate.ID, PacketAmadronOrderUpdate::fromNetwork,
				handler -> handler.server(PacketAmadronOrderUpdate::handle));
		registrar.play(PacketAmadronStockUpdate.ID, PacketAmadronStockUpdate::fromNetwork,
				handler -> handler.client(PacketAmadronStockUpdate::handle));
		registrar.play(PacketAmadronTradeNotifyDeal.ID, PacketAmadronTradeNotifyDeal::fromNetwork,
				handler -> handler.client(PacketAmadronTradeNotifyDeal::handle));
		registrar.play(PacketAmadronTradeRemoved.ID, PacketAmadronTradeRemoved::fromNetwork,
				handler -> handler.client(PacketAmadronTradeRemoved::handle));
		registrar.play(PacketAmadronTradeAddCustom.ID, PacketAmadronTradeAddCustom::fromNetwork,
				handler -> handler.client(PacketAmadronTradeAddCustom::handle)
						.server(PacketAmadronTradeAddCustom::handle));

		// hacking
		registrar.play(PacketHackingBlockStart.ID, PacketHackingBlockStart::fromNetwork,
				handler -> handler.client(PacketHackingBlockStart::handle).server(PacketHackingBlockStart::handle));
		registrar.play(PacketHackingEntityStart.ID, PacketHackingEntityStart::fromNetwork,
				handler -> handler.client(PacketHackingEntityStart::handle).server(PacketHackingEntityStart::handle));
		registrar.play(PacketHackingBlockFinish.ID, PacketHackingBlockFinish::fromNetwork,
				handler -> handler.client(PacketHackingBlockFinish::handle));
		registrar.play(PacketHackingEntityFinish.ID, PacketHackingEntityFinish::fromNetwork,
				handler -> handler.client(PacketHackingEntityFinish::handle));
		registrar.play(PacketSyncHackSimulationUpdate.ID, PacketSyncHackSimulationUpdate::fromNetwork,
				handler -> handler.client(PacketSyncHackSimulationUpdate::handle));
		registrar.play(PacketSyncEntityHacks.ID, PacketSyncEntityHacks::fromNetwork,
				handler -> handler.client(PacketSyncEntityHacks::handle));

		// pneumatic armor
		registrar.play(PacketToggleArmorFeature.ID, PacketToggleArmorFeature::fromNetwork,
				handler -> handler.server(PacketToggleArmorFeature::handle).client(PacketToggleArmorFeature::handle));
		registrar.play(PacketToggleArmorFeatureBulk.ID, PacketToggleArmorFeatureBulk::fromNetwork,
				handler -> handler.server(PacketToggleArmorFeatureBulk::handle));
		registrar.play(PacketPneumaticKick.ID, PacketPneumaticKick::fromNetwork,
				handler -> handler.server(PacketPneumaticKick::handle));
		registrar.play(PacketJetBootsActivate.ID, PacketJetBootsActivate::fromNetwork,
				handler -> handler.server(PacketJetBootsActivate::handle));
		registrar.play(PacketChestplateLauncher.ID, PacketChestplateLauncher::fromNetwork,
				handler -> handler.server(PacketChestplateLauncher::handle));
		registrar.play(PacketUpdateArmorColors.ID, PacketUpdateArmorColors::fromNetwork,
				handler -> handler.server(PacketUpdateArmorColors::handle));
		registrar.play(PacketUpdateArmorExtraData.ID, PacketUpdateArmorExtraData::fromNetwork,
				handler -> handler.server(PacketUpdateArmorExtraData::handle));
		registrar.play(PacketJetBootsStateSync.ID, PacketJetBootsStateSync::fromNetwork,
				handler -> handler.client(PacketJetBootsStateSync::handle));
		registrar.play(PacketSendArmorHUDMessage.ID, PacketSendArmorHUDMessage::fromNetwork,
				handler -> handler.client(PacketSendArmorHUDMessage::handle));

		// drones
		registrar.play(PacketUpdateDebuggingDrone.ID, PacketUpdateDebuggingDrone::fromNetwork,
				handler -> handler.server(DronePacket::handle));
		registrar.play(PacketSendDroneDebugEntry.ID, PacketSendDroneDebugEntry::fromNetwork,
				handler -> handler.client(DronePacket::handle));
		registrar.play(PacketSyncDroneProgWidgets.ID, PacketSyncDroneProgWidgets::fromNetwork,
				handler -> handler.client(DronePacket::handle));
		registrar.play(PacketShowWireframe.ID, PacketShowWireframe::fromNetwork,
				handler -> handler.client(PacketShowWireframe::handle));
		registrar.play(PacketProgrammerSync.ID, PacketProgrammerSync::fromNetwork,
				 handler -> handler.client(PacketProgrammerSync::handle).server(PacketProgrammerSync::handle));
	}

    public static void sendToAll(CustomPacketPayload message) {
		sendMessage(message, msg -> PacketDistributor.ALL.noArg().send(msg));
    }

    public static void sendToPlayer(CustomPacketPayload message, ServerPlayer player) {
		sendMessage(message, msg -> PacketDistributor.PLAYER.with(player).send(msg));
    }

	public static void sendToAllTracking(CustomPacketPayload message, Entity entity) {
    	sendMessage(message, msg -> PacketDistributor.TRACKING_ENTITY.with(entity).send(msg));
	}

	public static void sendToAllTracking(CustomPacketPayload message, Level level, BlockPos pos) {
		sendMessage(message, msg -> PacketDistributor.TRACKING_CHUNK.with(level.getChunkAt(pos)).send(msg));
	}

	public static void sendToAllTracking(CustomPacketPayload message, BlockEntity te) {
    	if (te.getLevel() != null) {
    		sendToAllTracking(message, te.getLevel(), te.getBlockPos());
		}
    }

	public static void sendToDimension(CustomPacketPayload message, ResourceKey<Level> level) {
		sendMessage(message, msg -> PacketDistributor.DIMENSION.with(level).send(msg));
    }

    public static void sendToServer(CustomPacketPayload message) {
		sendMessage(message, msg -> PacketDistributor.SERVER.noArg().send(msg));
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

	private static void sendMessage(CustomPacketPayload message, Consumer<CustomPacketPayload> consumer) {
		if (message instanceof ILargePayload) {
			// see PacketMultiHeader#receivePayload for message reassembly
			FriendlyByteBuf buf = ((ILargePayload) message).dumpToBuffer();
			if (buf.writerIndex() < ILargePayload.MAX_PAYLOAD_SIZE) {
				consumer.accept(message);
			} else {
				List<CustomPacketPayload> messageParts = new ArrayList<>();
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
