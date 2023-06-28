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

package me.desht.pneumaticcraft.common.sensor.pollSensors;

import com.google.common.collect.ImmutableSet;
import me.desht.pneumaticcraft.api.universal_sensor.IPollSensorSetting;
import me.desht.pneumaticcraft.api.upgrade.PNCUpgrade;
import me.desht.pneumaticcraft.common.inventory.UniversalSensorMenu;
import me.desht.pneumaticcraft.common.upgrades.ModUpgrades;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class WorldGlobalVariableSensor implements IPollSensorSetting {
    protected UUID playerID;

    @Override
    public String getSensorPath() {
        return "World/Global variable";
    }

    @Override
    public Set<PNCUpgrade> getRequiredUpgrades() {
        return ImmutableSet.of(ModUpgrades.DISPENSER.get());
    }

    @Override
    public boolean needsTextBox() {
        return true;
    }

    @Override
    public int getPollFrequency(BlockEntity te) {
        return 1;
    }

    @Override
    public int getRedstoneValue(Level level, BlockPos pos, int sensorRange, String textBoxText) {
        return GlobalVariableHelper.getBool(playerID, textBoxText) ? 15 : 0;
    }

    @Override
    public void getAdditionalInfo(List<Component> info) {
        info.add(Component.literal("Variable Name"));
    }

    @Override
    public void setPlayerContext(UUID playerID) {
        this.playerID = playerID;
    }

    @Override
    public List<String> getTextBoxOptions(Player player) {
        return player.containerMenu instanceof UniversalSensorMenu c ? c.getGlobalVars() : null;
    }
}
