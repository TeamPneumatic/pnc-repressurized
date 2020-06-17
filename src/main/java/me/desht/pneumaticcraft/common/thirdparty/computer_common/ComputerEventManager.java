package me.desht.pneumaticcraft.common.thirdparty.computer_common;

import net.minecraft.tileentity.TileEntity;

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

    public void sendEvents(TileEntity te, String name, Object... params) {
        senders.forEach(s -> s.sendEvent(te, name, params));
    }

    @FunctionalInterface
    public interface IComputerEventSender {
        void sendEvent(TileEntity te, String name, Object... params);
    }
}
