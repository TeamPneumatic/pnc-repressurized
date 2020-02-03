package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.api.crafting.recipe.IAssemblyRecipe.AssemblyProgramType;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram;
import me.desht.pneumaticcraft.common.recipes.assembly.ProgramDrill;
import me.desht.pneumaticcraft.common.recipes.assembly.ProgramDrillLaser;
import me.desht.pneumaticcraft.common.recipes.assembly.ProgramLaser;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.bullet;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemAssemblyProgram extends Item {
    private static final AssemblyProgram[] PROGRAMS = new AssemblyProgram[AssemblyProgramType.values().length];
    static {
        PROGRAMS[AssemblyProgramType.DRILL.ordinal()] = new ProgramDrill();
        PROGRAMS[AssemblyProgramType.LASER.ordinal()] = new ProgramLaser();
        PROGRAMS[AssemblyProgramType.DRILL_LASER.ordinal()] = new ProgramDrillLaser();
    }

    private final AssemblyProgramType programType;

    public ItemAssemblyProgram(AssemblyProgramType programType) {
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
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> infoList, ITooltipFlag par4) {
        infoList.add(new StringTextComponent("Required Machines:"));
        infoList.add(bullet().appendSibling(xlate(ModBlocks.ASSEMBLY_CONTROLLER.get().getTranslationKey())));
        Arrays.stream(getProgram().getRequiredMachines())
                .map(machine -> bullet().appendSibling(xlate(machine.getMachineBlock().getTranslationKey())))
                .forEach(infoList::add);
    }

    public static ItemAssemblyProgram fromProgramType(AssemblyProgramType program) {
        return PROGRAMS[program.ordinal()].getItem();
    }

    public static AssemblyProgram getProgram(ItemStack stack) {
        return stack.getItem() instanceof ItemAssemblyProgram ? ((ItemAssemblyProgram) stack.getItem()).getProgram() : null;
    }
}
