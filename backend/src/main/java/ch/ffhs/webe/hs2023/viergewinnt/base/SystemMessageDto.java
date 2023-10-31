package ch.ffhs.webe.hs2023.viergewinnt.base;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SystemMessageDto {
    private String code;
    private String messageKey;

    public static SystemMessageDto of(final SystemMessageCode code) {
        return SystemMessageDto.builder()
                .code(code.toString())
                .messageKey(code.getInternationalizedMessageKey())
                .build();
    }
}
