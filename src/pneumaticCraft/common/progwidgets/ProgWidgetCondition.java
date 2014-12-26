package pneumaticCraft.common.progwidgets;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetCondition;
import pneumaticCraft.common.ai.DroneAIBlockCondition;
import pneumaticCraft.common.entity.living.EntityDrone;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Log;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class ProgWidgetCondition extends ProgWidgetInventoryBase implements ICondition, IJump{

    private DroneAIBlockCondition evaluator;
    private boolean isAndFunction;
    private ICondition.Operator operator = ICondition.Operator.HIGHER_THAN_EQUALS;

    @Override
    public EntityAIBase getWidgetAI(EntityDrone drone, IProgWidget widget){
        evaluator = getEvaluator(drone, widget);
        return evaluator;
    }

    @Override
    public abstract Class<? extends IProgWidget>[] getParameters();

    protected abstract DroneAIBlockCondition getEvaluator(EntityDrone drone, IProgWidget widget);

    @Override
    public IProgWidget getOutputWidget(EntityDrone drone, List<IProgWidget> allWidgets){
        if(evaluator != null) {
            return ProgWidgetJump.jumpToLabel(allWidgets, this, evaluate(drone));
        } else {
            Log.error("Shouldn't be happening!");
            return super.getOutputWidget(drone, allWidgets);
        }
    }

    @Override
    public boolean evaluate(EntityDrone drone){
        return evaluator.getResult();
    }

    @Override
    public boolean isAndFunction(){
        return isAndFunction;
    }

    @Override
    public void setAndFunction(boolean isAndFunction){
        this.isAndFunction = isAndFunction;
    }

    @Override
    public WidgetCategory getCategory(){
        return WidgetCategory.CONDITION;
    }

    @Override
    public List<String> getPossibleJumpLocations(){
        ProgWidgetString textWidget = (ProgWidgetString)getConnectedParameters()[getParameters().length - 1];
        ProgWidgetString textWidget2 = (ProgWidgetString)getConnectedParameters()[getParameters().length * 2 - 1];
        List<String> locations = new ArrayList<String>();
        if(textWidget != null) locations.add(textWidget.string);
        if(textWidget2 != null) locations.add(textWidget2.string);
        return locations;
    }

    @Override
    public int getRequiredCount(){
        return getCount();
    }

    @Override
    public void setRequiredCount(int count){
        setCount(count);
    }

    @Override
    public Operator getOperator(){
        return operator;
    }

    @Override
    public void setOperator(Operator operator){
        this.operator = operator;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setBoolean("isAndFunction", isAndFunction);
        tag.setByte("operator", (byte)operator.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        isAndFunction = tag.getBoolean("isAndFunction");
        operator = ICondition.Operator.values()[tag.getByte("operator")];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetCondition(this, guiProgrammer);
    }

    @Override
    public String getExtraStringInfo(){
        String anyAll = I18n.format(isAndFunction() ? "gui.progWidget.condition.all" : "gui.progWidget.condition.any");
        return anyAll + " " + getOperator().toString() + " " + getRequiredCount();
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.LIGHTNING_PLANT_DAMAGE;
    }
}
