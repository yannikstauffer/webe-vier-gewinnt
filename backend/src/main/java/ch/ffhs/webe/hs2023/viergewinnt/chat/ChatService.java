package ch.ffhs.webe.hs2023.viergewinnt.chat;

import ch.ffhs.webe.hs2023.viergewinnt.chat.dto.InboundMessageDto;
import ch.ffhs.webe.hs2023.viergewinnt.chat.model.Message;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;

import java.util.List;

public interface ChatService {
    Message storePublicMessage(final InboundMessageDto inboundMessageDto, final User sender);

    Message storePrivateMessage(final InboundMessageDto inboundMessageDto, final User sender);

    List<Message> getPrivateMessages(User user);

    List<Message> getPublicMessages();
}
