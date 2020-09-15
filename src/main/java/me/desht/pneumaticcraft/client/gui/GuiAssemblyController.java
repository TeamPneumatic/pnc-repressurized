package me.desht.pneumaticcraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerAssemblyController;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram.EnumMachine;
import me.desht.pneumaticcraft.common.tileentity.IAssemblyMachine;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyController;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class GuiAssemblyController extends GuiPneumaticContainerBase<ContainerAssemblyController,TileEntityAssemblyController> {

    private WidgetAnimatedStat statusStat;

    public GuiAssemblyController(ContainerAssemblyController container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        statusStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.status"), new ItemStack(ModBlocks.ASSEMBLY_CONTROLLER.get()), 0xFFFFAA00, false);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        super.drawGuiContainerForegroundLayer(matrixStack, x, y);
        font.drawString(matrixStack, "Prog.", 70, 24, 0x404040);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_ASSEMBLY_CONTROLLER;
    }

    @Override
    public void tick() {
        super.tick();
        statusStat.setText(getStatusText());
    }

    private List<String> getStatusText() {
        List<String> text = new ArrayList<>();

        EnumSet<EnumMachine> foundMachines = EnumSet.of(EnumMachine.CONTROLLER);
        for (IAssemblyMachine machine : te.findMachines(EnumMachine.values().length)) {
            foundMachines.add(machine.getAssemblyType());
        }
        for (EnumMachine m : EnumMachine.values()) {
            if (m == EnumMachine.CONTROLLER) continue; // we *are* the controller!
            String s = foundMachines.contains(m) ? TextFormatting.DARK_GREEN + "\u2714 " : TextFormatting.RED + "\u2717 ";
            text.add(s + TextFormatting.BLACK + " " + I18n.format(m.getTranslationKey()));
        }
        return text;
    }

    @Override
    protected void addProblems(List<String> textList) {
        super.addProblems(textList);

        if (te.curProgram == null) {
            textList.addAll(PneumaticCraftUtils.splitString(I18n.format("pneumaticcraft.gui.tab.problems.assembly_controller.no_program")));
        } else {
            if (te.isMachineDuplicate) {
                String s = te.duplicateMachine == null ? "<???>" : I18n.format(te.duplicateMachine.getTranslationKey());
                textList.addAll(PneumaticCraftUtils.splitString(I18n.format("pneumaticcraft.gui.tab.problems.assembly_controller.duplicateMachine", s)));
            } else if (te.isMachineMissing) {
                String s = te.missingMachine == null ? "<???>" : I18n.format(te.missingMachine.getTranslationKey());
                textList.addAll(PneumaticCraftUtils.splitString(I18n.format("pneumaticcraft.gui.tab.problems.assembly_controller.missingMachine", s)));
            } else {
                te.curProgram.addProgramProblem(textList);
            }
        }
    }
}
