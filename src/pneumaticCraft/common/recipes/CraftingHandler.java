package pneumaticCraft.common.recipes;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import pneumaticCraft.common.AchievementHandler;
import pneumaticCraft.common.item.Itemss;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

public class CraftingHandler{

    @SubscribeEvent
    public void onCrafting(PlayerEvent.ItemCraftedEvent event){
        if(event.player != null) AchievementHandler.giveAchievement(event.player, event.crafting);

        if(event.player != null && !event.player.worldObj.isRemote && event.crafting != null && (event.crafting.getItem() == Itemss.assemblyProgram && event.crafting.getItemDamage() < 2 || event.crafting.getItem() == Itemss.PCBBlueprint) && ((EntityPlayerMP)event.player).playerNetServerHandler != null) {
            event.player.addChatComponentMessage(new ChatComponentTranslation("[PneumaticCraft] Bear in mind that this crafting recipe is temporary. Once I give Mechanics (PneumaticCraft Villagers)" + " a place to live, I'll remove this crafting recipe and you'll only be able to obtain this item via trading! (Ab)use it while you can."));
        }
    }

}
