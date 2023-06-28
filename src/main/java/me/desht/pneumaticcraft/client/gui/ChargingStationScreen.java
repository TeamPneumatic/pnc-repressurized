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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.ChargingStationBlockEntity;
import me.desht.pneumaticcraft.common.inventory.ChargingStationMenu;
import me.desht.pneumaticcraft.common.item.IChargeableContainerProvider;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ChargingStationScreen extends AbstractPneumaticCraftContainerScreen<ChargingStationMenu,ChargingStationBlockEntity> {
    private static final int PARTICLE_COUNT = 10;

    private WidgetButtonExtended guiSelectButton;
    private WidgetButtonExtended upgradeOnlyButton;
    private float renderAirProgress;

    private static final Component UPGRADE_ONLY_ON = Component.literal("\u2b06").withStyle(ChatFormatting.AQUA);
    private static final Component UPGRADE_ONLY_OFF = Component.literal("\u2b06").withStyle(ChatFormatting.GRAY);

    public ChargingStationScreen(ChargingStationMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);

        imageHeight = 182;
    }

    @Override
    public void init() {
        super.init();

        guiSelectButton = new WidgetButtonExtended(leftPos + 90, topPos + 22, 18, 19, Component.empty()).withTag("open_upgrades");
        guiSelectButton.setRenderedIcon(Textures.GUI_UPGRADES_LOCATION);
        guiSelectButton.visible = false;
        addRenderableWidget(guiSelectButton);

        addRenderableWidget(upgradeOnlyButton = new WidgetButtonExtended(leftPos + 129, topPos + 80, 14, 14, "U")
                .withTag("toggle_upgrade_only"));
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_CHARGING_STATION;
    }

    @Override
    protected PointXY getInvTextOffset() {
        return null;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        super.renderBg(graphics, partialTicks, x, y);

        if (te.upgradeOnly) {
            graphics.blit(getGuiTexture(), leftPos + 102, topPos + 76, 177, 0, 13, 16);
        } else {
            renderAir(graphics, partialTicks);
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();

        ItemStack stack = te.getPrimaryInventory().getStackInSlot(ChargingStationBlockEntity.CHARGE_INVENTORY_INDEX);
        guiSelectButton.visible = stack.getItem() instanceof IChargeableContainerProvider;
        if (guiSelectButton.visible) {
            guiSelectButton.setTooltipText(xlate("pneumaticcraft.gui.tooltip.charging_station.manageUpgrades", stack.getHoverName()));
        }

        // multiplier of 25 is about the max that looks good (higher values can make the animation look like
        // it's going the wrong way)
        if (!te.upgradeOnly) {
            if (te.charging) {
                renderAirProgress += 0.001F * Math.min(25f, te.getSpeedMultiplierFromUpgrades());
                if (renderAirProgress > 1f) renderAirProgress = 0f;
            } else if (te.discharging) {
                renderAirProgress -= 0.001F * Math.min(25f, te.getSpeedMultiplierFromUpgrades());
                if (renderAirProgress < 0f) renderAirProgress = 1f;
            }
        }

        upgradeOnlyButton.setMessage(te.upgradeOnly ? UPGRADE_ONLY_ON : UPGRADE_ONLY_OFF);
    }

    @Override
    protected PointXY getGaugeLocation() {
        return new PointXY(leftPos + imageWidth * 3 / 4 + 10, topPos + imageHeight / 4 + 10);
    }

    @Override
    protected void addPressureStatInfo(List<Component> pressureStatText) {
        super.addPressureStatInfo(pressureStatText);
        if (te.charging || te.discharging) {
            String key = te.charging ? "pneumaticcraft.gui.tooltip.charging" : "pneumaticcraft.gui.tooltip.discharging";
            String amount = PneumaticCraftUtils.roundNumberTo(PneumaticValues.CHARGING_STATION_CHARGE_RATE * te.getSpeedMultiplierFromUpgrades(), 1);
            pressureStatText.add(xlate(key, amount).withStyle(ChatFormatting.BLACK));
        } else {
            pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.charging", 0).withStyle(ChatFormatting.BLACK));
        }
    }

    @Override
    protected void addProblems(List<Component> textList) {
        super.addProblems(textList);
        ItemStack chargeStack  = te.getPrimaryInventory().getStackInSlot(ChargingStationBlockEntity.CHARGE_INVENTORY_INDEX);
        if (!chargeStack.isEmpty() && !chargeStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).isPresent()) {
            // shouldn't ever happen - I can't be bothered to add a translation
            textList.add(Component.literal(ChatFormatting.RED + "Non-pneumatic item in the charge slot!?"));
        }
    }

    @Override
    protected void addWarnings(List<Component> curInfo) {
        super.addWarnings(curInfo);
        ItemStack chargeStack  = te.getPrimaryInventory().getStackInSlot(ChargingStationBlockEntity.CHARGE_INVENTORY_INDEX);
        if (chargeStack.isEmpty()) {
            curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.charging_station.no_item"));
        } else if (!te.upgradeOnly) {
            chargeStack.getCapability(PNCCapabilities.AIR_HANDLER_ITEM_CAPABILITY).ifPresent(h -> {
                String name = chargeStack.getHoverName().getString();
                if (h.getPressure() > te.getPressure() + 0.01F && h.getPressure() <= 0) {
                    curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.charging_station.item_empty", name));
                } else if (h.getPressure() < te.getPressure() - 0.01F && h.getPressure() >= h.maxPressure()) {
                    curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.charging_station.item_full", name));
                } else if (!te.charging && !te.discharging) {
                    curInfo.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.charging_station.pressure_equal", name));
                }
            });
        }
    }

    private void renderAir(GuiGraphics graphics, float partialTicks) {
        RenderSystem.lineWidth(2.0F);
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            renderAirParticle(graphics, renderAirProgress % (1F / PARTICLE_COUNT) + (float) i / PARTICLE_COUNT);
        }
    }

    private void renderAirParticle(GuiGraphics graphics, float particleProgress) {
        int xStart = (width - imageWidth) / 2;
        int yStart = (height - imageHeight) / 2;
        float x = xStart + 117F;
        float y = yStart + 56.5F;
        if (particleProgress < 0.5F) {
            y += particleProgress * 56;
        } else if (particleProgress < 0.7F) {
            y += 28F;
            x -= (particleProgress - 0.5F) * 90;
        } else {
            y += 28F;
            x -= 18;
            y -= (particleProgress - 0.7F) * 70;
        }
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f posMat = graphics.pose().last().pose();
        bufferbuilder.vertex(posMat, x - 1f, y + 1f, 0.0F).color(0.7f, 0.8f, 0.9f, 1f).endVertex();
        bufferbuilder.vertex(posMat, x + 1f, y + 1f, 0.0F).color(0.6f, 0.7f, 0.7f, 1f).endVertex();
        bufferbuilder.vertex(posMat, x + 1f, y - 1f, 0.0F).color(0.7f, 0.8f, 0.9f, 1f).endVertex();
        bufferbuilder.vertex(posMat, x - 1f, y - 1f, 0.0F).color(0.8f, 0.9f, 0.9f, 1f).endVertex();
        Tesselator.getInstance().end();
    }
}
