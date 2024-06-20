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

package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe.AssemblyProgramType;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram;
import me.desht.pneumaticcraft.common.recipes.assembly.ProgramDrill;
import me.desht.pneumaticcraft.common.recipes.assembly.ProgramDrillLaser;
import me.desht.pneumaticcraft.common.recipes.assembly.ProgramLaser;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Arrays;
import java.util.List;

import static me.desht.pneumaticcraft.api.misc.Symbols.bullet;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class AssemblyProgramItem extends Item {
    private static final AssemblyProgram[] PROGRAMS = new AssemblyProgram[AssemblyProgramType.values().length];
    static {
        PROGRAMS[AssemblyProgramType.DRILL.ordinal()] = new ProgramDrill();
        PROGRAMS[AssemblyProgramType.LASER.ordinal()] = new ProgramLaser();
        PROGRAMS[AssemblyProgramType.DRILL_LASER.ordinal()] = new ProgramDrillLaser();
    }

    private final AssemblyProgramType programType;

    public AssemblyProgramItem(AssemblyProgramType programType) {
        super(ModItems.defaultProps());
        this.programType = programType;
    }

    public AssemblyProgramType getProgramType() {
        return programType;
    }

    public AssemblyProgram getProgram() {
        return PROGRAMS[programType.ordinal()];
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> infoList, TooltipFlag par4) {
        infoList.add(Component.literal("Required Machines:"));
        infoList.add(bullet().append(xlate(ModBlocks.ASSEMBLY_CONTROLLER.get().getDescriptionId())));
        Arrays.stream(getProgram().getRequiredMachines())
                .map(machine -> bullet().append(xlate(machine.getMachineBlock().getDescriptionId())))
                .forEach(infoList::add);
    }

    public static AssemblyProgramItem fromProgramType(AssemblyProgramType program) {
        return PROGRAMS[program.ordinal()].getItem();
    }

    public static AssemblyProgram getProgram(ItemStack stack) {
        return stack.getItem() instanceof AssemblyProgramItem ? ((AssemblyProgramItem) stack.getItem()).getProgram() : null;
    }
}
