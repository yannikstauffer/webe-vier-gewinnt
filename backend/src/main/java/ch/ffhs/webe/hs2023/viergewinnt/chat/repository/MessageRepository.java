package ch.ffhs.webe.hs2023.viergewinnt.chat.repository;

import ch.ffhs.webe.hs2023.viergewinnt.chat.model.Message;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends CrudRepository<Message, Integer> {
    @Query("""
            SELECT m FROM messages m
            WHERE (m.sender = :user OR m.receiver = :user)
            AND m.messageType = 'PRIVATE'
            ORDER BY m.sentAt
            """)
    List<Message> findPrivateBy(@Param("user") User user);

    @Query("""
            SELECT m FROM messages m
            WHERE m.sentAt > :since AND m.messageType = 'PUBLIC'
            ORDER BY m.sentAt
            """)
    List<Message> findPublicBy(@Param("since") LocalDateTime since);

}
