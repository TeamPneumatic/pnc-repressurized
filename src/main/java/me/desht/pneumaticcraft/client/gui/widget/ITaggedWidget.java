package me.desht.pneumaticcraft.client.gui.widget;

/**
 * Represents a widget with a string tag, which is sent to the server in a PacketUpdateGui when the widget is clicked.
 * The open container must implement {@link me.desht.pneumaticcraft.common.tileentity.IGUIButtonSensitive}
 */
public interface ITaggedWidget {
    String getTag();
}
