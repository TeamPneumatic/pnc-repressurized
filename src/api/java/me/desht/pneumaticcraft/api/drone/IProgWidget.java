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

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.DyeColor;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;
import java.util.List;

public interface IProgWidget {
    @ApiStatus.NonExtendable
    int getX();

    @ApiStatus.NonExtendable
    int getY();

    @ApiStatus.NonExtendable
    void setX(int x);

    @ApiStatus.NonExtendable
    void setY(int y);

    @ApiStatus.NonExtendable
    default void setPosition(int x, int y) {
        setX(x);
        setY(y);
    }

    int getWidth();

    int getHeight();

    ResourceLocation getTexture();

    @ApiStatus.NonExtendable
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

    /**
     * {@return true if this widget does not require any Programming Puzzle items to program a drone or Network API}
     */
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

    @ApiStatus.NonExtendable
    void setParameter(int index, IProgWidget paramWidget);

    boolean canSetParameter(int index);

    @ApiStatus.NonExtendable
    IProgWidget[] getConnectedParameters();

    @ApiStatus.NonExtendable
    void setParent(IProgWidget widget);

    @ApiStatus.NonExtendable
    IProgWidget getParent();

    @ApiStatus.NonExtendable
    ResourceLocation getTypeID();

    @ApiStatus.NonExtendable
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

    /**
     * Make a deep copy of this widget. It is essential that <strong>all</strong> mutable non-primitive fields of the
     * widget are properly deep-copied. Immutable fields may be shallow-copied. Only serialized fields need to be
     * copied; any fields which are dynamically calculated during a drone's runtime can be ignored.
     *
     * @return a copy of this progwidget
     */
    IProgWidget copyWidget();

    /**
     * Can this widget be run by a drone which is under the control of a Drone Interface (ComputerCraft) ?
     * @param drone the drone
     * @param widget the widget to check, which is not necessarily this widget!
     * @return true if the widget can be run, false otherwise
     */
    boolean canBeRunByComputers(IDrone drone, IProgWidget widget);

    /**
     * Check if this widget's difficulty level is OK for the Programmer's current difficulty level. Used by the
     * Programmer GUI to determine if this widget should be displayed in the widget tray.
     *
     * @param difficulty the Programmer's difficulty level
     * @return true if the widget should be displayed, false otherwise
     */
    @ApiStatus.NonExtendable
    default boolean isDifficultyOK(WidgetDifficulty difficulty) {
        return getDifficulty().isNotMoreDifficult(difficulty);
    }

    /**
     * Get the difficulty level of this widget, which determines when it will be displayed in the Programmer GUI
     * widget tray.
     *
     * @return the widget's difficulty
     */
    WidgetDifficulty getDifficulty();

    @Nonnull
    List<Component> getExtraStringInfo();

    /**
     * Create a default instance of the given widget type (as if the widget were just created from the
     * Programmer GUI's widget tray).
     *
     * @param type type of the progwidget
     * @return the a new progwidget with default settings
     */
    static IProgWidget create(ProgWidgetType<?> type) {
        return type.create();
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
