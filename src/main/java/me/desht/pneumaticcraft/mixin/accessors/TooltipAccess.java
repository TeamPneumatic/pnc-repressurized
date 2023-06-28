package me.desht.pneumaticcraft.mixin.accessors;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Tooltip.class)
public interface TooltipAccess {
    @Accessor
    Component getMessage();
}
