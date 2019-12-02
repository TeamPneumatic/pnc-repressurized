package me.desht.pneumaticcraft.proxy;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Pair;

public interface IProxy {
    World getWorldFor(NetworkEvent.Context ctx);

    World getClientWorld();

    PlayerEntity getClientPlayer();

    void preInit();

    void init();

    void postInit();

    int getArmorRenderID(String armorName);

//    HackTickHandler getHackTickHandler();

    boolean isSneakingInGui();

//    void initConfig();

    String xlate(String key);

    void suppressItemEquipAnimation();

    int particleLevel();

    Pair<Integer,Integer> getScaledScreenSize();

    Iterable<? extends Entity> getAllEntities(World world);

    boolean isScreenHiRes();

    void openGui(Screen gui);
}
