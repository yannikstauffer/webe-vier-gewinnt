package ch.ffhs.webe.hs2023.viergewinnt.session;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {
    @RequestMapping("/4gewinnt/csrf")
    public CsrfToken csrf(final CsrfToken token) {
        return token;
    }
}
