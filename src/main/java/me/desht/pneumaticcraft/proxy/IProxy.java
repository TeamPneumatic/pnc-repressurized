package me.desht.pneumaticcraft.proxy;

import me.desht.pneumaticcraft.common.HackTickHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public interface IProxy {
    World getClientWorld();

    EntityPlayer getClientPlayer();

    void preInit();

    void init();

    void postInit();

    int getArmorRenderID(String armorName);

    HackTickHandler getHackTickHandler();

    boolean isSneakingInGui();

    void addScheduledTask(Runnable runnable, boolean serverSide);

    void initConfig();
}
