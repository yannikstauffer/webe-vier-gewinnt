package ch.ffhs.webe.hs2023.viergewinnt.base;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {
    @RequestMapping("/error")
    String error(final HttpServletRequest request) {
        return "redirect:/";
    }
}
