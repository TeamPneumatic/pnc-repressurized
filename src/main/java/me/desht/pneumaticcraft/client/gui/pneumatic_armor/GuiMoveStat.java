package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.client.gui.GuiUtils;
import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.GuiKeybindCheckBox;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.MainHelmetHandler;
import me.desht.pneumaticcraft.common.config.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.text.TextFormatting;
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
    private final List<String> helpText = new ArrayList<>();
    private final ArmorHUDLayout.LayoutTypes layoutItem;

    GuiMoveStat(IUpgradeRenderHandler renderHandler, ArmorHUDLayout.LayoutTypes layoutItem) {
        this(renderHandler, layoutItem, renderHandler.getAnimatedStat());
    }

    GuiMoveStat(IUpgradeRenderHandler renderHandler, ArmorHUDLayout.LayoutTypes layoutItem, @Nonnull IGuiAnimatedStat movedStat) {
        this.movedStat = movedStat;
        this.renderHandler = renderHandler;
        this.layoutItem = layoutItem;

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
                    GuiAnimatedStat.StatIcon.NONE, 0x7000AA00, null, ArmorHUDLayout.INSTANCE.messageStat);
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
            save();
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
            save();
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

        GuiUtils.showPopupHelpScreen(this, fontRenderer, helpText);

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

        if (helpText.isEmpty()) {
            helpText.add(TextFormatting.GREEN + "" + TextFormatting.UNDERLINE + "Moving: "
                    + I18n.format(GuiKeybindCheckBox.UPGRADE_PREFIX + renderHandler.getUpgradeName()));
            helpText.add("");
            helpText.add("Left- or Right-Click: move the highlighted stat");
            helpText.add("...");
        }
        helpText.set(3, "Stat expands " + getDir(movedStat.isLeftSided()) + ". Middle-click: expand " + getDir(!movedStat.isLeftSided()));
    }

    private String getDir(boolean left) {
        return TextFormatting.YELLOW + (left ? "Left" : "Right") + TextFormatting.RESET;
    }

    private void save() {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        ArmorHUDLayout.INSTANCE.updateLayout(layoutItem,
                (float)(movedStat.getBaseX() / sr.getScaledWidth_double()),
                (float)(movedStat.getBaseY() / sr.getScaledHeight_double()),
                movedStat.isLeftSided());
    }
}
