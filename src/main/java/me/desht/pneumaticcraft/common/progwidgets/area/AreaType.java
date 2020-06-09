package me.desht.pneumaticcraft.common.progwidgets.area;

import me.desht.pneumaticcraft.common.util.LegacyAreaWidgetConverter;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AreaType{
    private final String unlocalizedName;

    public enum EnumAxis {
        X, Y, Z


    }
    public AreaType(String name){
        this.unlocalizedName = String.format("pneumaticcraft.gui.progWidget.area.type.%s.name", name);
    }
    public String getName(){
        return I18n.format(unlocalizedName);
    }

    public abstract void addArea(Consumer<BlockPos> areaAdder, BlockPos p1, BlockPos p2, int minX, int minY, int minZ, int maxX, int maxY, int maxZ);

    /**
     * Called when loading old-style programs from pastebin etc.  Convert any old-fashioned area representations to
     * their modern equivalents.
     *
     * @param oldType the old-style area type
     * @param typeInfo extra integer type information used by some types
     */
    public void convertFromLegacy(LegacyAreaWidgetConverter.EnumOldAreaType oldType, int typeInfo) {
    }

    /**
     * Whether or not the area added in addArea is deterministic (used to determine if stuff can be cached or not).
     * @return
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isDeterministic(){
        return true;
    }

    public void writeToPacket(PacketBuffer buffer) {

    }

    public void readFromPacket(PacketBuffer buf) {

    }

    public void writeToNBT(CompoundNBT tag){

    }

    public void readFromNBT(CompoundNBT tag){
        
    }

    public void addUIWidgets(List<AreaTypeWidget> widgets){
        
    }
        
    public static abstract class AreaTypeWidget{
        public final String title;
        
        public AreaTypeWidget(String title){
            this.title = title;
        }
        
        public abstract String getCurValue();
    }
    
    /**
     * Adds a number textbox.
     */
    public static class AreaTypeWidgetInteger extends AreaTypeWidget{
        public final Supplier<Integer> readAction;
        public final Consumer<Integer> writeAction;
        
        public AreaTypeWidgetInteger(String title, Supplier<Integer> readAction, Consumer<Integer> writeAction){
            super(title);
            this.readAction = readAction;
            this.writeAction = writeAction;
        }

        @Override
        public String getCurValue(){
            return readAction.get().toString();
        }
    }
    
    /**
     * Adds a dropdownlist with alle the enum options
     * @author Maarten
     *
     */
    public static class AreaTypeWidgetEnum<E extends Enum<?>> extends AreaTypeWidget{

        public final Class<E> enumClass;
        public final Supplier<E> readAction;
        public final Consumer<E> writeAction;
        
        public AreaTypeWidgetEnum(String title, Class<E> enumClass, Supplier<E> readAction, Consumer<E> writeAction){
            super(title);
            this.enumClass = enumClass;
            this.readAction = readAction;
            this.writeAction = writeAction;
        }

        @Override
        public String getCurValue(){
            return readAction.get().toString();
        }
        
    }
}
