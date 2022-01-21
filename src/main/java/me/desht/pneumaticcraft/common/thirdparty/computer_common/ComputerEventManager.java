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

package me.desht.pneumaticcraft.common.thirdparty.computer_common;

import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Mod-agnostic class for sending events to computers.
 */
public enum ComputerEventManager {
    INSTANCE;

    private final List<IComputerEventSender> senders = new ArrayList<>();

    public static ComputerEventManager getInstance() {
        return INSTANCE;
    }

    public void registerSender(IComputerEventSender sender) {
        senders.add(sender);
    }

    public void sendEvents(BlockEntity te, String name, Object... params) {
        senders.forEach(s -> s.sendEvent(te, name, params));
    }

    @FunctionalInterface
    public interface IComputerEventSender {
        void sendEvent(BlockEntity te, String name, Object... params);
    }
}
