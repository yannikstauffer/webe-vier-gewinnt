package ch.ffhs.webe.hs2023.viergewinnt.user.dto;

import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class UsersDto {
    private final UserDto triggeredBy;
    private final List<UserDto> users;

    public static UsersDto of(final User currentUser, final List<User> users) {
        final var usersDto = users.stream()
                .map(UserDto::of)
                .toList();

        return UsersDto.builder()
                .triggeredBy(UserDto.of(currentUser))
                .users(usersDto)
                .build();
    }
}
