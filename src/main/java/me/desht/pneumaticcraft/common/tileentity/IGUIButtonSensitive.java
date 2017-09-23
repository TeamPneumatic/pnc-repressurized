package me.desht.pneumaticcraft.common.tileentity;

import net.minecraft.entity.player.EntityPlayer;

public interface IGUIButtonSensitive {
    void handleGUIButtonPress(int guiID, EntityPlayer player);
}