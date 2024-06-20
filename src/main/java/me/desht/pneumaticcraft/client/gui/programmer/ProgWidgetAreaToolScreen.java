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

package me.desht.pneumaticcraft.client.gui.programmer;

import me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidgetArea;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketUpdateGPSAreaTool;
import net.minecraft.world.InteractionHand;

/**
 * Area widget as used by the GPS Area Tool.
 */
public class ProgWidgetAreaToolScreen extends ProgWidgetAreaScreen {
    private final InteractionHand hand;
    private final Runnable returnAction;
    
    public ProgWidgetAreaToolScreen(ProgWidgetArea widget, InteractionHand hand, Runnable returnAction) {
        super(widget, null);

        this.hand = hand;
        this.returnAction = returnAction;
    }

    @Override
    public void removed() {
        super.removed();

        NetworkHandler.sendToServer(new PacketUpdateGPSAreaTool(progWidget, hand));
    }

    @Override
    public void onClose() {
        returnAction.run();
    }

    @Override
    public boolean displayShowAreaButtons() {
        return false;
    }
}
