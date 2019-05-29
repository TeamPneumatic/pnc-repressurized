package me.desht.pneumaticcraft.proxy;

import me.desht.pneumaticcraft.common.event.HackTickHandler;
import me.desht.pneumaticcraft.lib.EnumCustomParticleType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

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

    void playCustomParticle(EnumCustomParticleType enumCustomParticleType, World w, double x, double y, double z, double dx, double dy, double dz);

    String xlate(String key);

    void suppressItemEquipAnimation();

    int particleLevel();

    Pair<Integer,Integer> getScaledScreenSize();
}
