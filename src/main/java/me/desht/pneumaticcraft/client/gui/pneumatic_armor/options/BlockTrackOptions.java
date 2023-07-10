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

package me.desht.pneumaticcraft.client.gui.pneumatic_armor.options;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.client.pneumatic_helmet.*;
import me.desht.pneumaticcraft.api.pneumatic_armor.IArmorUpgradeHandler;
import me.desht.pneumaticcraft.client.pneumatic_armor.ClientArmorRegistry;
import me.desht.pneumaticcraft.client.pneumatic_armor.block_tracker.BlockTrackHandler;
import me.desht.pneumaticcraft.client.pneumatic_armor.upgrade_handler.BlockTrackerClientHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class BlockTrackOptions extends IOptionPage.SimpleOptionPage<BlockTrackerClientHandler> {
    public BlockTrackOptions(IGuiScreen screen, BlockTrackerClientHandler renderHandler) {
        super(screen, renderHandler);
    }

    @Override
    public void populateGui(IGuiScreen gui) {
        gui.addWidget(ClientArmorRegistry.getInstance().makeStatMoveButton(30, settingsYposition() + 12, getClientUpgradeHandler()));

        ResourceLocation blockTrackerID = CommonUpgradeHandlers.blockTrackerHandler.getID();

        List<IBlockTrackEntry> entries = BlockTrackHandler.getInstance().getEntries();
        ResourceLocation owningId = getClientUpgradeHandler().getID();
        IClientArmorRegistry registry = PneumaticRegistry.getInstance().getClientArmorRegistry();
        for (int i = 0; i < entries.size(); i++) {
            ICheckboxWidget checkBox = registry.makeKeybindingCheckBox(
                    entries.get(i).getEntryID(), 5, 53 + i * 12, 0xFFFFFFFF, cb -> {
                        ResourceLocation subID = cb.getUpgradeId();
                        HUDHandler.getInstance().addFeatureToggleMessage(IArmorUpgradeHandler.getStringKey(blockTrackerID), IArmorUpgradeHandler.getStringKey(subID), cb.isChecked());
                    }).withOwnerUpgradeID(owningId);
            gui.addWidget(checkBox.asWidget());
        }
    }

    @Override
    public boolean displaySettingsHeader() {
        return true;
    }

    @Override
    public int settingsYposition() {
        return 60 + 12 * BlockTrackHandler.getInstance().getEntries().size();
    }
}
