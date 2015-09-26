package pneumaticCraft.common.recipes;

import pneumaticCraft.common.AchievementHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

public class CraftingHandler{

    @SubscribeEvent
    public void onCrafting(PlayerEvent.ItemCraftedEvent event){
        if(event.player != null) {
            AchievementHandler.giveAchievement(event.player, event.crafting);
        }
    }

}
