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

package me.desht.pneumaticcraft.client.gui.semiblock;

import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.entity.semiblock.LogisticsRequesterEntity;
import me.desht.pneumaticcraft.common.inventory.LogisticsMenu;
import me.desht.pneumaticcraft.common.thirdparty.ae2.AE2Integration;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class LogisticsRequesterScreen extends AbstractLogisticsScreen<LogisticsRequesterEntity> {
    private WidgetCheckBox aeIntegration;

    public LogisticsRequesterScreen(LogisticsMenu menu, Inventory inv, Component displayString) {
        super(menu, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.ghostSlotInteraction.title"), Textures.GUI_MOUSE_LOCATION, 0xFF00AAFF, true)
                .setText(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.info.ghostSlotInteraction"));

//        if (AE2Integration.isAvailable() && logistics.getAE2integration().isPlacedOnInterface()) {
//            addAE2Tab();
//        }
    }

    @Override
    protected void updateLabels() {
        itemLabel.setMessage(xlate("pneumaticcraft.gui.logistics_frame.requesting_items"));
        fluidLabel.setMessage(xlate("pneumaticcraft.gui.logistics_frame.requesting_fluids"));
    }

//    private void addAE2Tab() {
//        Item item = AE2PNCAddon.glassCable();
//        if (item == null) {
//            Log.warning("AE2 cable couldn't be found!");
//            item = ModItems.LOGISTICS_FRAME_REQUESTER.get();
//        }
//        WidgetAnimatedStat stat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.info.logisticsRequester.aeIntegration.title"),
//                new ItemStack(item), 0xFF00AAFF, false);
//        stat.setText(xlate("pneumaticcraft.gui.tab.info.logisticsRequester.aeIntegration"));
//        stat.addSubWidget(aeIntegration = new WidgetCheckBox(16, 13, 0xFF000000,
//                xlate("pneumaticcraft.gui.tab.info.logisticsRequester.aeIntegration.enable"))
//                .withTag("ae2")
//        );
//        stat.setReservedLines(2);
//    }

    @Override
    public void containerTick() {
        super.containerTick();

        if (AE2Integration.isAvailable() && aeIntegration != null) {
            aeIntegration.checked = logistics.isAE2enabled();
        }
    }

}
