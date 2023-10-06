package ch.ffhs.webe.hs2023.viergewinnt.chat.dto;

import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageUserDto {
    private int userId;
    private String firstName;
    private String lastName;

    public static MessageUserDto of(final User user) {
        return MessageUserDto.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

}
