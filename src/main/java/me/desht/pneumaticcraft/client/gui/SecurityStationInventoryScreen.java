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

import com.mojang.authlib.GameProfile;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetRangeToggleButton;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTextField;
import me.desht.pneumaticcraft.client.render.HackSimulationRenderer;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.SecurityStationBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.SecurityStationBlockEntity.EnumNetworkValidityProblem;
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.hacking.secstation.HackSimulation;
import me.desht.pneumaticcraft.common.inventory.SecurityStationMainMenu;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SecurityStationInventoryScreen extends AbstractPneumaticCraftContainerScreen<SecurityStationMainMenu, SecurityStationBlockEntity> {
    private WidgetAnimatedStat statusStat;
    private WidgetAnimatedStat accessStat;

    private WidgetButtonExtended addUserButton;
    private Button rebootButton;
    private WidgetTextField sharedUserTextField;
    private List<WidgetButtonExtended> sharedUserList;

    // for cosmetic purposes only; draws the animated connection lines between nodes
    private HackSimulationRenderer hackRenderer;
    private HackSimulation hackSimulation;
    private boolean reInitBG;

    public SecurityStationInventoryScreen(SecurityStationMainMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);

        imageHeight = 239;
    }

    @Override
    public void init() {
        super.init();

        statusStat = addAnimatedStat(xlate("pneumaticcraft.gui.securityStation.status"),
                new ItemStack(ModBlocks.SECURITY_STATION.get()), 0xFFD08000, false);
        accessStat = addAnimatedStat(xlate("pneumaticcraft.gui.securityStation.sharedUsers"),
                new ItemStack(Items.PLAYER_HEAD), 0xFF005500, false);

        addUserButton = new WidgetButtonExtended(105, 12, 16, 16, "+", b -> {
            if (!sharedUserTextField.getValue().isEmpty()) {
                NetworkHandler.sendToServer(new PacketGuiButton("add:" + sharedUserTextField.getValue()));
                sharedUserTextField.setValue("");
            }
        });

        sharedUserTextField = new WidgetTextField(font, 20, 15, 80, 10);

        accessStat.addSubWidget(sharedUserTextField);
        accessStat.addSubWidget(addUserButton);
        accessStat.setMinimumExpandedDimensions(125, 40);

        addRenderableWidget(rebootButton = new WidgetButtonExtended(leftPos + 110, topPos + 17, 60, 20, xlate("pneumaticcraft.gui.securityStation.reboot")).withTag("reboot"));

        WidgetButtonExtended testButton = addRenderableWidget(new WidgetButtonExtended(leftPos + 110, topPos + 107, 60, 20, xlate("pneumaticcraft.gui.securityStation.test")))
                .withTag("test");
        if (!ConfigHelper.common().machines.securityStationAllowHacking.get()) {
            testButton.active = false;
            testButton.setTooltipText(xlate("pneumaticcraft.message.securityStation.hackDisabled"));
        }

        addRenderableWidget(new WidgetRangeToggleButton(leftPos + 154, topPos + 130, te));

        updateUserList();
        initConnectionRendering();
    }

    private void initConnectionRendering() {
        hackRenderer = new HackSimulationRenderer(leftPos + 25, topPos + 27, 18);
        hackSimulation = HackSimulation.dummySimulation();
        hackSimulation.wakeUp();
        for (int i = 0; i < te.getItemHandler().getSlots(); i++) {
            hackSimulation.addNode(i, te.getItemHandler().getStackInSlot(i));
        }
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_SECURITY_STATION;
    }

    @Override
    protected PointXY getInvTextOffset() {
        return new PointXY(0, 2);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float opacity, int x, int y) {
        super.renderBg(graphics, opacity, x, y);
        hackRenderer.render(graphics, hackSimulation, 0xFF2222FF);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        if (reInitBG) {
            initConnectionRendering();
            reInitBG = false;
        }

        hackSimulation.tick();

        statusStat.setText(getStatusText());
        accessStat.setText(getAccessText());
        Component rebootButtonString;
        if (te.getRebootTime() > 0) {
            rebootButtonString = te.getRebootTime() % 100 < 20 ?
                    xlate("pneumaticcraft.gui.securityStation.rebooting") :
                    Component.literal(PneumaticCraftUtils.convertTicksToMinutesAndSeconds(te.getRebootTime(), false));
        } else {
            rebootButtonString = xlate("pneumaticcraft.gui.securityStation.reboot").withStyle(ChatFormatting.RED);
        }
        rebootButton.setMessage(rebootButtonString);

        addUserButton.visible = accessStat.isDoneExpanding();
        for (WidgetButtonExtended button : sharedUserList) {
            button.active = accessStat.isDoneExpanding();
        }
        if (sharedUserList.size() != te.sharedUsers.size()) {
            updateUserList();
        }

        rebootButton.active = te.getRebootTime() == 0;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER && sharedUserTextField.isFocused() && !sharedUserTextField.getValue().isEmpty()) {
            NetworkHandler.sendToServer(new PacketGuiButton("add:" + sharedUserTextField.getValue()));
            sharedUserTextField.setValue("");
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    protected void addProblems(List<Component> text) {
        super.addProblems(text);

        if (te.getRebootTime() > 0) {
            text.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.security_station.rebooting", PneumaticCraftUtils.convertTicksToMinutesAndSeconds(te.getRebootTime(), false)));
        } else if (te.isHacked()) {
            text.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.security_station.hacked"));
        }
        if (!te.hasValidNetwork()) {
            text.add(xlate("pneumaticcraft.gui.tab.problems.security_station.invalidNetwork").withStyle(ChatFormatting.WHITE));
            EnumNetworkValidityProblem problem = te.checkForNetworkValidity();
            if (problem != EnumNetworkValidityProblem.NONE) text.addAll(GuiUtils.xlateAndSplit(problem.getTranslationKey()));
        }
    }

    private List<Component> getStatusText() {
        List<Component> text = new ArrayList<>();
        text.add(xlate("pneumaticcraft.gui.tab.status.securityStation.protection").withStyle(ChatFormatting.WHITE));
        if (te.getRebootTime() > 0) {
            text.add(Component.literal("  ").append(xlate("pneumaticcraft.gui.securityStation.rebooting")).withStyle(ChatFormatting.DARK_RED));
        } else if (te.isHacked()) {
            text.add(Component.literal("  ").append(xlate("pneumaticcraft.gui.tab.status.securityStation.hackedBy")).withStyle(ChatFormatting.DARK_RED));
            for (GameProfile hacker : te.hackedUsers) {
                text.add(Component.literal("  ").append(Symbols.bullet()).append(hacker.getName()).withStyle(ChatFormatting.RED));
            }
        } else if (te.hasValidNetwork()) {
            text.add(Component.literal("  ").append(xlate("pneumaticcraft.gui.tab.status.securityStation.secure")).withStyle(ChatFormatting.GREEN));
        } else {
            text.add(Component.literal("  -").withStyle(ChatFormatting.GRAY));
            return text;
        }
        text.add(xlate("pneumaticcraft.gui.tab.status.securityStation.securityLevel").withStyle(ChatFormatting.WHITE));
        text.add(Component.literal("  ").append(Component.literal("L" + te.getSecurityLevel())).withStyle(ChatFormatting.BLACK));
        text.add(xlate("pneumaticcraft.gui.tab.status.securityStation.detectChance").withStyle(ChatFormatting.WHITE));
        text.add(Component.literal("  ").append(Component.literal(te.getDetectionChance() + "%")).withStyle(ChatFormatting.BLACK));
        text.add(xlate("pneumaticcraft.gui.tab.status.securityStation.securityRange").withStyle(ChatFormatting.WHITE));
        text.add(Component.literal("  ").append(Component.literal((te.getRange() * 2 + 1) + "m²")).withStyle(ChatFormatting.BLACK));
        return text;
    }

    private List<Component> getAccessText() {
        List<Component> textList = new ArrayList<>();
        textList.add(Component.empty());
        textList.add(Component.empty());
        boolean first = true;
        List<String> names = te.sharedUsers.stream().map(GameProfile::getName).sorted().toList();
        for (String name : names) {
            String str = first ? name + " \u2654" : name;
            textList.add(Symbols.bullet().append(str).withStyle(first ? ChatFormatting.YELLOW : ChatFormatting.WHITE));
            first = false;
        }
        return textList;
    }

    private void updateUserList() {
        if (sharedUserList != null) {
            for (WidgetButtonExtended button : sharedUserList) {
                accessStat.removeSubWidget(button);
            }
        }
        sharedUserList = new ArrayList<>();
        int n = 0;
        List<String> names = te.sharedUsers.stream().map(GameProfile::getName).sorted().toList();
        for (String name : names) {
            WidgetButtonExtended button = new WidgetButtonExtended(24, 30 + n * (font.lineHeight + 1), font.width(name), 8, Component.empty(), b -> {})
                    .setVisible(false)
                    .setInvisibleHoverColor(0x80FF0000)
                    .withTag("remove:" + name);
            button.setInvisibleHoverColor(0x80FF0000);
            button.setVisible(false);
            accessStat.addSubWidget(button);
            sharedUserList.add(button);
            button.visible = !name.equals(ClientUtils.getClientPlayer().getGameProfile().getName());
            n++;
        }
    }

    public static void reinitConnectionRendering() {
        if (Minecraft.getInstance().screen instanceof SecurityStationInventoryScreen) {
            ((SecurityStationInventoryScreen) Minecraft.getInstance().screen).reInitBG = true;
        }
    }
}
