package pneumaticCraft.client.render.pneumaticArmor;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import pneumaticCraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import pneumaticCraft.api.client.pneumaticHelmet.RenderHandlerRegistry;
import pneumaticCraft.common.CommonHUDHandler;

public class UpgradeRenderHandlerList{
    private static UpgradeRenderHandlerList INSTANCE;

    public final List<IUpgradeRenderHandler> upgradeRenderers = new ArrayList<IUpgradeRenderHandler>();

    public static UpgradeRenderHandlerList instance(){
        return INSTANCE;
    }

    public static void init(){
        INSTANCE = new UpgradeRenderHandlerList();
    }

    private UpgradeRenderHandlerList(){
        RenderHandlerRegistry.renderHandlers = upgradeRenderers;
        upgradeRenderers.add(new MainHelmetHandler());
        upgradeRenderers.add(new BlockTrackUpgradeHandler());
        upgradeRenderers.add(new EntityTrackUpgradeHandler());
        upgradeRenderers.add(new SearchUpgradeHandler());
        upgradeRenderers.add(new CoordTrackUpgradeHandler());
        upgradeRenderers.add(new DroneDebugUpgradeHandler());
    }

    public float getAirUsage(EntityPlayer player, boolean countDisabled){
        float totalUsage = 0;
        for(int i = 0; i < upgradeRenderers.size(); i++) {
            if(CommonHUDHandler.getHandlerForPlayer(player).upgradeRenderersInserted[i] && (countDisabled || CommonHUDHandler.getHandlerForPlayer(player).upgradeRenderersEnabled[i])) totalUsage += upgradeRenderers.get(i).getEnergyUsage(CommonHUDHandler.getHandlerForPlayer(player).rangeUpgradesInstalled, player);
        }
        return totalUsage;
    }
}
