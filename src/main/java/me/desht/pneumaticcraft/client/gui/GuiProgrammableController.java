package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.WidgetEnergy;
import me.desht.pneumaticcraft.common.ai.IDroneBase;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.inventory.ContainerProgrammableController;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammableController;
import me.desht.pneumaticcraft.lib.GuiConstants;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.energy.CapabilityEnergy;

import java.util.List;
import java.util.stream.Collectors;

public class GuiProgrammableController extends GuiPneumaticContainerBase<ContainerProgrammableController,TileEntityProgrammableController>
        implements IGuiDrone
{

    public GuiProgrammableController(ContainerProgrammableController container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();

        te.getCapability(CapabilityEnergy.ENERGY).ifPresent(handler -> addButton(new WidgetEnergy(guiLeft + 12, guiTop + 20, handler)));

        List<String> exc = TileEntityProgrammableController.BLACKLISTED_WIDGETS.stream()
                .map(s -> GuiConstants.BULLET + " " + I18n.format("programmingPuzzle." + s.getNamespace() + "." + s.getPath() + ".name"))
                .sorted()
                .collect(Collectors.toList());
        addAnimatedStat("gui.tab.info.programmable_controller.excluded",
                new ItemStack(ModItems.DRONE.get()), 0xFFFF5050, true).setText(exc);
    }

    @Override
    public IDroneBase getDrone() {
        return te;
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_PROGRAMMABLE_CONTROLLER;
    }

    @Override
    protected void addProblems(List<String> curInfo) {
        super.addProblems(curInfo);
        if (te.getPrimaryInventory().getStackInSlot(0).isEmpty()) curInfo.add("gui.tab.problems.programmableController.noProgram");
    }
}
