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

import com.google.common.collect.ImmutableList;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.render.HackSimulationRenderer;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.SecurityStationBlockEntity;
import me.desht.pneumaticcraft.common.hacking.secstation.HackSimulation;
import me.desht.pneumaticcraft.common.hacking.secstation.ISimulationController.HackingSide;
import me.desht.pneumaticcraft.common.inventory.SecurityStationHackingMenu;
import me.desht.pneumaticcraft.common.item.NetworkComponentItem.NetworkComponentType;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketGuiButton;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class SecurityStationHackingScreen extends AbstractPneumaticCraftContainerScreen<SecurityStationHackingMenu, SecurityStationBlockEntity> {
    private WidgetAnimatedStat statusStat;

    private HackSimulationRenderer hackRenderer;
    private HackSimulation bgSimulation;

    private int stopWorms = 0;
    private int nukeViruses = 0;

    private final ItemStack stopWorm = new ItemStack(ModItems.STOP_WORM.get());
    private final ItemStack nukeVirus = new ItemStack(ModItems.NUKE_VIRUS.get());
    private WidgetButtonExtended nukeVirusButton;
    private WidgetButtonExtended stopWormButton;

    public SecurityStationHackingScreen(SecurityStationHackingMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);

        imageHeight = 238;
    }

    @Override
    public void init() {
        super.init();

        statusStat = addAnimatedStat(xlate("pneumaticcraft.gui.securityStation.status"), new ItemStack(ModBlocks.SECURITY_STATION.get()), 0xFFFFAA00, false);

        addInfoTab(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.info.security_station.hacking"));
        addAnimatedStat(xlate(ModItems.NUKE_VIRUS.get().getDescriptionId()), new ItemStack(ModItems.NUKE_VIRUS.get()), 0xFF18c9e8, false)
                .setText(xlate("pneumaticcraft.gui.tab.info.security_station.nukeVirus"));
        addAnimatedStat(xlate(ModItems.STOP_WORM.get().getDescriptionId()), new ItemStack(ModItems.STOP_WORM.get()), 0xFFc13232, false)
                .setText(xlate("pneumaticcraft.gui.tab.info.security_station.stopWorm"));

        addRenderableWidget(nukeVirusButton = new WidgetButtonExtended(leftPos + 152, topPos + 95, 18, 18, "")
                .setRenderStacks(nukeVirus));
        addRenderableWidget(stopWormButton = new WidgetButtonExtended(leftPos + 152, topPos + 143, 18, 18, "", b -> {
            if (!te.getSimulationController().getSimulation(HackingSide.AI).isStopWormed()) {
                PneumaticCraftUtils.consumeInventoryItem(ClientUtils.getClientPlayer().getInventory(), ModItems.STOP_WORM.get());
                ClientUtils.getClientPlayer().playSound(SoundEvents.SLIME_BLOCK_BREAK, 1f, 1f);
            }
        })).withTag("stop_worm").setRenderStacks(stopWorm);

        initConnectionRendering();
    }

    @Override
    protected boolean shouldAddProblemTab() {
        return false;
    }

    private void initConnectionRendering() {
        hackRenderer = new HackSimulationRenderer(leftPos + 16, topPos + 30, SecurityStationHackingMenu.NODE_SPACING);
        bgSimulation = HackSimulation.dummySimulation();
        bgSimulation.wakeUp();
        for (int i = 0; i < te.getItemHandler().getSlots(); i++) {
            bgSimulation.addNode(i, te.getItemHandler().getStackInSlot(i));
        }
    }

    public static void addExtraHackInfoStatic(List<Component> curInfo) {
        if (Minecraft.getInstance().screen instanceof SecurityStationHackingScreen) {
            ((SecurityStationHackingScreen) Minecraft.getInstance().screen).addExtraHackInfo(curInfo);
        }
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_HACKING;
    }

    @Override
    protected boolean shouldAddInfoTab() {
        return false;
    }

    @Override
    protected boolean shouldAddUpgradeTab() {
        return false;
    }

    @Override
    protected boolean shouldAddRedstoneTab() {
        return false;
    }

    @Override
    protected PointXY getInvNameOffset() {
        return null;
    }

    @Override
    protected PointXY getInvTextOffset() {
        return null;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
        super.renderLabels(graphics, x, y);

        if (te.getSimulationController() != null) {
            HackSimulation aiSim = te.getSimulationController().getSimulation(HackingSide.AI);
            HackSimulation playerSim = te.getSimulationController().getSimulation(HackingSide.PLAYER);
            if (aiSim.isAwake()) {
                graphics.drawCenteredString(font, xlate("pneumaticcraft.gui.tooltip.hacking.aiTracing").withStyle(ChatFormatting.RED), imageWidth / 2, 7, 0xFFFFFF);
            } else {
                graphics.drawCenteredString(font, xlate("pneumaticcraft.gui.tooltip.hacking.detectionChance", te.getDetectionChance()).withStyle(ChatFormatting.GOLD), imageWidth / 2, 7, 0xFFFFFF);
            }

            if (aiSim.isHackComplete()) {
                ImmutableList.Builder<Component> builder = ImmutableList.builder();
                builder.add(xlate("pneumaticcraft.message.securityStation.hackFailed.1").withStyle(ChatFormatting.RED));
                if (!te.getSimulationController().isJustTesting()) {
                    builder.add(Component.empty());
                    builder.add(xlate("pneumaticcraft.message.securityStation.hackFailed.2").withStyle(ChatFormatting.RED));
                }
                GuiUtils.showPopupHelpScreen(graphics, this, font, builder.build());
            } else if (playerSim.isHackComplete()) {
                ImmutableList.Builder<Component> builder = ImmutableList.builder();
                builder.add(xlate("pneumaticcraft.message.securityStation.hackSucceeded.1").withStyle(ChatFormatting.GREEN));
                if (!te.getSimulationController().isJustTesting()) {
                    builder.add(Component.empty());
                    builder.add(xlate("pneumaticcraft.message.securityStation.hackSucceeded.2").withStyle(ChatFormatting.GREEN));
                }
                GuiUtils.showPopupHelpScreen(graphics, this, font, builder.build());
            }
        }
        renderConsumables(graphics);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        super.renderBg(graphics, partialTicks, x, y);

        hackRenderer.render(graphics, bgSimulation, 0xFF2222FF);
        if (te.getSimulationController() != null) {
            HackSimulation aiSim = te.getSimulationController().getSimulation(HackingSide.AI);
            if (!aiSim.isStopWormed() || (te.getLevel().getGameTime() & 0xf) < 8) {
                hackRenderer.render(graphics, te.getSimulationController().getSimulation(HackingSide.AI), 0xFFFF0000);
            }
            hackRenderer.render(graphics, te.getSimulationController().getSimulation(HackingSide.PLAYER), 0xFF00FF00);
        }
    }

    private void renderConsumables(GuiGraphics graphics) {
        graphics.drawString(font, PneumaticCraftUtils.convertAmountToString(nukeViruses), 158, 112, nukeViruses == 0 ? 0xFFFF6060: 0xFFFFFFFF, false);
        graphics.drawString(font, PneumaticCraftUtils.convertAmountToString(stopWorms), 158, 160, stopWorms == 0 ? 0xFFFF6060: 0xFFFFFFFF, false);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        stopWorms = 0;
        nukeViruses = 0;
        for (ItemStack stack : ClientUtils.getClientPlayer().getInventory().items) {
            if (stack.getItem() == ModItems.STOP_WORM.get()) stopWorms += stack.getCount();
            if (stack.getItem() == ModItems.NUKE_VIRUS.get()) nukeViruses += stack.getCount();
        }

        bgSimulation.tick();

        statusStat.setText(getStatusText());

        HackSimulation playerSim = te.getSimulationController() == null ? null : te.getSimulationController().getSimulation(HackingSide.PLAYER);
        HackSimulation aiSim = te.getSimulationController() == null ? null : te.getSimulationController().getSimulation(HackingSide.AI);

        if (aiSim != null && aiSim.isAwake()) {
            stopWormButton.active = stopWorms > 0;
            stopWormButton.setTooltipText(stopWorms > 0 ?
                    xlate("pneumaticcraft.gui.securityStation.stopWorm") :
                    xlate("pneumaticcraft.gui.securityStation.stopWorm.none").withStyle(ChatFormatting.GOLD)
            );
        } else {
            stopWormButton.active = false;
            stopWormButton.setTooltipText(xlate("pneumaticcraft.gui.securityStation.stopWorm.notTracing").withStyle(ChatFormatting.GOLD));
        }

        if (playerSim != null) {
            nukeVirusButton.active = hasNukeViruses() && playerSim.isNukeVirusReady();
            if (playerSim.isNukeVirusReady()) {
                nukeVirusButton.setTooltipText(hasNukeViruses() ?
                        xlate("pneumaticcraft.gui.securityStation.nukeVirus") :
                        xlate("pneumaticcraft.gui.securityStation.nukeVirus.none").withStyle(ChatFormatting.GOLD)
                );
            } else {
                nukeVirusButton.setTooltipText(xlate("pneumaticcraft.gui.securityStation.nukeVirus.coolDown").withStyle(ChatFormatting.GOLD));
            }
        }
    }

    private List<Component> getStatusText() {
        List<Component> text = new ArrayList<>();
        text.add(xlate("pneumaticcraft.gui.tab.status.securityStation.securityLevel").withStyle(ChatFormatting.WHITE));
        text.add(Component.literal("L" + te.getSecurityLevel()).withStyle(ChatFormatting.BLACK));
        text.add(xlate("pneumaticcraft.gui.tab.status.securityStation.securityRange").withStyle(ChatFormatting.WHITE));
        text.add(Component.literal((te.getRange() * 2 + 1) + "m²").withStyle(ChatFormatting.BLACK));
        return text;
    }

    @Override
    protected void slotClicked(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        // slotIn *can* be null here
        //noinspection ConstantConditions
        if (slotIn != null && slotIn.hasItem() && te.getSimulationController() != null) {
            switch (mouseButton) {
                case 0 -> tryHackSlot(slotId);
                case 1 -> tryFortifySlot(slotId);
                case 2 -> tryNukeVirus(slotId);
            }
        } else {
            super.slotClicked(slotIn, slotId, mouseButton, type);
        }
    }

    private void tryFortifySlot(int slotId) {
        HackSimulation playerSim = te.getSimulationController().getSimulation(HackingSide.PLAYER);
        HackSimulation.Node node = playerSim.getNodeAt(slotId);
        if (node.isHacked() && !node.isFortified() && node.getFortification() == 0) {
            playerSim.fortify(slotId);
            NetworkHandler.sendToServer(new PacketGuiButton("fortify:" + slotId));
        }
    }

    private void tryHackSlot(int slotId) {
        HackSimulation playerSim = te.getSimulationController().getSimulation(HackingSide.PLAYER);
        HackSimulation.Node node = playerSim.getNodeAt(slotId);
        if (!node.isHacked() && playerSim.getHackedNeighbour(slotId) >= 0) {
            playerSim.startHack(slotId);
            NetworkHandler.sendToServer(new PacketGuiButton("hack:" + slotId));
        }
    }

    private void tryNukeVirus(int slotId) {
        if (hasNukeViruses() && te.getSimulationController() != null) {
            HackSimulation playerSim = te.getSimulationController().getSimulation(HackingSide.PLAYER);
            HackSimulation.Node node = playerSim.getNodeAt(slotId);
            if (!node.isHacked() && playerSim.getHackedNeighbour(slotId) >= 0) {
                // node must have a hacked neighbour for this to work
                if (playerSim.initiateNukeVirus(slotId)) {
                    NetworkHandler.sendToServer(new PacketGuiButton("nuke:" + slotId));
                    PneumaticCraftUtils.consumeInventoryItem(ClientUtils.getClientPlayer().getInventory(), ModItems.NUKE_VIRUS.get());
                }
            }
        }
    }

    public void addExtraHackInfo(List<Component> toolTip) {
        if (hoveredSlot != null && te.getSimulationController() != null) {
            HackSimulation playerSim = te.getSimulationController().getSimulation(HackingSide.PLAYER);
            HackSimulation.Node node = playerSim.getNodeAt(hoveredSlot.index);
            if (node != null) {
                if (node.isHacked()) {
                    if (node.getFortification() == 0) {
                        toolTip.add(xlate("pneumaticcraft.gui.tooltip.hacking.rightClickFortify").withStyle(ChatFormatting.DARK_AQUA));
                    } else if (node.getFortificationProgress() < 1f) {
                        toolTip.add(xlate("pneumaticcraft.gui.tooltip.hacking.fortifyProgress", (int)(node.getFortificationProgress() * 100)).withStyle(ChatFormatting.DARK_AQUA));
                    } else {
                        toolTip.add(xlate("pneumaticcraft.gui.tooltip.hacking.fortified").withStyle(ChatFormatting.AQUA));
                    }
                } else {
                    if (playerSim.getHackedNeighbour(hoveredSlot.index) >= 0) {
                        if (node.getHackProgress() == 0F) {
                            toolTip.add(xlate("pneumaticcraft.gui.tooltip.hacking.leftClickHack").withStyle(ChatFormatting.GREEN));
                        } else {
                            toolTip.add(xlate("pneumaticcraft.gui.tooltip.hacking.hackProgress", (int)(node.getHackProgress() * 100)).withStyle(ChatFormatting.GREEN));
                        }
                        if (nukeViruses > 0 && playerSim.isNukeVirusReady() && node.getType() == NetworkComponentType.NETWORK_NODE) {
                            toolTip.add(xlate("pneumaticcraft.gui.tooltip.hacking.middleClickNuke").withStyle(ChatFormatting.YELLOW));
                        }
                    }
                }
            }
        }
    }

    boolean hasNukeViruses() {
        return nukeViruses > 0;
    }
}
