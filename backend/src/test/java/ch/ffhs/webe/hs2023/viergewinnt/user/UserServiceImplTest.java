package ch.ffhs.webe.hs2023.viergewinnt.user;

import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.LoginDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import ch.ffhs.webe.hs2023.viergewinnt.user.repository.UserRepository;
import ch.ffhs.webe.hs2023.viergewinnt.user.values.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class UserServiceImplTest {

    @MockBean
    UserRepository userRepository;

    @MockBean
    PasswordEncoder passwordEncoder;

    @Autowired
    UserServiceImpl userServiceImpl;

    @Test
    void registerNewUserAccount() {
        //arrange
        final var loginDto = this.loginDto();
        final var encodedPassword = "bar$foo";
        when(this.userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.empty());
        when(this.userRepository.countUsers()).thenReturn(0);
        when(this.userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(this.passwordEncoder.encode(loginDto.getPassword())).thenReturn(encodedPassword);

        //act
        final var user = this.userServiceImpl.registerNewUserAccount(loginDto);

        //assert
        verify(this.userRepository, times(1)).save(user);

        assertThat(loginDto).isNotNull();
        assertThat(user).isNotNull();
        assertThat(encodedPassword).isNotNull();
        assertThat(loginDto.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(loginDto.getLastName()).isEqualTo(user.getLastName());
        assertThat(loginDto.getEmail()).isEqualTo(user.getEmail());
        assertThat(encodedPassword).isEqualTo(user.getPassword());
        assertThat(user.getRoles()).contains(Role.USER);
    }

    @Test
    void registerNewUserAccount_throwsVierGewinntException_withAlreadyExistingEmail() {
        //arrange
        final var loginDto = this.loginDto();
        when(this.userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(new User()));

        //act + assert
        assertThatThrownBy(() -> this.userServiceImpl.registerNewUserAccount(loginDto))
                .isInstanceOf(VierGewinntException.class)
                .hasMessageContaining("Email address " + loginDto.getEmail() + " already in use for another account");
    }

    @Test
    void registerNewUserAccount_addsAdminRole_whenNoOtherUsersAreRegistered() {
        //arrange
        final var loginDto = this.loginDto();
        final var encodedPassword = "bar$foo";
        when(this.userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.empty());
        when(this.userRepository.countUsers()).thenReturn(0);
        when(this.userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(this.passwordEncoder.encode(loginDto.getPassword())).thenReturn(encodedPassword);

        //act
        final var user = this.userServiceImpl.registerNewUserAccount(loginDto);

        //assert
        verify(this.userRepository, times(1)).save(user);

        assertThat(user.getRoles()).contains(Role.ADMIN);
    }

    @Test
    void registerNewUserAccount_addsNoAdminRole_whenOtherUsersAreRegistered() {
        //arrange
        final var loginDto = this.loginDto();
        final var encodedPassword = "bar$foo";
        when(this.userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.empty());
        when(this.userRepository.countUsers()).thenReturn(1);
        when(this.userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(this.passwordEncoder.encode(loginDto.getPassword())).thenReturn(encodedPassword);

        //act
        final var user = this.userServiceImpl.registerNewUserAccount(loginDto);

        //assert
        verify(this.userRepository, times(1)).save(user);

        assertThat(user.getRoles()).doesNotContain(Role.ADMIN);
    }

    @Test
    void getUserById() {
        // arrange
        final var expectedUser = mock(User.class);
        when(this.userRepository.findById(1)).thenReturn(Optional.of(expectedUser));

        // act
        final var actualUser = this.userServiceImpl.getUserById(1);

        // assert
        assertThat(actualUser).isEqualTo(expectedUser);
    }

    @Test
    void getUserById_throwsVierGewinntException_whenUserIsNotFound() {
        // arrange
        when(this.userRepository.findById(1)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> this.userServiceImpl.getUserById(1))
                .isInstanceOf(VierGewinntException.class)
                .hasMessageContaining("User with id 1 not found");
    }

    @Test
    void getUserByEmail() {
        // arrange
        final var expectedUser = mock(User.class);
        when(this.userRepository.findByEmail("foo@bar.com")).thenReturn(Optional.of(expectedUser));

        // act
        final var actualUser = this.userServiceImpl.getUserByEmail("foo@bar.com");

        // assert
        assertThat(actualUser).isEqualTo(expectedUser);
    }

    @Test
    void getUserByEmail_throwsVierGewinntException_ifUserIsNotFound() {
        // arrange
        when(this.userRepository.findByEmail("foo@bar.com")).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> this.userServiceImpl.getUserByEmail("foo@bar.com"))
                .isInstanceOf(VierGewinntException.class)
                .hasMessageContaining("User with email foo@bar.com not found");
    }

    LoginDto loginDto() {
        return LoginDto.builder()
                .firstName("foo")
                .lastName("bar")
                .email("foo@bar.com")
                .password("foobar")
                .matchingPassword("foobar")
                .build();
    }
}