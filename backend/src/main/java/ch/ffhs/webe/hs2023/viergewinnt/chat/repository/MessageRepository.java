package ch.ffhs.webe.hs2023.viergewinnt.chat.repository;

import ch.ffhs.webe.hs2023.viergewinnt.chat.model.Message;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends CrudRepository<Message, Integer> {

    @Query("SELECT m FROM messages m WHERE m.receiver = :receiverId ORDER BY m.sentAt")
    List<Message> findByReceiverId(@Param("receiverId") int receiverId);

    @Query("SELECT m FROM messages m WHERE m.sender = :senderId ORDER BY m.sentAt")
    List<Message> findBySenderId(@Param("senderId") int senderId);

    @Query("SELECT m FROM messages m WHERE m.sender = :senderId AND m.receiver = :receiverId ORDER BY m.sentAt")
    List<Message> findBySenderIdAndReceiverId(@Param("senderId") int senderId, @Param("receiverId") int receiverId);

}
