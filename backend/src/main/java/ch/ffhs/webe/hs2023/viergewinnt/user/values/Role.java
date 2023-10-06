package ch.ffhs.webe.hs2023.viergewinnt.user.values;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public enum Role {
    USER(Role.Values.USER),
    ADMIN(Role.Values.ADMIN);

    private final String value;

    @Override
    public String toString() {
        return this.value;
    }


    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static class Values {
        public static final String USER = "ROLE_USER";
        public static final String ADMIN = "ROLE_ADMIN";
    }
}