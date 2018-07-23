package me.desht.pneumaticcraft.proxy;

import me.desht.pneumaticcraft.api.hacking.IHacking;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.HackingImpl;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import me.desht.pneumaticcraft.common.HackTickHandler;
import me.desht.pneumaticcraft.common.advancements.AdvancementTriggers;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {
    public int PneumaticHelmetRenderID = 0;

    private final HackTickHandler serverHackTickHandler = new HackTickHandler();

    public void initConfig() {
    }

    public World getClientWorld() {
        return null;
    }

    public EntityPlayer getPlayer() {
        return null;
    }

    public Side getSide() {
        return FMLCommonHandler.instance().getEffectiveSide();
    }

    public void postInit() {
    }

    public int getArmorRenderID(String armorName) {
        return 0;
    }

    public int getRenderIdForRenderer(Class clazz) {
        return 0;
    }

    public HackTickHandler getHackTickHandler() {
        return serverHackTickHandler;
    }

    public boolean isSneakingInGui() {
        return false;
    }

    public void preInit() {
        CapabilityManager.INSTANCE.register(IHacking.class, new HackingImpl.Storage(), HackingImpl::new);
        AdvancementTriggers.registerTriggers();
    }

    public void init() {
        MinecraftForge.EVENT_BUS.register(CommonHUDHandler.class);
        MinecraftForge.EVENT_BUS.register(getHackTickHandler());

        SemiBlockManager.registerEventHandler(getClientWorld() != null);
    }

    public void registerSemiBlockRenderer(Item semiBlock) {

    }

    public void addScheduledTask(Runnable runnable, boolean serverSide) {
        FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(runnable);
    }
}
