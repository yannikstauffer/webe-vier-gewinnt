package ch.ffhs.webe.hs2023.viergewinnt.user.values;

import lombok.AllArgsConstructor;

@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public enum Role {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    private final String value;

    @Override
    public String toString() {
        return this.value;
    }

}