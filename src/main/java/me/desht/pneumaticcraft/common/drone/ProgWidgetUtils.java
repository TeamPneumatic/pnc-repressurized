package me.desht.pneumaticcraft.common.drone;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import me.desht.pneumaticcraft.api.drone.IProgWidget;
import me.desht.pneumaticcraft.api.drone.ProgWidgetType;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.desht.pneumaticcraft.common.drone.progwidgets.ProgWidget.*;

public class ProgWidgetUtils {
    private static final int SNAP_MARGIN = 4;
    private static final int HALF_HEIGHT = PROGWIDGET_HEIGHT / 2;

    public static List<IProgWidget> getWidgetsFromNBT(HolderLookup.Provider provider, Tag tag) {
        return filterAvailable(LIST_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag)
                .resultOrPartial(message -> Log.warning("deserialization error: {}", message))
                .orElse(List.of())
        );
    }

    public static List<IProgWidget> getWidgetsFromJson(HolderLookup.Provider provider, JsonElement json) {
        return filterAvailable(Versioned.CODEC.parse(provider.createSerializationContext(JsonOps.INSTANCE), json)
                .resultOrPartial(message -> Log.warning("deserialization error: {}", message))
                .map(Versioned::widgets)
                .orElse(List.of())
        );
    }

    private static List<IProgWidget> filterAvailable(List<IProgWidget> list) {
        ImmutableList.Builder<IProgWidget> b = ImmutableList.builder();
        for (IProgWidget w : list) {
            if (w.isAvailable()) {
                b.add(w);
            } else {
                Log.warning("ignoring unavailable widget type: {}" + w.getType());
            }
        }
        return b.build();
    }

    public static Tag putWidgetsToNBT(HolderLookup.Provider provider, List<IProgWidget> widgets) {
        return LIST_CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), widgets)
                .getOrThrow();
    }

    public static JsonElement putWidgetsToJson(HolderLookup.Provider provider, List<IProgWidget> widgets) {
        return Versioned.CODEC.encodeStart(provider.createSerializationContext(JsonOps.INSTANCE),
                        new Versioned(JSON_VERSION, widgets))
                .getOrThrow();
    }

    /**
     * Scan the list of progwidgets, and recalculate how they all connect to each other
     *
     * @param progWidgets the list to updated; updated in-place
     */
    public static void updatePuzzleConnections(List<IProgWidget> progWidgets) {
        // pre-indexing all the widgets by position makes this an O(n) operation
        Map<PositionFields, IProgWidget> byPosition = new HashMap<>();

        // initial reset of all connections and indexing by widget position
        for (IProgWidget widget : progWidgets) {
            widget.setParent(null);
            List<ProgWidgetType<?>> parameters = widget.getParameters();
            for (int paramIdx = 0; paramIdx < parameters.size() * 2; paramIdx++) {
                widget.setParameter(paramIdx, null);
            }
            if (widget.hasStepOutput()) {
                widget.setOutputWidget(null);
            }
            byPosition.put(new PositionFields(widget.getX(), widget.getY()), widget);
        }

        for (IProgWidget checkedWidget : progWidgets) {
            // check for whitelist parameters (to the right of the checked widget)
            List<ProgWidgetType<?>> parameters = checkedWidget.getParameters();
            for (int paramIdx = 0; paramIdx < parameters.size(); paramIdx++) {
                IProgWidget widget = byPosition.get(PositionFields.rightParam(checkedWidget, paramIdx));
                if (widget != null && checkedWidget.canSetParameter(paramIdx) && parameters.get(paramIdx) == widget.returnType()) {
                    checkedWidget.setParameter(paramIdx, widget);
                    widget.setParent(checkedWidget);
                }
            }

            // check for next widget (to the bottom of the checked widget)
            if (checkedWidget.hasStepOutput()) {
                IProgWidget widget = byPosition.get(PositionFields.below(checkedWidget));
                if (widget != null && widget.hasStepInput()) {
                    checkedWidget.setOutputWidget(widget);
                }
            }
        }

        for (IProgWidget checkedWidget : progWidgets) {
            // second pass to check for blacklist parameters (to the left of the checked widget)
            if (checkedWidget.returnType() == null) {
                // this is a program widget rather than a parameter widget (area, item filter)
                List<ProgWidgetType<?>> parameters = checkedWidget.getParameters();
                for (int paramIdx = 0; paramIdx < parameters.size(); paramIdx++) {
                    if (checkedWidget.canSetParameter(paramIdx)) {
                        IProgWidget widget = byPosition.get(PositionFields.leftParam(checkedWidget, paramIdx));
                        if (widget != null && parameters.get(paramIdx) == widget.returnType()) {
                            IProgWidget root = widget;
                            while (root.getParent() != null) {
                                root = root.getParent();
                            }
                            checkedWidget.setParameter(paramIdx + parameters.size(), root);
                        }
                    }
                }
            }
        }
    }

    /**
     * Here's where we "snap together" nearby widgets which can connect.  Called when a widget is placed, on mouse
     * release.
     * @param placingWidget the widget being placed
     * @param allWidgets all the existing widgets
     */
    public static void snapWidgetIntoProgram(IProgWidget placingWidget, List<IProgWidget> allWidgets) {
        // Check for connection to the left of the dragged widget.
        ProgWidgetType<?> returnValue = placingWidget.returnType();
        if (returnValue != null) {
            for (IProgWidget widget : allWidgets) {
                if (widget != placingWidget && Math.abs(widget.getX() + widget.getWidth() / 2 - placingWidget.getX()) <= SNAP_MARGIN) {
                    List<ProgWidgetType<?>> parameters = widget.getParameters();
                    for (int i = 0; i < parameters.size(); i++) {
                        if (widget.canSetParameter(i) && parameters.get(i) == returnValue
                                && Math.abs(widget.getY() + i * 11 - placingWidget.getY()) <= SNAP_MARGIN) {
                            positionConnectedWidgets(placingWidget,
                                    widget.getX() + widget.getWidth() / 2,
                                    widget.getY() + i * HALF_HEIGHT);
                            return;
                        }
                    }
                }
            }
        }

        // check for connection to the right of the placing widget
        List<ProgWidgetType<?>> parameters = placingWidget.getParameters();
        if (!parameters.isEmpty()) {
            for (IProgWidget widget : allWidgets) {
                IProgWidget outerPiece = placingWidget;
                if (outerPiece.returnType() != null) {
                    // The widget is a parameter piece (area, item filter, text)
                    while (outerPiece.getConnectedParameters()[0] != null) {
                        outerPiece = outerPiece.getConnectedParameters()[0];
                    }
                }
                if (widget != placingWidget && Math.abs(outerPiece.getX() + outerPiece.getWidth() / 2 - widget.getX()) <= SNAP_MARGIN) {
                    if (widget.returnType() != null) {
                        for (int i = 0; i < parameters.size(); i++) {
                            if (placingWidget.canSetParameter(i) && parameters.get(i) == widget.returnType()
                                    && Math.abs(placingWidget.getY() + i * HALF_HEIGHT - widget.getY()) <= SNAP_MARGIN) {
                                positionConnectedWidgets(placingWidget,
                                        widget.getX() - placingWidget.getWidth() / 2 - (outerPiece.getX() - placingWidget.getX()),
                                        widget.getY() - i * 11);
                            }
                        }
                    } else {
                        List<ProgWidgetType<?>> params = widget.getParameters();
                        for (int paramIdx = 0; paramIdx < params.size(); paramIdx++) {
                            if (widget.canSetParameter(paramIdx + parameters.size()) && params.get(paramIdx) == parameters.getFirst()
                                    && Math.abs(widget.getY() + paramIdx * 11 - placingWidget.getY()) <= SNAP_MARGIN) {
                                positionConnectedWidgets(placingWidget,
                                        widget.getX() - placingWidget.getWidth() / 2 - (outerPiece.getX() - placingWidget.getX()),
                                        widget.getY() + paramIdx * HALF_HEIGHT);
                            }
                        }
                    }
                }
            }
        }

        // check for connection to the top of the placing widget
        if (placingWidget.hasStepInput()) {
            for (IProgWidget widget : allWidgets) {
                if (widget.hasStepOutput() && Math.abs(widget.getX() - placingWidget.getX()) <= SNAP_MARGIN && Math.abs(widget.getY() + widget.getHeight() / 2 - placingWidget.getY()) <= SNAP_MARGIN) {
                    positionConnectedWidgets(placingWidget, widget.getX(), widget.getY() + widget.getHeight() / 2);
                }
            }
        }

        // check for connection to the bottom of the dragged widget.
        if (placingWidget.hasStepOutput()) {
            for (IProgWidget widget : allWidgets) {
                if (widget.hasStepInput() && Math.abs(widget.getX() - placingWidget.getX()) <= SNAP_MARGIN && Math.abs(widget.getY() - placingWidget.getY() - placingWidget.getHeight() / 2) <= SNAP_MARGIN) {
                    positionConnectedWidgets(placingWidget, widget.getX(), widget.getY() - placingWidget.getHeight() / 2);
                }
            }
        }
    }

    /**
     * Set the position of the given widget and (recursively) all widgets which connect to it on the sides or below
     * (but not above).
     *
     * @param widget the widget
     * @param x new X pos
     * @param y new Y pos
     */
    public static void positionConnectedWidgets(IProgWidget widget, int x, int y) {
        widget.setPosition(x, y);

        IProgWidget[] connectingWidgets = widget.getConnectedParameters();
        if (connectingWidgets != null) {
            for (int i = 0; i < connectingWidgets.length; i++) {
                if (connectingWidgets[i] != null) {
                    if (i < connectingWidgets.length / 2) {
                        positionConnectedWidgets(connectingWidgets[i], x + widget.getWidth() / 2, y + i * HALF_HEIGHT);
                    } else {
                        int totalWidth = 0;
                        IProgWidget branch = connectingWidgets[i];
                        while (branch != null) {
                            totalWidth += branch.getWidth() / 2;
                            branch = branch.getConnectedParameters()[0];
                        }
                        positionConnectedWidgets(connectingWidgets[i], x - totalWidth, y + (i - connectingWidgets.length / 2) * HALF_HEIGHT);
                    }
                }
            }
        }
        IProgWidget outputWidget = widget.getOutputWidget();
        if (outputWidget != null) positionConnectedWidgets(outputWidget, x, y + widget.getHeight() / 2);
    }
}
