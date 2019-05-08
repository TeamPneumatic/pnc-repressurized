package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.MainHelmetHandler;
import me.desht.pneumaticcraft.common.CommonArmorHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiMoveStat extends GuiScreen {
    private final IGuiAnimatedStat movedStat;
    private final IUpgradeRenderHandler renderHandler;
    private boolean clicked = false;
    private final List<IGuiAnimatedStat> otherStats = new ArrayList<>();

    GuiMoveStat(IUpgradeRenderHandler renderHandler) {
        this(renderHandler, renderHandler.getAnimatedStat());
    }

    GuiMoveStat(IUpgradeRenderHandler renderHandler, @Nonnull IGuiAnimatedStat movedStat) {
        this.movedStat = movedStat;
        this.renderHandler = renderHandler;

        movedStat.openWindow();

        CommonArmorHandler hudHandler = CommonArmorHandler.getHandlerForPlayer();
        for (EntityEquipmentSlot slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
            List<IUpgradeRenderHandler> renderHandlers = UpgradeRenderHandlerList.instance().getHandlersForSlot(slot);
            for (int i = 0; i < renderHandlers.size(); i++) {
                IUpgradeRenderHandler upgradeRenderHandler = renderHandlers.get(i);
                if (hudHandler.isUpgradeRendererInserted(slot, i) && hudHandler.isUpgradeRendererEnabled(slot, i)) {
                    IGuiAnimatedStat stat = upgradeRenderHandler.getAnimatedStat();
                    if (stat != null && stat != movedStat) {
                        otherStats.add(stat);
                    }
                }
            }
        }

        MainHelmetHandler mainOptions = HUDHandler.instance().getSpecificRenderer(MainHelmetHandler.class);
        if (movedStat != mainOptions.testMessageStat) {
            mainOptions.testMessageStat = new GuiAnimatedStat(null, "Test Message, keep in mind messages can be long!",
                    mainOptions.messagesStatX, mainOptions.messagesStatY, 0x7000AA00, null, mainOptions.messagesStatLeftSided);
            mainOptions.testMessageStat.openWindow();
            otherStats.add(mainOptions.testMessageStat);
        }
    }

    @Override
    protected void mouseClickMove(int x, int y, int lastButtonClicked, long timeSinceMouseClick) {
        if (clicked) {
            movedStat.setBaseX(x);
            movedStat.setBaseY(y);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
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
            Minecraft.getMinecraft().displayGuiScreen(GuiHelmetMainScreen.getInstance());
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
        drawDefaultBackground();
        drawString(fontRenderer, "Middle mouse click to switch between expansion to the right or left", 5, 5, 0xFFFFFFFF);
        super.drawScreen(x, y, partialTicks);

        movedStat.render(-1, -1, partialTicks);

        otherStats.forEach(stat -> {
            int c = stat.getBackgroundColor();
            stat.setBackGroundColor(0x30606060);
            stat.render(-1, -1, partialTicks);
            stat.setBackGroundColor(c);
        });
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        movedStat.update();
        otherStats.forEach(IGuiAnimatedStat::update);
    }
}
