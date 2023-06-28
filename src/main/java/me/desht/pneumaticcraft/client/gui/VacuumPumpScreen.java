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

import me.desht.pneumaticcraft.api.PNCCapabilities;
import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.api.tileentity.IAirHandlerMachine;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D;
import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.block.entity.VacuumPumpBlockEntity;
import me.desht.pneumaticcraft.common.inventory.VacuumPumpMenu;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.PneumaticValues;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class VacuumPumpScreen extends AbstractPneumaticCraftContainerScreen<VacuumPumpMenu,VacuumPumpBlockEntity> {

    public VacuumPumpScreen(VacuumPumpMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_VACUUM_PUMP;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
        super.renderLabels(graphics, x, y);

        graphics.drawString(font, "+", 32, 47, 0xFF00AA00, false);
        graphics.drawString(font, "-", 138, 47, 0xFFFF0000, false);

        float pressure = te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, te.getInputSide())
                .orElseThrow(RuntimeException::new).getPressure();
        PressureGaugeRenderer2D.drawPressureGauge(graphics, font, -1, PneumaticValues.MAX_PRESSURE_VACUUM_PUMP,
                PneumaticValues.DANGER_PRESSURE_VACUUM_PUMP, PneumaticValues.MIN_PRESSURE_VACUUM_PUMP, pressure,
                imageWidth / 5, imageHeight / 5 + 4);

        float vacPressure = te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, te.getVacuumSide())
                .orElseThrow(RuntimeException::new).getPressure();
        PressureGaugeRenderer2D.drawPressureGauge(graphics, font, -1, PneumaticValues.MAX_PRESSURE_VACUUM_PUMP,
                PneumaticValues.DANGER_PRESSURE_VACUUM_PUMP, -1, vacPressure,
                imageWidth * 4 / 5, imageHeight / 5 + 4);
    }

    @Override
    protected PointXY getGaugeLocation() {
        return null;
    }

    @Override
    protected void addPressureStatInfo(List<Component> pressureStatText) {
        IAirHandlerMachine inputAirHandler = te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, te.getInputSide())
                .orElseThrow(RuntimeException::new);
        IAirHandlerMachine vacuumHandler = te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, te.getVacuumSide())
                .orElseThrow(RuntimeException::new);

        pressureStatText.add(xlate("pneumaticcraft.gui.tab.status.vacuumPump.inputPressure",
                PneumaticCraftUtils.roundNumberTo(inputAirHandler.getPressure(), 2)));
        pressureStatText.add(xlate("pneumaticcraft.gui.tab.status.vacuumPump.vacuumPressure",
                PneumaticCraftUtils.roundNumberTo(vacuumHandler.getPressure(), 2)));
        pressureStatText.add(xlate("pneumaticcraft.gui.tab.status.vacuumPump.inputAir",
                String.format("%,d", inputAirHandler.getAir())));
        pressureStatText.add(xlate("pneumaticcraft.gui.tab.status.vacuumPump.vacuumAir",
                String.format("%,d", vacuumHandler.getAir())));

        int volume = inputAirHandler.getVolume();
        int upgrades = te.getUpgrades(ModUpgrades.VOLUME.get());
        pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.baseVolume",
                String.format("%,d", PneumaticValues.VOLUME_VACUUM_PUMP)));
        if (volume > inputAirHandler.getBaseVolume()) {
            pressureStatText.add(Component.literal(Symbols.TRIANGLE_RIGHT + " " + upgrades + " x ")
                    .append(ModUpgrades.VOLUME.get().getItemStack().getHoverName())
            );
            pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.effectiveVolume", String.format("%,d",volume)));
        }

        if (te.turning) {
            int suction = Math.round(PneumaticValues.PRODUCTION_VACUUM_PUMP * te.getSpeedMultiplierFromUpgrades());
            pressureStatText.add(xlate("pneumaticcraft.gui.tooltip.suction", String.format("%,d", suction)));
        }
    }

    @Override
    protected void addProblems(List<Component> textList) {
        super.addProblems(textList);
        float pressure = te.getCapability(PNCCapabilities.AIR_HANDLER_MACHINE_CAPABILITY, te.getInputSide())
                .map(IAirHandlerMachine::getPressure).orElseThrow(RuntimeException::new);
        if (pressure < PneumaticValues.MIN_PRESSURE_VACUUM_PUMP) {
            textList.add(xlate("pneumaticcraft.gui.tab.problems.notEnoughPressure"));
            textList.add(xlate("pneumaticcraft.gui.tab.problems.applyPressure", PneumaticValues.MIN_PRESSURE_VACUUM_PUMP).withStyle(ChatFormatting.BLACK));
        }
    }

}
