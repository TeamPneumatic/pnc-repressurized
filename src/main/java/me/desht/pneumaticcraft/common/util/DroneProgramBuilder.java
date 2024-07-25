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

package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.common.drone.ProgWidgetUtils;
import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to build simple (no jumping) Drone programs, without needing to worry about the X/Y locations of widgets
 *
 * @author MineMaarten
 */
public class DroneProgramBuilder {
    private final List<DroneInstruction> instructions = new ArrayList<>();

    public void add(IProgWidget mainInstruction, IProgWidget... whitelist) {
        instructions.add(new DroneInstruction(mainInstruction, Arrays.asList(whitelist)));
    }

    public List<IProgWidget> build() {
        List<IProgWidget> allWidgets = new ArrayList<>();
        int curY = 0;
        for (DroneInstruction instruction : instructions) {
            instruction.mainInstruction.setPosition(0, curY);

            // Add whitelist pieces
            if (!instruction.whitelist.isEmpty()) {
                for (int paramIdx = 0; paramIdx < instruction.mainInstruction.getParameters().size(); paramIdx++) {
                    ProgWidgetType<?> type = instruction.mainInstruction.getParameters().get(paramIdx);
                    List<IProgWidget> whitelist = instruction.whitelist.stream()
                            .filter(w -> type == w.getType()).toList();
                    int curX = instruction.mainInstruction.getWidth() / 2;
                    for (IProgWidget whitelistItem : whitelist) {
                        whitelistItem.setPosition(curX, curY + paramIdx * ProgWidget.PROGWIDGET_HEIGHT / 2);
                        curX += whitelistItem.getWidth() / 2;
                    }
                }
            }

            curY += instruction.mainInstruction.getHeight() / 2;
            instruction.addToWidgets(allWidgets);
        }
        ProgWidgetUtils.updatePuzzleConnections(allWidgets);
        return allWidgets;
    }

    private record DroneInstruction(IProgWidget mainInstruction, List<IProgWidget> whitelist) {
        void addToWidgets(List<IProgWidget> widgets) {
            widgets.add(mainInstruction);
            widgets.addAll(whitelist);
        }
    }
}
