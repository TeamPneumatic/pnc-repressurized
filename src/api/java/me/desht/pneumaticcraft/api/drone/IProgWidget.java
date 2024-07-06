/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.drone;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public interface IProgWidget {
    int getX();

    int getY();

    void setX(int x);

    void setY(int y);

    default void setPosition(int x, int y) {
        setX(x);
        setY(y);
    }

    int getWidth();

    int getHeight();

    ResourceLocation getTexture();

    Pair<Float,Float> getMaxUV();

    void getTooltip(List<Component> curTooltip);

    void addWarnings(List<Component> curInfo, List<IProgWidget> widgets);

    void addErrors(List<Component> curInfo, List<IProgWidget> widgets);

    /**
     * Can this widget connect to a widget above?
     * @return true if it can, false otherwise
     */
    boolean hasStepInput();

    /**
     * Can another widget be connected below this widget?
     * @return true if it can, false otherwise
     */
    boolean hasStepOutput();

    default boolean freeToUse() { return false; }

    ProgWidgetType<?> getType();

    /**
     * Get the AI for this progwidget
     * @param drone the drone
     * @param widget will be 'this' most of the time, but not when controlled externally (e.g. ComputerCraft)
     * @return widget AI
     */
    Goal getWidgetAI(IDrone drone, IProgWidget widget);

    /**
     * Get the targeting AI for this progwidget
     * @param drone the drone
     * @param widget Will be 'this' most of the time, but not when controlled externally (e.g. ComputerCraft)
     * @return widget targeting AI
     */
    Goal getWidgetTargetAI(IDrone drone, IProgWidget widget);

    /**
     * Set the output widget for this widget, i.e. next in the program.  Called when building the program.
     * @param widget the next widget in the program
     */
    void setOutputWidget(IProgWidget widget);

    /**
     * Get the next widget in the program; the widget attached to the bottom of this one.
     *
     * @return the next widget to run
     */
    IProgWidget getOutputWidget();

    /**
     * Get the next widget in the program, which may or may not be the widget attached to the bottom of this one.
     * This method variant is called when running in a live program, and has access to the drone
     * context and a view of the full program, so it can deal with special conditions like jumps etc.
     *
     * @param drone the drone
     * @param allWidgets a list of widgets
     * @return the next widget to run
     */
    IProgWidget getOutputWidget(IDrone drone, List<IProgWidget> allWidgets);

    /**
     * For parameter widgets that are added onto the left or right of another widget, get the type of the widget
     * being connected to.
     *
     * @return the widget being connected to, or null if this widget isn't a parameter widget.
     */
    ProgWidgetType<?> returnType();

    /**
     * Get the types of the widgets which connect to this widget on the right, in order (top to bottom). Note that the
     * length of the widget array returned by {@link #getConnectedParameters()} will be double the length of this list,
     * since these widgets can be added on both the right (whitelist) and left (blacklist) sides.
     *
     * @return a list of widget types, or an empty list if no widgets can be added to the side of this widget
     */
    @Nonnull
    List<ProgWidgetType<?>> getParameters();

    void setParameter(int index, IProgWidget parm);

    boolean canSetParameter(int index);

    IProgWidget[] getConnectedParameters();

    void setParent(IProgWidget widget);

    IProgWidget getParent();

    ResourceLocation getTypeID();

    default String getTranslationKey() {
        String s = getTypeID().toString().replace(':', '.');
        return "programmingPuzzle." + s + ".name";
    }

    DyeColor getColor();

    /**
     * Is this widget currently available in this world?  i.e. make sure it's not blacklisted by server admin,
     * and any mod dependencies are loaded
     * @return true if the widget is available for use by players
     */
    boolean isAvailable();

    Optional<? extends IProgWidget> copy(HolderLookup.Provider provider);

    IProgWidget copyWidget();

    boolean canBeRunByComputers(IDrone drone, IProgWidget widget);

    default boolean isDifficultyOK(WidgetDifficulty difficulty) {
        return getDifficulty().isNotMoreDifficult(difficulty);
    }

    WidgetDifficulty getDifficulty();

    @Nonnull
    List<Component> getExtraStringInfo();

    /**
     * Cast from the API interface to our internal interface.  Should always succeed!
     *
     * @param type type of the progwidget
     * @return the internal non-API progwidget type
     */
    static IProgWidget create(ProgWidgetType<?> type) {
        return type.create();
//        IProgWidgetBase base = type.create();
//        Validate.isTrue(base instanceof IProgWidget);
//        return (IProgWidgetBase) base;
    }

    enum WidgetDifficulty {
        EASY("easy", 0),
        MEDIUM("medium", 1),
        ADVANCED("advanced", 2);

        private final String name;
        private final int difficultyLevel;

        WidgetDifficulty(String name, int difficultyLevel) {
            this.name = name;
            this.difficultyLevel = difficultyLevel;
        }

        public String getTranslationKey() {
            return "pneumaticcraft.gui.progWidget.difficulty." + name;
        }

        public String getTooltipTranslationKey() { return "pneumaticcraft.gui.programmer.difficulty." + name + ".tooltip"; }

        public boolean isNotMoreDifficult(WidgetDifficulty other) {
            return this.difficultyLevel <= other.difficultyLevel;
        }
    }
}
