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

package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTooltipArea;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.inventory.JackhammerSetupMenu;
import me.desht.pneumaticcraft.common.item.ItemDrillBit;
import me.desht.pneumaticcraft.common.item.ItemDrillBit.DrillBitType;
import me.desht.pneumaticcraft.common.item.ItemJackHammer;
import me.desht.pneumaticcraft.common.item.ItemJackHammer.DigMode;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.tileentity.AbstractPneumaticCraftBlockEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiJackHammerSetup extends GuiPneumaticContainerBase<JackhammerSetupMenu, AbstractPneumaticCraftBlockEntity> {
    private final EnumMap<DigMode,WidgetButtonExtended> typeButtons = new EnumMap<>(DigMode.class);
    private WidgetButtonExtended selectorButton;

    public GuiJackHammerSetup(JackhammerSetupMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);

        imageHeight = 182;
    }

    @Override
    public void init() {
        super.init();

        ItemStack hammerStack = ClientUtils.getClientPlayer().getItemInHand(menu.getHand());

        DigMode digMode = ItemJackHammer.getDigMode(hammerStack);

        if (digMode != null) {
            addRenderableWidget(selectorButton = new WidgetButtonExtended(leftPos + 127, topPos + 67, 20, 20,
                    TextComponent.EMPTY, b -> toggleShowChoices()))
                    .setRenderedIcon(digMode.getGuiIcon());

            int xBase = 147 - 20 * DigMode.values().length;
            for (DigMode dm : DigMode.values()) {
                WidgetButtonExtended button = new WidgetButtonExtended(leftPos + xBase, topPos + 47, 20, 20,
                        TextComponent.EMPTY, b -> selectDigMode(dm))
                        .setRenderedIcon(dm.getGuiIcon())
                        .withTag("digmode:" + dm);
                xBase += 20;
                button.visible = false;
                typeButtons.put(dm, button);
                addRenderableWidget(button);
            }
        }

        addRenderableWidget(new WidgetTooltipArea(leftPos + 96, topPos + 19, 18, 18) {
            @Override
            public void addTooltip(double mouseX, double mouseY, List<Component> curTip, boolean shiftPressed) {
                if (!menu.slots.get(1).hasItem()) {
                    curTip.add(xlate("pneumaticcraft.gui.tooltip.jackhammer.enchantedBookTip"));
                }
            }
        });
    }

    private void selectDigMode(DigMode digMode) {
        // communication to server handled via PacketGuiButton
        typeButtons.values().forEach(button -> button.visible = false);
        selectorButton.setRenderedIcon(digMode.getGuiIcon());
    }

    private void toggleShowChoices() {
        typeButtons.values().forEach(button -> button.visible = !button.visible);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        updateDigModeButtons();
    }

    private void updateDigModeButtons() {
        ItemStack drillStack = menu.getSlot(0).getItem();
        DrillBitType bitType = drillStack.getItem() instanceof ItemDrillBit ?
                ((ItemDrillBit) drillStack.getItem()).getType() :
                DrillBitType.NONE;

        ItemStack hammerStack = ClientUtils.getClientPlayer().getItemInHand(menu.getHand());
        DigMode digMode = ItemJackHammer.getDigMode(hammerStack);
        if (digMode == null) digMode = DigMode.MODE_1X1;

        typeButtons.forEach((dm, button) -> button.active = bitType.getHarvestLevel() >= dm.getBitType().getHarvestLevel());

        if (digMode.getBitType().getHarvestLevel() > bitType.getHarvestLevel() && digMode != DigMode.MODE_1X1) {
            // jackhammer currently has a selected dig type of a tier too high for the installed drill bit
            digMode = DigMode.MODE_1X1;
            NetworkHandler.sendToServer(new PacketGuiButton("digmode:" + digMode));
        }

        selectorButton.setRenderedIcon(digMode.getGuiIcon());
    }

    @Override
    protected void addProblems(List<Component> curInfo) {
        super.addProblems(curInfo);

        if (!menu.slots.get(0).hasItem()) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.jackhammer.noBit"));
        }
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_JACKHAMMER_SETUP;
    }
}
