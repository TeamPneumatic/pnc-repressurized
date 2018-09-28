package me.desht.pneumaticcraft.api.hacking;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class CapabilityHacking {
    @CapabilityInject(IHacking.class)
    public static final Capability<IHacking> HACKING_CAPABILITY = null;
}
