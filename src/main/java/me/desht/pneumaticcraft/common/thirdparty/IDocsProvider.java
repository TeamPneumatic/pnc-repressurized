package me.desht.pneumaticcraft.common.thirdparty;

import net.minecraft.util.text.ITextComponent;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public interface IDocsProvider {
    void showWidgetDocs(String path);

    default void addTooltip(List<ITextComponent> tooltip, boolean showingAll) {
        tooltip.add(xlate(showingAll ? "pneumaticcraft.gui.programmer.pressIForInfoTrayOpen" : "pneumaticcraft.gui.programmer.pressIForInfo"));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    default boolean isInstalled() {
        return false;
    }

    class NoDocsProvider implements IDocsProvider {
        @Override
        public void showWidgetDocs(String path) {
        }

        @Override
        public void addTooltip(List<ITextComponent> tooltip, boolean showingAll) {
        }
    }
}
