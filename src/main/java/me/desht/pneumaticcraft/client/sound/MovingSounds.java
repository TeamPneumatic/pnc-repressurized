package me.desht.pneumaticcraft.client.sound;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MovingSounds {
    public enum Sound {
        JET_BOOTS,
        MINIGUN
    }

    private static MovingSound createMovingSound(Sound s, Object o) {
        switch (s) {
            case JET_BOOTS:
                if (o instanceof EntityPlayer) {
                    return new MovingSoundJetBoots((EntityPlayer) o);
                }
                break;
            case MINIGUN:
                if (o instanceof EntityPlayer || o instanceof EntityDrone) {
                    return new MovingSoundMinigun((Entity) o);
                } else if (o instanceof BlockPos) {
                    TileEntity te = Minecraft.getMinecraft().world.getTileEntity((BlockPos) o);
                    return te == null ? null : new MovingSoundMinigun(te);
                }
                break;
        }
        throw new IllegalArgumentException("Invalid moving sound " + s + " for entity " + o);
    }

    @SideOnly(Side.CLIENT)
    public static void playMovingSound(Sound s, Object o) {
        MovingSound movingSound = createMovingSound(s, o);
        if (movingSound != null) {
            Minecraft.getMinecraft().getSoundHandler().playSound(movingSound);
        }
    }
}
