/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.drone.progwidgets.area;

import me.desht.pneumaticcraft.common.util.LegacyAreaWidgetConverter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public abstract class AreaType {
    private final String translationKey;
    private final String name;

    public enum EnumAxis {
        X, Y, Z
    }

    public AreaType(String name) {
        this.name = name;
        this.translationKey = String.format("pneumaticcraft.gui.progWidget.area.type.%s.name", name);
    }

    public String getName() {
        return name;
    }

    public String getTranslationKey() {
        return translationKey;
    }

    /**
     * Actually add the positions defined by this area to the given blockpos set
     *  @param areaAdder the adder; call {@code adder.accept(pos)} to add a blockpos
     * @param p1 the first raw blockpos of the area
     * @param p2 the second raw blockpos of the area
     * @param minX min X coord
     * @param minY min Y coord
     * @param minZ min Z coord
     * @param maxX max X coord
     * @param maxY max Y coord
     * @param maxZ max Z coord
     */
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
     * @return true if deterministic, false otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isDeterministic(){
        return true;
    }

    public void writeToPacket(FriendlyByteBuf buffer) {

    }

    public void readFromPacket(FriendlyByteBuf buf) {

    }

    public void writeToNBT(CompoundTag tag){

    }

    public void readFromNBT(CompoundTag tag){
        
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
        public final IntSupplier readAction;
        public final IntConsumer writeAction;
        
        public AreaTypeWidgetInteger(String title, IntSupplier readAction, IntConsumer writeAction){
            super(title);
            this.readAction = readAction;
            this.writeAction = writeAction;
        }

        @Override
        public String getCurValue(){
            return String.valueOf(readAction.getAsInt());
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
