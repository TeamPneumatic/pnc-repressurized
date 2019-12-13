package me.desht.pneumaticcraft.client.gui.widget;

/**
 * Represents a widget with a string tag, which is sent to the server in a PacketUpdateGui when the widget is clicked.
 * The open container must implement {@link me.desht.pneumaticcraft.common.tileentity.IGUIButtonSensitive} to
 * process the received packet.
 */
public interface ITaggedWidget {
    /**
     * Get the data to send to the server.  This is just some arbitrary string data which will be processed by the
     * server-side container.
     *
     * @return  a string tag containing any arbitrary information
     */
    String getTag();
}
