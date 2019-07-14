package me.desht.pneumaticcraft.common.thirdparty;

import net.minecraft.util.text.ITextComponent;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public interface IDocsProvider {
    void showWidgetDocs(String path);

    default void addTooltip(List<ITextComponent> tooltip, boolean showingAll) {
        tooltip.add(xlate(showingAll ? "gui.programmer.pressIForInfoTrayOpen" : "gui.programmer.pressIForInfo"));
    }

    default boolean docsProviderInstalled() {
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
