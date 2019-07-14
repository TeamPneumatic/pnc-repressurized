package me.desht.pneumaticcraft.client.sound;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MovingSounds {
    public enum Sound {
        JET_BOOTS,
        MINIGUN,
        ELEVATOR
    }

    private static TickableSound createMovingSound(Sound s, Object o) {
        switch (s) {
            case JET_BOOTS:
                if (o instanceof PlayerEntity) {
                    return new MovingSoundJetBoots((PlayerEntity) o);
                }
                break;
            case MINIGUN:
                if (o instanceof PlayerEntity || o instanceof EntityDrone) {
                    return new MovingSoundMinigun((Entity) o);
                } else if (o instanceof BlockPos) {
                    TileEntity te = Minecraft.getInstance().world.getTileEntity((BlockPos) o);
                    return te == null ? null : new MovingSoundMinigun(te);
                }
                break;
            case ELEVATOR:
                if (o instanceof BlockPos) {
                    TileEntity te = Minecraft.getInstance().world.getTileEntity((BlockPos) o);
                    if (te instanceof TileEntityElevatorBase) {
                        return new MovingSoundElevator((TileEntityElevatorBase) te);
                    }
                }
        }
        throw new IllegalArgumentException("Invalid moving sound " + s + " for entity " + o);
    }

    @OnlyIn(Dist.CLIENT)
    public static void playMovingSound(Sound s, Object o) {
        TickableSound movingSound = createMovingSound(s, o);
        if (movingSound != null) {
            Minecraft.getInstance().getSoundHandler().play(movingSound);
        }
    }
}
