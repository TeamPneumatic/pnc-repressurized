package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.EntityTrackUpgradeHandler;
import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketShowWireframe extends LocationIntPacket<PacketShowWireframe> {

    private int entityId;

    public PacketShowWireframe() {
    }

    public PacketShowWireframe(EntityDrone entity, BlockPos pos) {
        super(pos);
        entityId = entity.getEntityId();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);
        buffer.writeInt(entityId);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        super.fromBytes(buffer);
        entityId = buffer.readInt();
    }

    @Override
    public void handleClientSide(PacketShowWireframe message, EntityPlayer player) {
        Entity ent = player.world.getEntityByID(message.entityId);
        if (ent instanceof EntityDrone) {
            addToHudHandler((EntityDrone) ent, message.pos);
        }
    }

    @SideOnly(Side.CLIENT)
    private void addToHudHandler(EntityDrone drone, BlockPos pos) {
        HUDHandler.instance().getSpecificRenderer(EntityTrackUpgradeHandler.class).getTargetsStream()
                .filter(target -> target.entity == drone)
                .forEach(target -> target.getDroneAIRenderer().addBlackListEntry(drone.world, pos));
    }

    @Override
    public void handleServerSide(PacketShowWireframe message, EntityPlayer player) {
    }

}
