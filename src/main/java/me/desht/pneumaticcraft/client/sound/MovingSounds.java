package me.desht.pneumaticcraft.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class MovingSounds {
    public enum Sound {
        JET_BOOTS
    }

    private static final List<MovingSound> sounds = new ArrayList<>();

    private static MovingSound createMovingSound(Sound s, Entity e) {
        switch (s) {
            case JET_BOOTS:
                if (e instanceof EntityPlayer) {
                    return new MovingSoundJetBoots((EntityPlayer) e);
                }
        }
        throw new IllegalArgumentException("Invalid moving sound " + s + " for entity " + e);
    }

    @SideOnly(Side.CLIENT)
    public static void playMovingSound(Sound s, Entity e) {
        Minecraft.getMinecraft().getSoundHandler().playSound(createMovingSound(s, e));
    }
}
