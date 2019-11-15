package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.client.gui.widget.GuiAnimatedStat;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.ContainerAssemblyController;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram.EnumMachine;
import me.desht.pneumaticcraft.common.tileentity.IAssemblyMachine;
import me.desht.pneumaticcraft.common.tileentity.TileEntityAssemblyController;
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

public class GuiAssemblyController extends GuiPneumaticContainerBase<ContainerAssemblyController,TileEntityAssemblyController> {

    private GuiAnimatedStat statusStat;

    public GuiAssemblyController(ContainerAssemblyController container, PlayerInventory inv, ITextComponent displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        statusStat = addAnimatedStat("gui.tab.status", new ItemStack(ModBlocks.ASSEMBLY_CONTROLLER), 0xFFFFAA00, false);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        font.drawString("Upgr.", 18, 21, 4210752);
        font.drawString("Prog.", 70, 24, 4210752);
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

        te.addProblems(textList);
    }
}
