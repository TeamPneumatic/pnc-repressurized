package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetButtonExtended;
import me.desht.pneumaticcraft.client.gui.widget.WidgetCheckBox;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.item.ClassifyFilterItem;
import me.desht.pneumaticcraft.common.item.ClassifyFilterItem.FilterCondition;
import me.desht.pneumaticcraft.common.item.ClassifyFilterItem.FilterSettings;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketSyncClassifyFilter;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

import java.util.EnumSet;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ClassifyFilterScreen extends AbstractPneumaticCraftScreen {
    private final InteractionHand handIn;
    private final EnumSet<FilterCondition> conditions = EnumSet.noneOf(FilterCondition.class);
    private boolean matchAll;
    private WidgetButtonExtended matchButton;

    private ClassifyFilterScreen(Component title, InteractionHand handIn) {
        super(title);
        this.handIn = handIn;
        xSize = 256;
        ySize = 202;

        FilterSettings settings = FilterSettings.fromStack(ClientUtils.getClientPlayer().getItemInHand(handIn));
        this.conditions.addAll(settings.filterConditions());
        this.matchAll = settings.matchAll();
    }

    public static void openGui(Component title, InteractionHand handIn) {
        Minecraft.getInstance().setScreen(new ClassifyFilterScreen(title, handIn));
    }

    @Override
    public void init() {
        super.init();

        int x = guiLeft + 55;
        int y = guiTop + 20;

        for (FilterCondition cond : FilterCondition.values()) {
            WidgetCheckBox cb = addRenderableWidget(new WidgetCheckBox(x + 20, y + 5, 20, Component.empty(), b -> toggleCondition(cond))
                    .setChecked(conditions.contains(cond))
            );
            cb.setTooltip(Tooltip.create(xlate(cond.getTranslationKey())));
            var btn = new WidgetButtonExtended(x, y, 20, 20, "", b -> {
                toggleCondition(cond);
                cb.setChecked(conditions.contains(cond));
            }).setRenderStacks(cond.getIcon()).setVisible(false);
            btn.setTooltip(Tooltip.create(xlate(cond.getTranslationKey())));
            addRenderableWidget(btn);
            y += 20;
            if (y > guiTop + 140) {
                x += 100;
                y = guiTop + 20;
            }
        }

        Component txtAny = ClassifyFilterItem.xlateMatch(false);
        Component txtAll = ClassifyFilterItem.xlateMatch(true);
        int w = Math.max(font.width(txtAll), font.width(txtAny)) + 20;
        matchButton = addRenderableWidget(new WidgetButtonExtended(guiLeft + (xSize - w) / 2, guiTop + 170, w, 20,
                matchAll ? txtAll : txtAny, b -> toggleMatchAll()));
    }

    private void toggleMatchAll() {
        matchAll = !matchAll;
        matchButton.setMessage(ClassifyFilterItem.xlateMatch(matchAll));
        NetworkHandler.sendToServer(new PacketSyncClassifyFilter(matchAll, conditions, handIn));
    }

    private void toggleCondition(FilterCondition cond) {
        if (conditions.contains(cond)) {
            conditions.remove(cond);
        } else {
            conditions.add(cond);
        }
        NetworkHandler.sendToServer(new PacketSyncClassifyFilter(matchAll, conditions, handIn));
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float partialTicks) {
        renderBackground(graphics, x, y, partialTicks);

        super.render(graphics, x, y, partialTicks);
    }

    @Override
    protected void drawForeground(GuiGraphics graphics, int x, int y, float partialTicks) {
        super.drawForeground(graphics, x, y, partialTicks);

        graphics.drawString(font, title, guiLeft + (xSize - font.width(title)) / 2, guiTop + 8, 0x404040, false);
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_WIDGET_AREA;
    }
}
