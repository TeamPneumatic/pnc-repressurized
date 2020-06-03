package me.desht.pneumaticcraft.client.gui.tubemodule;

import me.desht.pneumaticcraft.client.gui.widget.WidgetColorSelector;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.common.block.tubes.ModuleLogistics;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketTubeModuleColor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class GuiLogisticsModule extends GuiTubeModule<ModuleLogistics> {
    private int ourColor;

    public GuiLogisticsModule(BlockPos modulePos) {
        super(modulePos);

        ySize = 57;
    }

    @Override
    public void init() {
        super.init();

        ourColor = module.getColorChannel();

        WidgetLabel ourColorLabel;
        addButton(ourColorLabel = new WidgetLabel(guiLeft + 10, guiTop + 25, I18n.format("gui.tubeModule.channel")));

        addLabel(getTitle().getFormattedText(), guiLeft + xSize / 2, guiTop + 5, WidgetLabel.Alignment.CENTRE);

        addButton(new WidgetColorSelector(guiLeft + 10 + ourColorLabel.getWidth() + 5, guiTop + 22, w -> ourColor = w.getColor().getId())
                .withInitialColor(DyeColor.byId(ourColor)));
    }

    @Override
    protected ResourceLocation getTexture() {
        return Textures.GUI_MODULE_SIMPLE;
    }

    @Override
    public void onClose() {
        super.onClose();

        module.setColorChannel(ourColor);
        NetworkHandler.sendToServer(new PacketTubeModuleColor(module));
    }
}
