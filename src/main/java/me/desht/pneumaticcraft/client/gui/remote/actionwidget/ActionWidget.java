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

package me.desht.pneumaticcraft.client.gui.remote.actionwidget;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.client.gui.RemoteEditorScreen;
import me.desht.pneumaticcraft.client.gui.RemoteScreen;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.mixin.accessors.TooltipAccess;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;

public abstract class ActionWidget<W extends AbstractWidget> {
    protected W widget;
    protected BaseSettings baseSettings;
    protected final WidgetSettings widgetSettings;

    protected static <P extends ActionWidget<?>> Products.P2<RecordCodecBuilder.Mu<P>, BaseSettings, WidgetSettings> baseParts(RecordCodecBuilder.Instance<P> pInstance) {
        return pInstance.group(
                BaseSettings.CODEC.fieldOf("base").forGetter(a -> a.baseSettings),
                WidgetSettings.CODEC.fieldOf("widget").forGetter(a -> a.widgetSettings)
        );
    }

    protected ActionWidget(BaseSettings baseSettings, WidgetSettings widgetSettings) {
        this.baseSettings = baseSettings;
        this.widgetSettings = widgetSettings;
    }

    protected ActionWidget(WidgetSettings widgetSettings) {
        this(BaseSettings.DEFAULT, widgetSettings);
    }

    public abstract MapCodec<? extends ActionWidget<W>> codec();

    public abstract String getId();

    public ActionWidget<?> copy(HolderLookup.Provider provider) {
        try {
            RegistryOps<Tag> ops = provider.createSerializationContext(NbtOps.INSTANCE);
            Tag tag = ActionWidgets.CODEC.encodeStart(ops, this).getOrThrow();
            return ActionWidgets.CODEC.parse(ops, tag).getOrThrow();
        } catch (Exception e) {
            Log.error("Error occurred when trying to copy a {} action widget: {}", getId(), e.getMessage());
            return null;
        }
    }

    public final W getOrCreateMinecraftWidget(RemoteScreen screen) {
        if (widget == null) {
            widget = createMinecraftWidget(screen);
        }
        return widget;
    }

    protected abstract W createMinecraftWidget(RemoteScreen screen);

    public abstract Screen createConfigurationGui(RemoteEditorScreen guiRemote);

    public final void setWidgetPos(RemoteEditorScreen screen, int absX, int absY) {
        // widget X/Y are stored relative to screen left/top
        widgetSettings.setX(absX - screen.getGuiLeft());
        widgetSettings.setY(absY - screen.getGuiTop());

        if (widget != null) {
            widget.setPosition(absX, absY);
        }
    }

    public void setEnableVariable(String varName) {
        baseSettings = baseSettings.withVariable(varName);
    }

    public String getEnableVariable() {
        return baseSettings.enableVariable;
    }

    public boolean isEnabled() {
        if (getEnableVariable().isEmpty()) {
            return true;
        }
        BlockPos pos = GlobalVariableHelper.getPos(ClientUtils.getClientPlayer().getUUID(), getEnableVariable(), BlockPos.ZERO);
        return pos.equals(getEnablingValue());
    }

    public void setEnablingValue(int x, int y, int z) {
        baseSettings = baseSettings.withEnablingValue(new BlockPos(x, y, z));
    }

    public BlockPos getEnablingValue() {
        return baseSettings.enablingValue;
    }

    public void setTooltip(Tooltip tooltip) {
        widget.setTooltip(tooltip);
    }

    public Tooltip getTooltip() {
        return widget.getTooltip();
    }

    public Component getTooltipMessage(RemoteScreen screen) {
        Tooltip tooltip = getOrCreateMinecraftWidget(screen).getTooltip();
        return tooltip == null ? Component.empty() : ((TooltipAccess) tooltip).getMessage();
    }

    protected record BaseSettings(String enableVariable, BlockPos enablingValue) {
        public static final Codec<BaseSettings> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.STRING.fieldOf("enable_var").forGetter(BaseSettings::enableVariable),
                BlockPos.CODEC.fieldOf("pos").forGetter(BaseSettings::enablingValue)
        ).apply(builder, BaseSettings::new));

        public static final BaseSettings DEFAULT = new BaseSettings("", BlockPos.ZERO);

        public BaseSettings withVariable(String var) {
            return new BaseSettings(var, enablingValue);
        }

        public BaseSettings withEnablingValue(BlockPos value) {
            return new BaseSettings(enableVariable, value);
        }
    }
}