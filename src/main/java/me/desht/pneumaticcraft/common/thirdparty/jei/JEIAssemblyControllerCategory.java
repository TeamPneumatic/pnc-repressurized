package me.desht.pneumaticcraft.common.thirdparty.jei;

import me.desht.pneumaticcraft.common.block.Blockss;
import me.desht.pneumaticcraft.common.recipes.AssemblyRecipe;
import me.desht.pneumaticcraft.common.recipes.programs.AssemblyProgram;
import me.desht.pneumaticcraft.lib.Textures;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.IDrawableAnimated.StartDirection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;

import javax.annotation.Nonnull;

import static me.desht.pneumaticcraft.common.recipes.programs.AssemblyProgram.EnumMachine.IO_UNIT_EXPORT;
import static me.desht.pneumaticcraft.common.recipes.programs.AssemblyProgram.EnumMachine.IO_UNIT_IMPORT;

public class JEIAssemblyControllerCategory extends PneumaticCraftCategory<JEIAssemblyControllerCategory.AssemblyRecipeWrapper> {
    JEIAssemblyControllerCategory(IJeiHelpers jeiHelpers) {
        super(jeiHelpers);
    }

    @Override
    public String getUid() {
        return ModCategoryUid.ASSEMBLY_CONTROLLER;
    }

    @Override
    public String getTitle() {
        return I18n.format(Blockss.ASSEMBLY_CONTROLLER.getUnlocalizedName() + ".name");
    }

    @Override
    public ResourceDrawable getGuiTexture() {
        return new ResourceDrawable(Textures.GUI_NEI_ASSEMBLY_CONTROLLER, 0, 0, 5, 11, 166, 130);
    }

    static class AssemblyRecipeWrapper extends PneumaticCraftCategory.MultipleInputOutputRecipeWrapper {
        AssemblyRecipeWrapper(AssemblyRecipe recipe) {
            AssemblyProgram program = AssemblyProgram.fromRecipe(recipe);

            ItemStack[] inputStacks = new ItemStack[]{recipe.getInput()};//for now not useful to put it in an array, but supports when adding multiple input/output.
            for (int i = 0; i < inputStacks.length; i++) {
                PositionedStack stack = new PositionedStack(inputStacks[i], 29 + i % 2 * 18, 66 + i / 2 * 18);
                this.addIngredient(stack);
            }

            ItemStack[] outputStacks = new ItemStack[]{recipe.getOutput()};
            for (int i = 0; i < outputStacks.length; i++) {
                PositionedStack stack = new PositionedStack(outputStacks[i], 96 + i % 2 * 18, 66 + i / 2 * 18);
                this.addOutput(stack);
            }
            this.addIngredient(new PositionedStack(program.getItemStack(1), 133, 22));
            ItemStack[] requiredMachines = getMachinesFromEnum(program.getRequiredMachines());
            for (int i = 0; i < requiredMachines.length; i++) {
                this.addIngredient(new PositionedStack(requiredMachines[i], 5 + i * 18, 25));
            }
        }

        private ItemStack[] getMachinesFromEnum(AssemblyProgram.EnumMachine[] requiredMachines) {
            ItemStack[] machineStacks = new ItemStack[requiredMachines.length];
            for (int i = 0; i < requiredMachines.length; i++) {
                switch (requiredMachines[i]) {
                    case PLATFORM:
                        machineStacks[i] = new ItemStack(Blockss.ASSEMBLY_PLATFORM);
                        break;
                    case DRILL:
                        machineStacks[i] = new ItemStack(Blockss.ASSEMBLY_DRILL);
                        break;
                    case LASER:
                        machineStacks[i] = new ItemStack(Blockss.ASSEMBLY_LASER);
                        break;
                    case IO_UNIT_IMPORT:
                        machineStacks[i] = makeIOUnitStack(IO_UNIT_IMPORT);
                        break;
                    case IO_UNIT_EXPORT:
                        machineStacks[i] = makeIOUnitStack(IO_UNIT_EXPORT);
                        break;
                }
            }
            return machineStacks;
        }
    }

    @Nonnull
    private static ItemStack makeIOUnitStack(AssemblyProgram.EnumMachine what) {
        ItemStack stack = new ItemStack(Blockss.ASSEMBLY_IO_UNIT, 1, what == IO_UNIT_IMPORT ? 0 : 1);
        return stack.setStackDisplayName(TextFormatting.RESET.toString() + TextFormatting.WHITE +
                stack.getDisplayName() + (what == IO_UNIT_IMPORT ? " (import)" : " (export)"));
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        drawProgressBar(68, 75, 173, 0, 24, 17, StartDirection.LEFT);
        FontRenderer fontRenderer = FMLClientHandler.instance().getClient().fontRenderer;
        fontRenderer.drawString("Required Machines", 5, 15, 4210752);
        fontRenderer.drawString("Prog.", 129, 9, 4210752);
    }
}
