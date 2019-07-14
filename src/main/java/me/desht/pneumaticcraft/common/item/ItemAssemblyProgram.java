package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.recipes.programs.AssemblyProgram;
import me.desht.pneumaticcraft.common.recipes.programs.ProgramDrill;
import me.desht.pneumaticcraft.common.recipes.programs.ProgramDrillLaser;
import me.desht.pneumaticcraft.common.recipes.programs.ProgramLaser;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.BULLET;
import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class ItemAssemblyProgram extends ItemPneumatic {
    private final AssemblyProgramType programType;

    public enum AssemblyProgramType {
        DRILL("drill", new ProgramDrill()),
        LASER("laser", new ProgramLaser()),
        DRILL_LASER("drill_laser", new ProgramDrillLaser());

        private final String name;
        private AssemblyProgram program;

        AssemblyProgramType(String name, AssemblyProgram program) {
            this.name = name;
            this.program = program;
        }

        public String getRegistryName() {
            return name;
        }

    }

    public ItemAssemblyProgram(AssemblyProgramType programType) {
        super("assembly_program_" + programType.getRegistryName());
        this.programType = programType;
    }

    public AssemblyProgram getProgram() {
        return programType.program;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> infoList, ITooltipFlag par4) {
        infoList.add(new StringTextComponent("Required Machines:"));
        infoList.add(BULLET.appendSibling(xlate(ModBlocks.ASSEMBLY_CONTROLLER.getTranslationKey())));

        AssemblyProgram program = getProgram();
        AssemblyProgram.EnumMachine[] requiredMachines = program.getRequiredMachines();
        for (AssemblyProgram.EnumMachine machine : requiredMachines) {
            switch (machine) {
                case PLATFORM:
                    infoList.add(BULLET.appendSibling(xlate(ModBlocks.ASSEMBLY_PLATFORM.getTranslationKey())));
                    break;
                case DRILL:
                    infoList.add(BULLET.appendSibling(xlate(ModBlocks.ASSEMBLY_DRILL.getTranslationKey())));
                    break;
                case LASER:
                    infoList.add(BULLET.appendSibling(xlate(ModBlocks.ASSEMBLY_LASER.getTranslationKey())));
                    break;
                case IO_UNIT_EXPORT:
                    infoList.add(BULLET.appendSibling(xlate(ModBlocks.ASSEMBLY_IO_UNIT.getTranslationKey()).appendText(" (export)")));//TODO localize
                    break;
                case IO_UNIT_IMPORT:
                    infoList.add(BULLET.appendSibling(xlate(ModBlocks.ASSEMBLY_IO_UNIT.getTranslationKey()).appendText(" (import)")));
                    break;
            }
        }
    }

    public static AssemblyProgram getProgramForType(AssemblyProgramType t) {
        switch (t) {
            case DRILL:
                return new ProgramDrill();
            case LASER:
                return new ProgramLaser();
            case DRILL_LASER:
                return new ProgramDrillLaser();
        }
        return null;
    }

    public static AssemblyProgram getProgram(ItemStack stack) {
        return stack.getItem() instanceof ItemAssemblyProgram ? ((ItemAssemblyProgram) stack.getItem()).getProgram() : null;
    }
}
