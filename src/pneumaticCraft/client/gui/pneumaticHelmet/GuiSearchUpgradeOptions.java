package pneumaticCraft.client.gui.pneumaticHelmet;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import pneumaticCraft.api.client.pneumaticHelmet.IGuiScreen;
import pneumaticCraft.api.client.pneumaticHelmet.IOptionPage;
import pneumaticCraft.client.gui.GuiSearcher;
import pneumaticCraft.client.render.pneumaticArmor.SearchUpgradeHandler;
import pneumaticCraft.common.NBTUtil;
import pneumaticCraft.common.item.ItemPneumaticArmor;
import pneumaticCraft.common.network.NetworkHandler;
import pneumaticCraft.common.network.PacketUpdateSearchStack;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;

public class GuiSearchUpgradeOptions implements IOptionPage{

    private final SearchUpgradeHandler renderHandler;
    private static GuiSearcher searchGui;
    private final EntityPlayer player = FMLClientHandler.instance().getClient().thePlayer;

    public GuiSearchUpgradeOptions(SearchUpgradeHandler searchUpgradeHandler){
        renderHandler = searchUpgradeHandler;
    }

    @Override
    public String getPageName(){
        return "Item Search Upgrade";
    }

    @Override
    public void initGui(IGuiScreen gui){
        gui.getButtonList().add(new GuiButton(10, 30, 40, 150, 20, "Search for item..."));
        gui.getButtonList().add(new GuiButton(11, 30, 128, 150, 20, "Move Stat Screen..."));
        if(searchGui != null && player.getCurrentArmor(3) != null) {
            ItemStack searchStack = searchGui.getSearchStack();
            ItemStack helmetStack = ItemPneumaticArmor.getSearchedStack(player.getCurrentArmor(3));
            if(searchStack == null && helmetStack != null || searchStack != null && helmetStack == null || searchStack != null && helmetStack != null && !searchStack.isItemEqual(helmetStack)) {
                NetworkHandler.sendToServer(new PacketUpdateSearchStack(searchStack));
                NBTTagCompound tag = NBTUtil.getCompoundTag(player.getCurrentArmor(3), "SearchStack");
                tag.setInteger("itemID", searchStack != null ? Item.getIdFromItem(searchStack.getItem()) : -1);
                tag.setInteger("itemDamage", searchStack != null ? searchStack.getItemDamage() : -1);
            }
        }
    }

    @Override
    public void actionPerformed(GuiButton button){
        if(button.id == 10) {
            searchGui = new GuiSearcher(player);
            if(player.getCurrentArmor(3) != null) {
                searchGui.setSearchStack(ItemPneumaticArmor.getSearchedStack(player.getCurrentArmor(3)));
            }
            FMLClientHandler.instance().showGuiScreen(searchGui);
        } else {
            FMLCommonHandler.instance().showGuiScreen(new GuiMoveStat(renderHandler));
        }
    }

    @Override
    public void drawPreButtons(int x, int y, float partialTicks){}

    @Override
    public void drawScreen(int x, int y, float partialTicks){}

    @Override
    public void keyTyped(char ch, int key){}

    @Override
    public void mouseClicked(int x, int y, int button){}

    @Override
    public void handleMouseInput(){}

    @Override
    public boolean canBeTurnedOff(){
        return true;
    }

    @Override
    public boolean displaySettingsText(){
        return true;
    }
}
