package pneumaticCraft.common.progwidgets;

import java.util.List;
import java.util.Set;

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

public class ProgWidgetCoordinateOperator extends ProgWidget implements IVariableSetWidget{
    public enum EnumOperator{
        PLUS_MINUS("plusMinus"), MULIPLY_DIVIDE("multiplyDivide"), MAX_MIN("maxMin");

        public ResourceLocation texture;
        private final String name;

        private EnumOperator(String name){
            this.name = name;
            texture = new ResourceLocation(Textures.PROG_WIDGET_LOCATION + "coordinateOperation" + name.substring(0, 1).toUpperCase() + name.substring(1) + ".png");
        }

        public String getUnlocalizedName(){
            return "gui.progWidget.coordinateOperator." + name;
        }
    }

    private EnumOperator operator = EnumOperator.PLUS_MINUS;
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
    public void addErrors(List<String> curInfo, List<IProgWidget> widgets){
        super.addErrors(curInfo, widgets);
        if(variable.equals("")) {
            curInfo.add("gui.progWidget.general.error.emptyVariable");
        }
        if(operator == EnumOperator.MAX_MIN) {
            if(getConnectedParameters()[0] == null && getConnectedParameters()[getParameters().length] == null) {
                curInfo.add("gui.progWidget.coordinateOperator.noParameter");
            }
        }
    }

    @Override
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets){
        if(!variable.equals("")) {
            ChunkPosition curPos = calculateCoordinate(this, 0, operator);
            aiManager.setCoordinate(variable, curPos);
        }
        return super.getOutputWidget(drone, allWidgets);
    }

    public static ChunkPosition calculateCoordinate(IProgWidget widget, int argIndex, EnumOperator operator){
        ChunkPosition curPos = null;
        switch(operator){
            case MULIPLY_DIVIDE:
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
                break;
            case PLUS_MINUS:
                curPos = new ChunkPosition(0, 0, 0);
                coordinateWidget = (ProgWidgetCoordinate)widget.getConnectedParameters()[argIndex];
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
                break;
            case MAX_MIN:
                curPos = new ChunkPosition(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
                coordinateWidget = (ProgWidgetCoordinate)widget.getConnectedParameters()[argIndex];
                while(coordinateWidget != null) {
                    ChunkPosition pos = coordinateWidget.getCoordinate();
                    curPos = new ChunkPosition(Math.max(curPos.chunkPosX, pos.chunkPosX), Math.max(curPos.chunkPosY, pos.chunkPosY), Math.max(curPos.chunkPosZ, pos.chunkPosZ));
                    coordinateWidget = (ProgWidgetCoordinate)coordinateWidget.getConnectedParameters()[0];
                }
                if(curPos.equals(new ChunkPosition(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE))) curPos = new ChunkPosition(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
                coordinateWidget = (ProgWidgetCoordinate)widget.getConnectedParameters()[widget.getParameters().length + argIndex];
                while(coordinateWidget != null) {
                    ChunkPosition pos = coordinateWidget.getCoordinate();
                    curPos = new ChunkPosition(Math.min(curPos.chunkPosX, pos.chunkPosX), Math.min(curPos.chunkPosY, pos.chunkPosY), Math.min(curPos.chunkPosZ, pos.chunkPosZ));
                    coordinateWidget = (ProgWidgetCoordinate)coordinateWidget.getConnectedParameters()[0];
                }
                break;
        }
        return curPos;
    }

    @Override
    protected ResourceLocation getTexture(){
        return operator.texture;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setString("variable", variable);
        tag.setByte("operator", (byte)operator.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        variable = tag.getString("variable");
        byte operatorValue = tag.hasKey("multiplyDivide") ? tag.getByte("multiplyDivide") : tag.getByte("operator");
        operator = EnumOperator.values()[operatorValue];
    }

    public EnumOperator getOperator(){
        return operator;
    }

    public void setOperator(EnumOperator operator){
        this.operator = operator;
    }

    @Override
    public String getVariable(){
        return variable;
    }

    @Override
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

    @Override
    public void addVariables(Set<String> variables){
        variables.add(variable);
    }
}
