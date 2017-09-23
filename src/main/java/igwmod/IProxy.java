package igwmod;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public interface IProxy{
    void preInit(FMLPreInitializationEvent event);

    void postInit();

    void processIMC(FMLInterModComms.IMCEvent event);

    String getSaveLocation();

    EntityPlayer getPlayer();
}
