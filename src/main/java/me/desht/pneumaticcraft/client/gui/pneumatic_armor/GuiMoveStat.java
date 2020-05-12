package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IUpgradeRenderHandler;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticScreenBase;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.gui.widget.WidgetKeybindCheckBox;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.MainHelmetHandler;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.client.MainWindow;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.gui.widget.Slider;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GuiMoveStat extends GuiPneumaticScreenBase {
    private final IGuiAnimatedStat movedStat;
    private final IUpgradeRenderHandler renderHandler;
    private boolean clicked = false;
    private final List<IGuiAnimatedStat> otherStats = new ArrayList<>();
    private final List<String> helpText = new ArrayList<>();
    private final ArmorHUDLayout.LayoutTypes layoutItem;

    private WidgetCheckBox snapToGrid;
    private Slider gridSlider;

    private static boolean snap = false;
    private static int gridSize = 4;

    GuiMoveStat(IUpgradeRenderHandler renderHandler, ArmorHUDLayout.LayoutTypes layoutItem) {
        this(renderHandler, layoutItem, renderHandler.getAnimatedStat());
    }

    GuiMoveStat(IUpgradeRenderHandler renderHandler, ArmorHUDLayout.LayoutTypes layoutItem, @Nonnull IGuiAnimatedStat movedStat) {
        super(new StringTextComponent("Move Gui"));

        this.movedStat = movedStat;
        this.renderHandler = renderHandler;
        this.layoutItem = layoutItem;

        movedStat.openWindow();

        CommonArmorHandler hudHandler = CommonArmorHandler.getHandlerForPlayer();
        for (EquipmentSlotType slot : UpgradeRenderHandlerList.ARMOR_SLOTS) {
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
            mainOptions.testMessageStat = new WidgetAnimatedStat(null, "Test Message, keep in mind messages can be long!",
                    WidgetAnimatedStat.StatIcon.NONE, 0x7000AA00, null, ArmorHUDLayout.INSTANCE.messageStat);
            mainOptions.testMessageStat.openWindow();
            otherStats.add(mainOptions.testMessageStat);
        }
    }

    @Override
    public void init() {
        super.init();

        snapToGrid = new WidgetCheckBox(10, (height * 3) / 5, 0xC0C0C0, "Snap To Grid");
        snapToGrid.x = (width - snapToGrid.getWidth()) / 2;
        snapToGrid.checked = snap;
        addButton(snapToGrid);

        gridSlider = new Slider(snapToGrid.x, snapToGrid.y + 12, snapToGrid.getWidth(), 10,
                "", "", 1, 12, gridSize, false, true, b -> {}, null);
        addButton(gridSlider);
    }

    @Override
    protected ResourceLocation getTexture() {
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (movedStat.getBounds().contains((int)mouseX, (int)mouseY)) {
            if (mouseButton == 2) {
                movedStat.setLeftSided(!movedStat.isLeftSided());
                save();
            } else if (mouseButton < 2) {
                clicked = true;
                reposition(movedStat, mouseX, mouseY);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (clicked) {
            if (mouseButton == 0 || mouseButton == 1) {
                reposition(movedStat, mouseX, mouseY);
            }
            save();
            clicked = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dx, double dy) {
        if (clicked) {
            reposition(movedStat, mouseX, mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, mouseButton, dx, dy);
    }

    private void reposition(IGuiAnimatedStat stat, double x, double y) {
        if (snap) {
            x = x - (x % gridSize);
            y = y - (y % gridSize);
        }
        stat.setBaseX((int) x);
        stat.setBaseY((int) y);
    }

    @Override
    public void onClose() {
        minecraft.displayGuiScreen(GuiHelmetMainScreen.getInstance());
    }


    @Override
    public void render(int x, int y, float partialTicks) {
        renderBackground();

        GuiUtils.showPopupHelpScreen(this, font, helpText);

        super.render(x, y, partialTicks);

        movedStat.render(-1, -1, partialTicks);

        otherStats.forEach(stat -> {
            int c = stat.getBackgroundColor();
            stat.setBackgroundColor(0x30606060);
            stat.render(-1, -1, partialTicks);
            stat.setBackgroundColor(c);
        });
    }

    @Override
    public void tick() {
        super.tick();

        snap = snapToGrid.checked;
        gridSize = gridSlider.getValueInt();
        gridSlider.visible = snap;

        movedStat.tickWidget();
        otherStats.forEach(IGuiAnimatedStat::tickWidget);

        if (helpText.isEmpty()) {
            helpText.add(TextFormatting.GREEN + "" + TextFormatting.UNDERLINE + "Moving: "
                    + I18n.format(WidgetKeybindCheckBox.UPGRADE_PREFIX + renderHandler.getUpgradeID()));
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
        MainWindow sr = minecraft.getMainWindow();
        ArmorHUDLayout.INSTANCE.updateLayout(layoutItem,
                ((float) movedStat.getBaseX() / (float) sr.getScaledWidth()),
                ((float) movedStat.getBaseY() / (float) sr.getScaledHeight()),
                movedStat.isLeftSided());
    }
}
