package ch.ffhs.webe.hs2023.viergewinnt.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest
class LoginControllerTest {

    @Autowired
    LoginController loginController;

    @Test
    void login_redirectsToMain_ifLoggedIn() {
        // arrange
        final var principal = mock(Principal.class);

        // act
        final var actual = this.loginController.login(principal);

        // assert
        assertThat(actual).isEqualTo("redirect:/");
    }

    @Test
    void logout_redirectsToLogin_ifNotLoggedIn() {
        // act
        final var actual = this.loginController.logout(null);

        // assert
        assertThat(actual).isEqualTo("redirect:/login");
    }


    @Test
    void login() {
        // act
        final var actual = this.loginController.login(null);

        // assert
        assertThat(actual).isEqualTo("login");

    }


    @Test
    void logout() {
        // arrange
        final var principal = mock(Principal.class);

        // act
        final var actual = this.loginController.logout(principal);

        // assert
        assertThat(actual).isEqualTo("logout");
    }
}