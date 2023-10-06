package ch.ffhs.webe.hs2023.viergewinnt.user;

import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import ch.ffhs.webe.hs2023.viergewinnt.user.repository.UserRepository;
import ch.ffhs.webe.hs2023.viergewinnt.user.values.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        final User user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + email));

        final boolean enabled = true;
        final boolean accountNonExpired = true;
        final boolean credentialsNonExpired = true;
        final boolean accountNonLocked = true;

        return getSpringUser(user, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked);
    }

    private static org.springframework.security.core.userdetails.User getSpringUser(final User user, final boolean enabled, final boolean accountNonExpired, final boolean credentialsNonExpired, final boolean accountNonLocked) {
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                enabled,
                accountNonExpired,
                credentialsNonExpired,
                accountNonLocked,
                getAuthorities(user.getRoles()));
    }

    private static List<GrantedAuthority> getAuthorities(final List<Role> userRoles) {
        final List<GrantedAuthority> authorities = new ArrayList<>();
        for (final Role role : userRoles) {
            authorities.add(new SimpleGrantedAuthority(role.toString()));
        }
        return authorities;
    }
}
