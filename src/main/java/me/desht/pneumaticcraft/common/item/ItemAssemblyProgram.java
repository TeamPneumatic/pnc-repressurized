package me.desht.pneumaticcraft.common.item;

import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.recipes.programs.AssemblyProgram;
import me.desht.pneumaticcraft.common.recipes.programs.ProgramDrill;
import me.desht.pneumaticcraft.common.recipes.programs.ProgramDrillLaser;
import me.desht.pneumaticcraft.common.recipes.programs.ProgramLaser;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemAssemblyProgram extends ItemPneumaticSubtyped {
    public static final int PROGRAMS_AMOUNT = 3;

    public static final int DRILL_DAMAGE = 0;
    public static final int LASER_DAMAGE = 1;
    public static final int DRILL_LASER_DAMAGE = 2;

    private AssemblyProgram[] referencePrograms;

    public ItemAssemblyProgram() {
        super("assembly_program");
        setHasSubtypes(true);
    }

    @Override
    public String getUnlocalizedName(ItemStack is) {
        return super.getUnlocalizedName(is) + is.getItemDamage();
    }

    @Override
    public int getMetadata(int meta) {
        return meta;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            for (int i = 0; i < PROGRAMS_AMOUNT; i++) {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> infoList, ITooltipFlag par4) {
        infoList.add("Required Machines:");
        infoList.add("-" + Blockss.ASSEMBLY_CONTROLLER.getLocalizedName());

        if (referencePrograms == null) {
            referencePrograms = new AssemblyProgram[PROGRAMS_AMOUNT];
            for (int i = 0; i < PROGRAMS_AMOUNT; i++) {
                referencePrograms[i] = getProgramFromItem(i);
            }
        }
        AssemblyProgram program = referencePrograms[Math.min(stack.getItemDamage(), PROGRAMS_AMOUNT - 1)];
        AssemblyProgram.EnumMachine[] requiredMachines = program.getRequiredMachines();
        for (AssemblyProgram.EnumMachine machine : requiredMachines) {
            switch (machine) {
                case PLATFORM:
                    infoList.add("-" + Blockss.ASSEMBLY_PLATFORM.getLocalizedName());
                    break;
                case DRILL:
                    infoList.add("-" + Blockss.ASSEMBLY_DRILL.getLocalizedName());
                    break;
                case LASER:
                    infoList.add("-" + Blockss.ASSEMBLY_LASER.getLocalizedName());
                    break;
                case IO_UNIT_EXPORT:
                    infoList.add("-" + Blockss.ASSEMBLY_IO_UNIT.getLocalizedName() + " (export)");//TODO localize
                    break;
                case IO_UNIT_IMPORT:
                    infoList.add("-" + Blockss.ASSEMBLY_IO_UNIT.getLocalizedName() + " (import)");
                    break;
            }
        }
    }

    public static AssemblyProgram getProgramFromItem(int meta) {
        switch (meta) {
            case DRILL_DAMAGE:
                return new ProgramDrill();
            case LASER_DAMAGE:
                return new ProgramLaser();
            case DRILL_LASER_DAMAGE:
                return new ProgramDrillLaser();
        }
        return null;
    }

    @Override
    public String getSubtypeModelName(int meta) {
        return "assembly_program" + meta;
    }
}
