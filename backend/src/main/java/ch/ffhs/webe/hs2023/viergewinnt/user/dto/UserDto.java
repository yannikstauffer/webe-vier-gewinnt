package ch.ffhs.webe.hs2023.viergewinnt.user.dto;

import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserDto {
    private int id;
    private String firstName;
    private String lastName;

    public static UserDto of(final User user) {
        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    public static List<UserDto> of(final List<User> users) {
        return users.stream()
                .map(UserDto::of)
                .toList();
    }

}
