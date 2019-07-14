package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.block.Block;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * MineChess
 *
 * @author MineMaarten
 *         www.minemaarten.com
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 */

public abstract class LocationIntPacket {

    protected BlockPos pos;

    public LocationIntPacket() {
    }

    public LocationIntPacket(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
    }

    public LocationIntPacket(BlockPos pos) {
        this.pos = pos;
    }

    public void toBytes(ByteBuf buf) {
        PacketUtil.writeBlockPos(buf, pos);
    }

    PacketDistributor.TargetPoint getTargetPoint(World world) {
        return getTargetPoint(world, TileEntityConstants.PACKET_UPDATE_DISTANCE);
    }

    PacketDistributor.TargetPoint getTargetPoint(World world, double updateDistance) {
        return new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), updateDistance, world.getDimension().getType());
    }

    protected Block getBlock(World world) {
        return world.getBlockState(pos).getBlock();
    }

    protected TileEntity getTileEntity(Supplier<NetworkEvent.Context> ctx) {
        return PneumaticCraftRepressurized.proxy.getWorldFor(ctx.get()).getTileEntity(pos);
    }
}
