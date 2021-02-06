package me.desht.pneumaticcraft.client.gui.tubemodule;

import me.desht.pneumaticcraft.client.gui.widget.WidgetColorSelector;
import me.desht.pneumaticcraft.client.gui.widget.WidgetLabel;
import me.desht.pneumaticcraft.common.block.tubes.ModuleLogistics;
import me.desht.pneumaticcraft.common.network.NetworkHandler;
import me.desht.pneumaticcraft.common.network.PacketTubeModuleColor;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiLogisticsModule extends GuiTubeModule<ModuleLogistics> {
    private int ourColor;

    public GuiLogisticsModule(ModuleLogistics module) {
        super(module);

        ySize = 57;
    }

    @Override
    public void init() {
        super.init();

        ourColor = module.getColorChannel();

        WidgetLabel ourColorLabel;
        addButton(ourColorLabel = new WidgetLabel(guiLeft + 10, guiTop + 25, xlate("pneumaticcraft.gui.tubeModule.channel")));

        addLabel(getTitle(), guiLeft + xSize / 2, guiTop + 5, WidgetLabel.Alignment.CENTRE);

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
