package pneumaticCraft.common.progwidgets;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetCoordinateOperator;
import pneumaticCraft.common.ai.DroneAIManager;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetCoordinateOperator extends ProgWidget implements IVariableWidget{
    private boolean multiplyDivide;
    private String variable = "";
    private DroneAIManager aiManager;

    @Override
    public boolean hasStepInput(){
        return true;
    }

    @Override
    public Class<? extends IProgWidget> returnType(){
        return null;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetCoordinate.class};
    }

    @Override
    public String getWidgetString(){
        return "coordinateOperator";
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.BURST_PLANT_DAMAGE;
    }

    @Override
    public WidgetDifficulty getDifficulty(){
        return WidgetDifficulty.ADVANCED;
    }

    @Override
    public void addErrors(List<String> curInfo){
        super.addErrors(curInfo);
        if(variable.equals("")) {
            curInfo.add("gui.progWidget.general.error.emptyVariable");
        }
    }

    @Override
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets){
        if(!variable.equals("")) {
            ChunkPosition curPos = calculateCoordinate(this, 0, multiplyDivide);
            aiManager.setCoordinate(variable, curPos);
        }
        return super.getOutputWidget(drone, allWidgets);
    }

    public static ChunkPosition calculateCoordinate(IProgWidget widget, int argIndex, boolean multiplyDivide){
        ChunkPosition curPos;
        if(multiplyDivide) {
            curPos = new ChunkPosition(1, 1, 1);
            ProgWidgetCoordinate coordinateWidget = (ProgWidgetCoordinate)widget.getConnectedParameters()[argIndex];
            while(coordinateWidget != null) {
                ChunkPosition pos = coordinateWidget.getCoordinate();
                curPos = new ChunkPosition(curPos.chunkPosX * pos.chunkPosX, curPos.chunkPosY * pos.chunkPosY, curPos.chunkPosZ * pos.chunkPosZ);
                coordinateWidget = (ProgWidgetCoordinate)coordinateWidget.getConnectedParameters()[0];
            }
            coordinateWidget = (ProgWidgetCoordinate)widget.getConnectedParameters()[widget.getParameters().length + argIndex];
            while(coordinateWidget != null) {
                ChunkPosition pos = coordinateWidget.getCoordinate();
                if(pos.chunkPosX != 0 && pos.chunkPosY != 0 && pos.chunkPosZ != 0) curPos = new ChunkPosition(curPos.chunkPosX / pos.chunkPosX, curPos.chunkPosY / pos.chunkPosY, curPos.chunkPosZ / pos.chunkPosZ);
                coordinateWidget = (ProgWidgetCoordinate)coordinateWidget.getConnectedParameters()[0];
            }
        } else {
            curPos = new ChunkPosition(0, 0, 0);
            ProgWidgetCoordinate coordinateWidget = (ProgWidgetCoordinate)widget.getConnectedParameters()[argIndex];
            while(coordinateWidget != null) {
                ChunkPosition pos = coordinateWidget.getCoordinate();
                curPos = new ChunkPosition(curPos.chunkPosX + pos.chunkPosX, curPos.chunkPosY + pos.chunkPosY, curPos.chunkPosZ + pos.chunkPosZ);
                coordinateWidget = (ProgWidgetCoordinate)coordinateWidget.getConnectedParameters()[0];
            }
            coordinateWidget = (ProgWidgetCoordinate)widget.getConnectedParameters()[widget.getParameters().length + argIndex];
            while(coordinateWidget != null) {
                ChunkPosition pos = coordinateWidget.getCoordinate();
                curPos = new ChunkPosition(curPos.chunkPosX - pos.chunkPosX, curPos.chunkPosY - pos.chunkPosY, curPos.chunkPosZ - pos.chunkPosZ);
                coordinateWidget = (ProgWidgetCoordinate)coordinateWidget.getConnectedParameters()[0];
            }
        }
        return curPos;
    }

    @Override
    protected ResourceLocation getTexture(){
        return multiplyDivide ? Textures.PROG_WIDGET_COORDINATE_OPERATION_MULTIPLY_DIVIDE : Textures.PROG_WIDGET_COORDINATE_OPERATION_PLUS_MINUS;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setString("variable", variable);
        tag.setBoolean("multiplyDivide", multiplyDivide);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        variable = tag.getString("variable");
        multiplyDivide = tag.getBoolean("multiplyDivide");
    }

    public boolean isMultiplyAndDividing(){
        return multiplyDivide;
    }

    public void setMultiplyDividing(boolean multiplyDivide){
        this.multiplyDivide = multiplyDivide;
    }

    public String getVariable(){
        return variable;
    }

    public void setVariable(String variable){
        this.variable = variable;
    }

    @Override
    public void setAIManager(DroneAIManager aiManager){
        this.aiManager = aiManager;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetCoordinateOperator(this, guiProgrammer);
    }

    @Override
    public void getTooltip(List<String> curTooltip){
        super.getTooltip(curTooltip);
        curTooltip.add("Setting variable: \"" + variable + "\"");
    }

    @Override
    public String getExtraStringInfo(){
        return "\"" + variable + "\"";
    }
}
