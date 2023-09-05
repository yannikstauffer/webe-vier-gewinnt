package ch.ffhs.webe.hs2023.viergewinnt.chat.repository;

import ch.ffhs.webe.hs2023.viergewinnt.chat.model.Message;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends CrudRepository<Message, Integer> {

    @Query("SELECT m FROM Message m WHERE m.receiver = :receiver ORDER BY m.timestamp")
    List<Message> findMessagesByReceiver(@Param("receiver") int receiverId);

    @Query("SELECT m FROM Message m WHERE m.sender = :sender ORDER BY m.timestamp")
    List<Message> findMessagesBySender(@Param("sender") int senderId);

}
