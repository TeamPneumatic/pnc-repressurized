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

package me.desht.pneumaticcraft.common.util.fakeplayer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.*;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.net.SocketAddress;
import java.util.Set;

// With credit to CoFH-Core

public class FakeNetHandlerPlayerServer extends ServerPlayNetHandler {
    private static class NetworkManagerFake extends NetworkManager {
        NetworkManagerFake() {
            super(PacketDirection.CLIENTBOUND);
        }

        @Override
        public void channelActive(ChannelHandlerContext p_channelActive_1_) {

        }

        @Override
        public void setProtocol(ProtocolType newState) {

        }

        @Override
        public void channelInactive(ChannelHandlerContext p_channelInactive_1_) {

        }

        @Override
        public void exceptionCaught(ChannelHandlerContext p_exceptionCaught_1_, Throwable p_exceptionCaught_2_) {

        }

        @Override
        public void setListener(INetHandler handler) {

        }

        @Override
        public void send(IPacket<?> packetIn) {

        }

        @Override
        public void send(IPacket<?> p_201058_1_, @Nullable GenericFutureListener<? extends Future<? super Void>> p_201058_2_) {
        }

        @Override
        public void tick() {

        }

        @Override
        public SocketAddress getRemoteAddress() {

            return null;
        }

        @Override
        public boolean isMemoryConnection() {

            return false;
        }

//        @Override
//        public void enableEncryption(SecretKey key) {
//
//        }

        @Override
        public boolean isConnected() {

            return false;
        }

        @Override
        public INetHandler getPacketListener() {

            return null;
        }

        @Override
        public ITextComponent getDisconnectedReason() {

            return null;
        }

        @Override
        public void setupCompression(int threshold) {

        }

        @Override
        public void setReadOnly() {

        }

        @Override
        public void handleDisconnection() {

        }

        @Override
        public Channel channel() {

            return null;
        }
    }

    public FakeNetHandlerPlayerServer(MinecraftServer server, ServerPlayerEntity playerIn) {
        super(server, new NetworkManagerFake(), playerIn);
    }

    @Override
    public void tick() {

    }

    @Override
    public void disconnect(final ITextComponent textComponent) {

    }

    @Override
    public void handlePlayerInput(CInputPacket packetIn) {

    }

    @Override
    public void handleMoveVehicle(CMoveVehiclePacket packetIn) {

    }

    @Override
    public void handleAcceptTeleportPacket(CConfirmTeleportPacket packetIn) {

    }

    @Override
    public void handleMovePlayer(CPlayerPacket packetIn) {

    }

    @Override
    public void teleport(double x, double y, double z, float yaw, float pitch) {

    }

    @Override
    public void teleport(double p_175089_1_, double p_175089_3_, double p_175089_5_, float p_175089_7_, float p_175089_8_, Set<SPlayerPositionLookPacket.Flags> p_175089_9_) {

    }

    @Override
    public void handlePlayerAction(CPlayerDiggingPacket packetIn) {

    }

    @Override
    public void handleUseItemOn(CPlayerTryUseItemOnBlockPacket packetIn) {

    }

    @Override
    public void handleUseItem(CPlayerTryUseItemPacket packetIn) {

    }

    @Override
    public void handleTeleportToEntityPacket(CSpectatePacket packetIn) {

    }

    @Override
    public void handleResourcePackResponse(CResourcePackStatusPacket packetIn) {

    }

    @Override
    public void handlePaddleBoat(CSteerBoatPacket packetIn) {

    }

    @Override
    public void onDisconnect(ITextComponent reason) {

    }

    @Override
    public void send(final IPacket<?> packetIn) {

    }

    @Override
    public void handleSetCarriedItem(CHeldItemChangePacket packetIn) {

    }

    @Override
    public void handleChat(CChatMessagePacket packetIn) {

    }

    @Override
    public void handleAnimate(CAnimateHandPacket packetIn) {

    }

    @Override
    public void handlePlayerCommand(CEntityActionPacket packetIn) {

    }

    @Override
    public void handleInteract(CUseEntityPacket packetIn) {

    }

    @Override
    public void handleClientCommand(CClientStatusPacket packetIn) {

    }

    @Override
    public void handleContainerClose(CCloseWindowPacket packetIn) {

    }

    @Override
    public void handleContainerClick(CClickWindowPacket packetIn) {

    }

    @Override
    public void handleContainerButtonClick(CEnchantItemPacket packetIn) {

    }

    @Override
    public void handleSetCreativeModeSlot(CCreativeInventoryActionPacket packetIn) {

    }

    @Override
    public void handleContainerAck(CConfirmTransactionPacket packetIn) {

    }

    @Override
    public void handleSignUpdate(CUpdateSignPacket packetIn) {

    }

    @Override
    public void handleKeepAlive(CKeepAlivePacket packetIn) {

    }

    @Override
    public void handlePlayerAbilities(CPlayerAbilitiesPacket packetIn) {

    }

    @Override
    public void handleCustomCommandSuggestions(CTabCompletePacket packetIn) {

    }

    @Override
    public void handleClientInformation(CClientSettingsPacket packetIn) {

    }

    @Override
    public void handleCustomPayload(CCustomPayloadPacket packetIn) {

    }
}
