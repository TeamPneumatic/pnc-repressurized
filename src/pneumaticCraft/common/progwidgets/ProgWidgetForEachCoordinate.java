package pneumaticCraft.common.progwidgets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetForEach;
import pneumaticCraft.common.ai.DroneAIForEachCoordinate;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetForEachCoordinate extends ProgWidgetAreaItemBase implements IJumpBackWidget, IJump,
        IVariableSetWidget{
    private String elementVariable = "";
    private final Set<ChunkPosition> traversedPositions = new HashSet<ChunkPosition>();
    private DroneAIForEachCoordinate ai;

    @Override
    public String getWidgetString(){
        return "forEachCoordinate";
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.HELIUM_PLANT_DAMAGE;
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_FOR_EACH_COORDINATE;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetArea.class, ProgWidgetString.class};
    }

    @Override
    public void addVariables(Set<String> variables){
        super.addVariables(variables);
        variables.add(elementVariable);
    }

    @Override
    public String getVariable(){
        return elementVariable;
    }

    @Override
    public void setVariable(String variable){
        elementVariable = variable;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        tag.setString("variable", elementVariable);
        super.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        elementVariable = tag.getString("variable");
        super.readFromNBT(tag);
    }

    @Override
    public WidgetDifficulty getDifficulty(){
        return WidgetDifficulty.ADVANCED;
    }

    @Override
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets){
        List<String> locations = getPossibleJumpLocations();
        if(locations.size() > 0 && ai != null && (traversedPositions.size() == 1 || !aiManager.getCoordinate(elementVariable).equals(new ChunkPosition(0, 0, 0)))) {
            ChunkPosition pos = ai.getCurCoord();
            if(pos != null) {
                aiManager.setCoordinate(elementVariable, pos);
                return ProgWidgetJump.jumpToLabel(drone, allWidgets, locations.get(0));
            }
        }
        traversedPositions.clear();
        return super.getOutputWidget(drone, allWidgets);
    }

    @Override
    public List<String> getPossibleJumpLocations(){
        IProgWidget widget = getConnectedParameters()[getParameters().length - 1];
        ProgWidgetString textWidget = widget != null ? (ProgWidgetString)widget : null;
        List<String> locations = new ArrayList<String>();
        if(textWidget != null) locations.add(textWidget.string);
        return locations;
    }

    @Override
    public EntityAIBase getWidgetAI(IDroneBase drone, IProgWidget widget){
        return ai = new DroneAIForEachCoordinate(drone, (ProgWidgetForEachCoordinate)widget);
    }

    public boolean isValidPosition(ChunkPosition pos){
        return traversedPositions.add(pos);
    }

    @Override
    public boolean canBeRunByComputers(IDroneBase drone, IProgWidget widget){
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetForEach(this, guiProgrammer);
    }

    @Override
    public String getExtraStringInfo(){
        return "\"" + elementVariable + "\"";
    }

    @Override
    public boolean canSetParameter(int index){
        return index != 2;//Don't use the blacklist side of the jump parameter.
    }
}
