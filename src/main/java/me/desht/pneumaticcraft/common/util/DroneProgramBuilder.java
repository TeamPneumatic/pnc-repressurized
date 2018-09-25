package me.desht.pneumaticcraft.common.util;

import me.desht.pneumaticcraft.common.progwidgets.IProgWidget;
import me.desht.pneumaticcraft.common.tileentity.TileEntityProgrammer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to build simple (no jumping) Drone programs, without needing to worry about the X/Y locations of widgets
 * @author MineMaarten
 *
 */
public class DroneProgramBuilder{

    private final List<DroneInstruction> instructions = new ArrayList<>();
    
    public void add(IProgWidget mainInstruction, IProgWidget... whitelist){
        instructions.add(new DroneInstruction(mainInstruction, Arrays.asList(whitelist)));
    }
    
    public List<IProgWidget> build(){
        List<IProgWidget> allWidgets = new ArrayList<>();
        int curY = 0;
        for(DroneInstruction instruction : instructions){
            instruction.mainInstruction.setX(0);
            instruction.mainInstruction.setY(curY);
            
            //Add whitelist
            if(!instruction.whitelist.isEmpty()){
                for(int parameterIndex = 0; parameterIndex < instruction.mainInstruction.getParameters().length; parameterIndex++){
                    Class<? extends IProgWidget> parameterClass = instruction.mainInstruction.getParameters()[parameterIndex];
                    List<IProgWidget> whitelist = instruction.whitelist.stream()
                                                             .filter(x -> parameterClass.isAssignableFrom(x.getClass()))
                                                             .collect(Collectors.toList());
                    int curX = instruction.mainInstruction.getWidth() / 2;
                    for(IProgWidget whitelistItem : whitelist){
                        whitelistItem.setX(curX);
                        whitelistItem.setY(curY + parameterIndex * 11);
                        curX += whitelistItem.getWidth() / 2;
                    }
                }
            }
            
            
            curY += instruction.mainInstruction.getHeight() / 2;
            instruction.addToWidgets(allWidgets);
        }
        TileEntityProgrammer.updatePuzzleConnections(allWidgets);
        return allWidgets;
    }
    
    private class DroneInstruction{
        final IProgWidget mainInstruction;
        final List<IProgWidget> whitelist;
        
        DroneInstruction(IProgWidget mainInstruction, List<IProgWidget> whitelist){
            this.mainInstruction = mainInstruction;
            this.whitelist = whitelist;
        }
        
        void addToWidgets(List<IProgWidget> widgets){
            widgets.add(mainInstruction);
            widgets.addAll(whitelist);
        }
    }
}
