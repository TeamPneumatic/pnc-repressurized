/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.client;

import net.minecraft.client.KeyMapping;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Key mapping categories used by PneumaticCraft. These are registered by PneumaticCraft itself; addons may use
 * them for their own key mappings (e.g. from {@link me.desht.pneumaticcraft.api.client.pneumatic_helmet.IArmorUpgradeClientHandler#getKeybindCategory()})
 * but must not register them again.
 */
public class PNCKeyCategories {
    public static final KeyMapping.Category MAIN = new KeyMapping.Category(RL("main"));
    public static final KeyMapping.Category UPGRADE_TOGGLES = new KeyMapping.Category(RL("upgrade_toggles"));
    public static final KeyMapping.Category BLOCK_TRACKER = new KeyMapping.Category(RL("block_tracker"));

    private PNCKeyCategories() {
    }
}
