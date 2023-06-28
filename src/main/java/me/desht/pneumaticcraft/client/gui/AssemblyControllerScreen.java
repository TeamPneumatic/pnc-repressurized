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

package me.desht.pneumaticcraft.client.gui;

import me.desht.pneumaticcraft.api.misc.Symbols;
import me.desht.pneumaticcraft.client.gui.widget.WidgetAnimatedStat;
import me.desht.pneumaticcraft.client.util.GuiUtils;
import me.desht.pneumaticcraft.common.block.entity.AssemblyControllerBlockEntity;
import me.desht.pneumaticcraft.common.block.entity.IAssemblyMachine;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.inventory.AssemblyControllerMenu;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram.EnumMachine;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class AssemblyControllerScreen extends AbstractPneumaticCraftContainerScreen<AssemblyControllerMenu,AssemblyControllerBlockEntity> {

    private WidgetAnimatedStat statusStat;

    public AssemblyControllerScreen(AssemblyControllerMenu container, Inventory inv, Component displayString) {
        super(container, inv, displayString);
    }

    @Override
    public void init() {
        super.init();
        statusStat = addAnimatedStat(xlate("pneumaticcraft.gui.tab.status"), new ItemStack(ModBlocks.ASSEMBLY_CONTROLLER.get()), 0xFFFFAA00, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
        super.renderLabels(graphics, x, y);
        graphics.drawString(font, "Prog.", 70, 24, 0x404040, false);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return Textures.GUI_ASSEMBLY_CONTROLLER;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        statusStat.setText(getStatusText());
    }

    private List<Component> getStatusText() {
        List<Component> text = new ArrayList<>();

        EnumSet<EnumMachine> foundMachines = EnumSet.of(EnumMachine.CONTROLLER);
        for (IAssemblyMachine machine : te.findMachines(EnumMachine.values().length)) {
            foundMachines.add(machine.getAssemblyType());
        }
        for (EnumMachine m : EnumMachine.values()) {
            if (m == EnumMachine.CONTROLLER) continue; // we *are* the controller!
            MutableComponent s = foundMachines.contains(m) ?
                    Component.literal(Symbols.TICK_MARK).withStyle(ChatFormatting.DARK_GREEN) :
                    Component.literal(Symbols.X_MARK).withStyle(ChatFormatting.RED);
            text.add(s.append(" ").append(xlate(m.getTranslationKey()).withStyle(ChatFormatting.WHITE)));
        }
        return text;
    }

    @Override
    protected void addProblems(List<Component> textList) {
        super.addProblems(textList);

        if (te.curProgram == null) {
            textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.assembly_controller.no_program"));
        } else {
            if (te.isMachineDuplicate) {
                String s = te.duplicateMachine == null ? "<???>" : I18n.get(te.duplicateMachine.getTranslationKey());
                textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.assembly_controller.duplicateMachine", s));
            } else if (te.isMachineMissing) {
                String s = te.missingMachine == null ? "<???>" : I18n.get(te.missingMachine.getTranslationKey());
                textList.addAll(GuiUtils.xlateAndSplit("pneumaticcraft.gui.tab.problems.assembly_controller.missingMachine", s));
            } else {
                te.curProgram.addProgramProblem(textList);
            }
        }
    }
}
