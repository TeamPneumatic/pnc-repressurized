package me.desht.pneumaticcraft.client.gui.pneumaticHelmet;

import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiMoveStat extends GuiScreen {
    private final IGuiAnimatedStat movedStat;
    private final IUpgradeRenderHandler renderHandler;
    private boolean clicked = false;

    public GuiMoveStat(IUpgradeRenderHandler renderHandler) {
        movedStat = renderHandler.getAnimatedStat();
        this.renderHandler = renderHandler;
        if (movedStat == null) {
            System.err.println("OPENING A MOVE STAT GUI WHILE THERE IS NO STAT TO MOVE!");
            FMLClientHandler.instance().getClient().player.closeScreen();
        }
    }

    public GuiMoveStat(IUpgradeRenderHandler renderHandler, GuiAnimatedStat movedStat) {
        this.renderHandler = renderHandler;
        this.movedStat = movedStat;
    }

    @Override
    protected void mouseClickMove(int x, int y, int lastButtonClicked, long timeSinceMouseClick) {
        if (clicked) {
            movedStat.setBaseX(x);
            movedStat.setBaseY(y);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 2) {
            movedStat.setLeftSided(!movedStat.isLeftSided());
            renderHandler.saveToConfig();
        } else if (mouseButton < 2) {
            clicked = true;
            movedStat.setBaseX(mouseX);
            movedStat.setBaseY(mouseY);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (clicked) {
            if (mouseButton == 0 || mouseButton == 1) {
                movedStat.setBaseX(mouseX);
                movedStat.setBaseY(mouseY);
            }
            renderHandler.saveToConfig();
            clicked = false;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            FMLCommonHandler.instance().showGuiScreen(GuiHelmetMainScreen.getInstance());
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
        drawDefaultBackground();
        drawString(fontRenderer, "Middle mouse click to switch between expansion to the right or left", 5, 5, 0xFFFFFFFF);
        movedStat.render(x, y, partialTicks);
        super.drawScreen(x, y, partialTicks);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        movedStat.update();
    }
}
