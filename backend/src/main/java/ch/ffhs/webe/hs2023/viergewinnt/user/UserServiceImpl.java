package ch.ffhs.webe.hs2023.viergewinnt.user;

import ch.ffhs.webe.hs2023.viergewinnt.base.ErrorCode;
import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.LoginDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import ch.ffhs.webe.hs2023.viergewinnt.user.repository.UserRepository;
import ch.ffhs.webe.hs2023.viergewinnt.user.values.Role;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
@Transactional
class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(final UserRepository userRepository, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerNewUserAccount(final LoginDto loginDto) throws VierGewinntException {
        if (this.emailExists(loginDto.getEmail())) {
            throw VierGewinntException.of(
                    ErrorCode.EMAIL_ALREADY_EXISTS,
                    String.format("Email address %s already in use for another account", loginDto.getEmail())
            );
        }

        final User user = new User();
        user.setFirstName(loginDto.getFirstName());
        user.setLastName(loginDto.getLastName());
        user.setPassword(this.passwordEncoder.encode(loginDto.getPassword()));
        user.setEmail(loginDto.getEmail());
        user.setRoles(Collections.singletonList(Role.USER));

        return this.userRepository.save(user);
    }

    @Override
    public User getUserById(final int id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> VierGewinntException.of(
                        ErrorCode.USER_NOT_FOUND,
                        "User with id " + id + " not found"));
    }

    @Override
    public User getUserByEmail(final String email) throws VierGewinntException {
        return this.userRepository.findByEmail(email)
                .orElseThrow(() -> VierGewinntException.of(
                        ErrorCode.USER_NOT_FOUND,
                        "User with email " + email + " not found"));
    }

    /**
     * Ruft den aktuell authentifizierten Benutzer ab
     *
     * @return User
     */
    @Override
    public User getCurrentlyAuthenticatedUser() {
        final String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return this.getUserByEmail(username);
    }

    @Override
    public Optional<User> findUserById(final int id) {
        return this.userRepository.findById(id);
    }

    private boolean emailExists(final String email) {
        return this.userRepository.findByEmail(email).isPresent();
    }


}
