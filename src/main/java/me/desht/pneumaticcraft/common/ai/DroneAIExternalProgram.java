package me.desht.pneumaticcraft.common.ai;

import me.desht.pneumaticcraft.api.item.IProgrammable;
import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetExternalProgram;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DroneAIExternalProgram extends DroneAIBlockInteraction<ProgWidgetExternalProgram> {

    private final DroneAIManager subAI, mainAI;
    private final Set<BlockPos> traversedPositions = new HashSet<>();
    private int curSlot;
    private NBTTagCompound curProgramTag; //Used to see if changes have been made to the program while running it.

    public DroneAIExternalProgram(IDroneBase drone, DroneAIManager mainAI, ProgWidgetExternalProgram widget) {
        super(drone, widget);
        this.mainAI = mainAI;
        subAI = new DroneAIManager(drone, new ArrayList<>());
    }

    @Override
    public boolean shouldExecute() {
        if (super.shouldExecute()) {
            traversedPositions.clear();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean moveToPositions() {
        return false;
    }

    @Override
    protected boolean isValidPosition(BlockPos pos) {
        if (traversedPositions.add(pos)) {
            curSlot = 0;
            TileEntity te = drone.world().getTileEntity(pos);
            return te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        }
        return false;
    }

    @Override
    protected boolean doBlockInteraction(BlockPos pos, double distToBlock) {
        IItemHandler inv = IOHelper.getInventoryForTE(drone.world().getTileEntity(pos));
        if (inv == null) return false;
        if (curProgramTag != null) {
            if (curSlot < inv.getSlots()) {
                ItemStack stack = inv.getStackInSlot(curSlot);
                if (curProgramTag.equals(stack.getTagCompound())) {
                    subAI.onUpdateTasks();
                    if (subAI.isIdling() || isRunningSameProgram(subAI.getCurrentAI())) {
                        curProgramTag = null;
                        curSlot++;
                    }
                } else {
                    curProgramTag = null;
                    subAI.setWidgets(new ArrayList<>());
                }
            }
            return true;
        } else {
            while (curSlot < inv.getSlots()) {
                ItemStack stack = inv.getStackInSlot(curSlot);
                if (stack.getItem() instanceof IProgrammable) {
                    IProgrammable programmable = (IProgrammable) stack.getItem();
                    if (programmable.canProgram(stack) && programmable.usesPieces(stack)) {
                        List<IProgWidget> widgets = TileEntityProgrammer.getProgWidgets(stack);

                        boolean areWidgetsValid = true;
                        for (IProgWidget widget : widgets) {
                            if (!drone.isProgramApplicable(widget)) {
                                areWidgetsValid = false;
                                break;
                            }
                        }

                        if (areWidgetsValid) {
                            if (widget.shareVariables) mainAI.connectVariables(subAI);
                            subAI.getDrone().getAIManager().setLabel("Main");
                            subAI.setWidgets(widgets);
                            curProgramTag = stack.getTagCompound();
                            if (!subAI.isIdling()) {
                                return true;
                            }
                        }
                    }
                }
                curSlot++;
            }
            return false;
        }
    }

    //Prevent a memory leak, as a result of the same External program recursively calling itself.
    private boolean isRunningSameProgram(EntityAIBase ai) {
        return ai instanceof DroneAIExternalProgram && curProgramTag.equals(((DroneAIExternalProgram) ai).curProgramTag);
    }

    DroneAIManager getRunningAI() {
        return subAI;
    }

}
