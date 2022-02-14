/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.gui.pneumatic_armor;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.api.client.IGuiAnimatedStat;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler;
import me.desht.pneumaticcraft.client.gui.AbstractPneumaticCraftScreen;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.pneumatic_armor.ArmorUpgradeClientRegistry;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.CoreComponentsClientHandler;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.config.subconfig.ArmorHUDLayout;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.client.gui.widget.Slider;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ArmorStatMoveScreen extends AbstractPneumaticCraftScreen {
    private final IGuiAnimatedStat movedStat;
    private final IArmorUpgradeClientHandler<?> renderHandler;
    private boolean clicked = false;
    private final List<IGuiAnimatedStat> otherStats = new ArrayList<>();
    private final List<Component> helpText = new ArrayList<>();
    private final ArmorHUDLayout.LayoutType layoutItem;

    private WidgetCheckBox snapToGrid;
    private Slider gridSlider;

    private static boolean snap = false;
    private static int gridSize = 4;

    public ArmorStatMoveScreen(IArmorUpgradeClientHandler<?> renderHandler, ArmorHUDLayout.LayoutType layoutItem) {
        this(renderHandler, layoutItem, renderHandler.getAnimatedStat());
    }

    public ArmorStatMoveScreen(IArmorUpgradeClientHandler<?> renderHandler, ArmorHUDLayout.LayoutType layoutItem, @Nonnull IGuiAnimatedStat movedStat) {
        super(new TextComponent("Move Gui"));

        this.movedStat = movedStat;
        this.renderHandler = renderHandler;
        this.layoutItem = layoutItem;

        movedStat.openStat();

        CommonArmorHandler commonArmorHandler = CommonArmorHandler.getHandlerForPlayer();
        for (EquipmentSlot slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            List<IArmorUpgradeClientHandler<?>> renderHandlers = ArmorUpgradeClientRegistry.getInstance().getHandlersForSlot(slot);
            for (int i = 0; i < renderHandlers.size(); i++) {
                IArmorUpgradeClientHandler<?> upgradeRenderHandler = renderHandlers.get(i);
                if (commonArmorHandler.isUpgradeInserted(slot, i) && commonArmorHandler.isUpgradeEnabled(slot, i)) {
                    IGuiAnimatedStat stat = upgradeRenderHandler.getAnimatedStat();
                    if (stat != null && stat != movedStat) {
                        otherStats.add(stat);
                    }
                }
            }
        }

        CoreComponentsClientHandler mainOptions = ArmorUpgradeClientRegistry.getInstance()
                .getClientHandler(ArmorUpgradeRegistry.getInstance().coreComponentsHandler, CoreComponentsClientHandler.class);
        if (movedStat != mainOptions.testMessageStat) {
            mainOptions.testMessageStat = new WidgetAnimatedStat(null, new TextComponent("Test Message, keep in mind messages can be long!"),
                    WidgetAnimatedStat.StatIcon.NONE, HUDHandler.getInstance().getStatOverlayColor(), null, ArmorHUDLayout.INSTANCE.messageStat);
            mainOptions.testMessageStat.openStat();
            otherStats.add(mainOptions.testMessageStat);
        }
    }

    @Override
    public void init() {
        super.init();

        snapToGrid = new WidgetCheckBox(10, (height * 3) / 5, 0xC0C0C0, xlate("pneumaticcraft.gui.misc.snapToGrid"));
        snapToGrid.x = (width - snapToGrid.getWidth()) / 2;
        snapToGrid.checked = snap;
        addRenderableWidget(snapToGrid);

        gridSlider = new Slider(snapToGrid.x, snapToGrid.y + 12, snapToGrid.getWidth(), 10,
                TextComponent.EMPTY, TextComponent.EMPTY, 1, 12, gridSize, false, true, b -> {}, null);
        addRenderableWidget(gridSlider);
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
                // prevent stat from going off-screen where players can't interact with it
                if (movedStat.isLeftSided() && movedStat.getBaseX() < 10) {
                    movedStat.setBaseX(10);
                } else if (!movedStat.isLeftSided() && movedStat.getBaseX() > width - 10) {
                    movedStat.setBaseX(width - 10);
                }
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
        minecraft.setScreen(ArmorMainScreen.getInstance());
    }

    @Override
    public void render(PoseStack matrixStack, int x, int y, float partialTicks) {
        renderBackground(matrixStack);

        GuiUtils.showPopupHelpScreen(matrixStack,this, font, helpText);

        super.render(matrixStack, x, y, partialTicks);

        movedStat.renderStat(matrixStack,-1, -1, partialTicks);

        otherStats.forEach(stat -> {
            int c = stat.getBackgroundColor();
            stat.setBackgroundColor(0x30606060);
            stat.renderStat(matrixStack, -1, -1, partialTicks);
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
            helpText.add(xlate(ArmorUpgradeRegistry.getStringKey(renderHandler.getCommonHandler().getID())).withStyle(ChatFormatting.GREEN, ChatFormatting.UNDERLINE));
            helpText.add(TextComponent.EMPTY);
            helpText.add(xlate("pneumaticcraft.armor.moveStat.move"));
            helpText.add(new TextComponent("<REPLACEME>"));
        }
        helpText.set(3, xlate("pneumaticcraft.armor.moveStat.expand" + (movedStat.isLeftSided() ? "Left" : "Right")));
    }

    private void save() {
        Window sr = minecraft.getWindow();
        ArmorHUDLayout.INSTANCE.updateLayout(layoutItem,
                ((float) movedStat.getBaseX() / (float) sr.getGuiScaledWidth()),
                ((float) movedStat.getBaseY() / (float) sr.getGuiScaledHeight()),
                movedStat.isLeftSided());
    }
}
