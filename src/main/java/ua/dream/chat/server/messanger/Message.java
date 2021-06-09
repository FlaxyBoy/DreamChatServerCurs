package ua.dream.chat.server.messanger;

import org.jetbrains.annotations.NotNull;
import ua.dream.chat.network.netty.packet.out.PacketOut8Message;

public interface Message {

    @NotNull String getSenderLogin();

    @NotNull String getDisplayName();

    @NotNull String getMessage();

    PacketOut8Message encode();

}
