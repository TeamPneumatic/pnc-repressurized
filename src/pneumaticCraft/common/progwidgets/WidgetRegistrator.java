package pneumaticCraft.common.progwidgets;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pneumaticCraft.common.config.ProgWidgetConfig;

public class WidgetRegistrator{

    public static List<IProgWidget> registeredWidgets = new ArrayList<IProgWidget>();
    private static Map<String, IProgWidget> allRegisteredWidgets = new LinkedHashMap<String, IProgWidget>();

    public static void init(){
        register(new ProgWidgetComment());
        register(new ProgWidgetStart());
        register(new ProgWidgetArea());
        register(new ProgWidgetString());
        register(new ProgWidgetItemFilter());
        register(new ProgWidgetItemAssign());
        register(new ProgWidgetLiquidFilter());
        register(new ProgWidgetCoordinate());
        register(new ProgWidgetCoordinateOperator());
        register(new ProgWidgetEntityAttack());
        register(new ProgWidgetDig());
        register(new ProgWidgetPlace());
        register(new ProgWidgetBlockRightClick());
        register(new ProgWidgetEntityRightClick());
        register(new ProgWidgetPickupItem());
        register(new ProgWidgetDropItem());
        register(new ProgWidgetInventoryExport());
        register(new ProgWidgetInventoryImport());
        register(new ProgWidgetLiquidExport());
        register(new ProgWidgetLiquidImport());
        register(new ProgWidgetEntityExport());
        register(new ProgWidgetEntityImport());
        register(new ProgWidgetGoToLocation());
        register(new ProgWidgetTeleport());
        register(new ProgWidgetEmitRedstone());
        register(new ProgWidgetLabel());
        register(new ProgWidgetJump());
        register(new ProgWidgetWait());
        register(new ProgWidgetRename());
        register(new ProgWidgetSuicide());
        register(new ProgWidgetExternalProgram());
        register(new ProgWidgetCrafting());
        register(new ProgWidgetStandby());
        register(new ProgWidgetLogistics());
        register(new ProgWidgetForEachCoordinate());
        register(new ProgWidgetForEachItem());
        register(new ProgWidgetEditSign());
        register(new ProgWidgetCoordinateCondition());
        register(new ProgWidgetRedstoneCondition());
        register(new ProgWidgetLightCondition());
        register(new ProgWidgetItemInventoryCondition());
        register(new ProgWidgetBlockCondition());
        register(new ProgWidgetLiquidInventoryCondition());
        register(new ProgWidgetEntityCondition());
        register(new ProgWidgetPressureCondition());
        register(new ProgWidgetItemCondition());
        register(new ProgWidgetDroneConditionItem());
        register(new ProgWidgetDroneConditionLiquid());
        register(new ProgWidgetDroneConditionEntity());
        register(new ProgWidgetDroneConditionPressure());
    }

    public static void register(IProgWidget widget){
        allRegisteredWidgets.put(widget.getWidgetString(), widget);
    }

    public static Set<String> getAllWidgetNames(){
        return allRegisteredWidgets.keySet();
    }

    public static void compileBlacklist(){
        registeredWidgets.clear();
        for(Map.Entry<String, IProgWidget> entry : allRegisteredWidgets.entrySet()) {
            if(!ProgWidgetConfig.blacklistedPieces.contains(entry.getKey())) {
                registeredWidgets.add(entry.getValue());
            }
        }
    }
}
