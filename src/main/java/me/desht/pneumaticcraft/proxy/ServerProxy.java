package me.desht.pneumaticcraft.proxy;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Pair;

public class ServerProxy implements IProxy {
//    private final HackTickHandler serverHackTickHandler = new HackTickHandler();

//    @Override
//    public void initConfig() {
//    }

    @Override
    public World getWorldFor(NetworkEvent.Context ctx) {
        assert ctx.getSender() != null;
        return ctx.getSender().world;
    }

    @Override
    public World getClientWorld() {
        return null;
    }

    @Override
    public PlayerEntity getClientPlayer() {
        return null;
    }

    @Override
    public void preInit() {
    }

    @Override
    public void init() {
    }

    @Override
    public void postInit() {
    }

    @Override
    public int getArmorRenderID(String armorName) {
        return 0;
    }

//    @Override
//    public HackTickHandler getHackTickHandler() {
//        return serverHackTickHandler;
//    }

    @Override
    public boolean isSneakingInGui() {
        return false;
    }

    @Override
    public String xlate(String key) {
        return "{*" + key + "*}";  // for TheOneProbe formatting
    }

    @Override
    public void suppressItemEquipAnimation() {
    }

    @Override
    public int particleLevel() {
        return 0;
    }

    @Override
    public Pair<Integer, Integer> getScaledScreenSize() {
        return Pair.of(0, 0);
    }

    @Override
    public Iterable<? extends Entity> getAllEntities(World world) {
        return ((ServerWorld)world).getEntities()::iterator;
    }

    @Override
    public boolean isScreenHiRes() {
        return false;
    }

    @Override
    public void openGui(Screen gui) {
    }
}
