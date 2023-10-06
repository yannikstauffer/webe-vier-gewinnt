package ch.ffhs.webe.hs2023.viergewinnt.user.dto;

import ch.ffhs.webe.hs2023.viergewinnt.user.validation.PasswordMatches;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@PasswordMatches
public class UserDto implements Serializable {
    @NotNull
    @NotEmpty
    private String firstName;

    @NotNull
    @NotEmpty
    private String lastName;

    @NotNull
    @NotEmpty
    @Email
    private String email;

    @NotNull
    @NotEmpty
    private String password;
    private String matchingPassword;

}
