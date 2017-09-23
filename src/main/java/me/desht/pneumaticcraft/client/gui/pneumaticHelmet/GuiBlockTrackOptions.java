package me.desht.pneumaticcraft.client.gui.pneumaticHelmet;

import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IGuiScreen;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IOptionPage;
import me.desht.pneumaticcraft.client.gui.widget.GuiKeybindCheckBox;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.BlockTrackUpgradeHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.blockTracker.BlockTrackEntryList;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class GuiBlockTrackOptions implements IOptionPage {

    private final BlockTrackUpgradeHandler renderHandler;

    public GuiBlockTrackOptions(BlockTrackUpgradeHandler renderHandler) {
        this.renderHandler = renderHandler;
    }

    @Override
    public String getPageName() {
        return "Block Tracker";
    }

    @Override
    public void initGui(IGuiScreen gui) {
        gui.getButtonList().add(new GuiButton(10, 30, 128, 150, 20, "Move Stat Screen..."));
        for (int i = 0; i < BlockTrackEntryList.instance.trackList.size(); i++) {
            ((GuiHelmetMainScreen) gui).addWidget(new GuiKeybindCheckBox(i, 5, 32 + i * 12, 0xFFFFFFFF, BlockTrackEntryList.instance.trackList.get(i).getEntryName()));
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 10) {
            FMLClientHandler.instance().getClient().player.closeScreen();
            FMLCommonHandler.instance().showGuiScreen(new GuiMoveStat(renderHandler));
        }
    }

    @Override
    public void drawPreButtons(int x, int y, float partialTicks) {
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
    }

    @Override
    public void keyTyped(char ch, int key) {
    }

    @Override
    public void mouseClicked(int x, int y, int button) {
    }

    @Override
    public void handleMouseInput() {
    }

    @Override
    public boolean canBeTurnedOff() {
        return true;
    }

    @Override
    public boolean displaySettingsText() {
        return true;
    }

}
