package ch.ffhs.webe.hs2023.viergewinnt.user.dto;

import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import ch.ffhs.webe.hs2023.viergewinnt.user.values.UserUpdateType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateDto {
    private final UserDto user;
    private final UserUpdateType updateType;

    public static UserUpdateDto of(final User user, final UserUpdateType userUpdateType) {
        final var userDto = UserDto.of(user);
        return UserUpdateDto.builder()
                .user(userDto)
                .updateType(userUpdateType)
                .build();
    }
}
