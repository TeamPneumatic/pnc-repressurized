package me.desht.pneumaticcraft.common.thirdparty;

import net.minecraft.client.resources.I18n;

import java.util.List;

public interface IDocsProvider {
    void showWidgetDocs(String path);

    default void addTooltip(List<String> tooltip, boolean showingAll) {
        tooltip.add(I18n.format(showingAll ? "gui.programmer.pressIForInfoTrayOpen" : "gui.programmer.pressIForInfo"));
    }

    class NoDocsProvider implements IDocsProvider {
        @Override
        public void showWidgetDocs(String path) {
        }

        @Override
        public void addTooltip(List<String> tooltip, boolean showingAll) {
        }
    }
}
