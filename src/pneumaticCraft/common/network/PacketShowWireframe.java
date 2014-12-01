package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import pneumaticCraft.client.render.pneumaticArmor.EntityTrackUpgradeHandler;
import pneumaticCraft.client.render.pneumaticArmor.HUDHandler;
import pneumaticCraft.client.render.pneumaticArmor.RenderTarget;
import pneumaticCraft.common.entity.living.EntityDrone;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketShowWireframe extends LocationIntPacket<PacketShowWireframe>{

    private int entityId;

    public PacketShowWireframe(){}

    public PacketShowWireframe(EntityDrone entity, int x, int y, int z){
        super(x, y, z);
        entityId = entity.getEntityId();
    }

    @Override
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        buffer.writeInt(entityId);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        entityId = buffer.readInt();
    }

    @Override
    public void handleClientSide(PacketShowWireframe message, EntityPlayer player){
        Entity ent = player.worldObj.getEntityByID(message.entityId);
        if(ent instanceof EntityDrone) {
            addToHudHandler((EntityDrone)ent, message.x, message.y, message.z);
        }
    }

    @SideOnly(Side.CLIENT)
    private void addToHudHandler(EntityDrone drone, int x, int y, int z){
        List<RenderTarget> targets = HUDHandler.instance().getSpecificRenderer(EntityTrackUpgradeHandler.class).getTargets();
        for(RenderTarget target : targets) {
            if(target.entity == drone) {
                target.getDroneAIRenderer().addBlackListEntry(drone.worldObj, x, y, z);
            }
        }
    }

    @Override
    public void handleServerSide(PacketShowWireframe message, EntityPlayer player){}

}
