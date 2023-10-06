package ch.ffhs.webe.hs2023.viergewinnt.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Slf4j
@Controller
public class LoginController {
    @GetMapping("/login")
    String login(final Principal user) {
        return user == null
                ? "login"
                : "redirect:/";
    }

    @GetMapping("/logout")
    String logout(final Principal user) {
        return user == null
                ? "redirect:/login"
                : "logout";
    }
}
