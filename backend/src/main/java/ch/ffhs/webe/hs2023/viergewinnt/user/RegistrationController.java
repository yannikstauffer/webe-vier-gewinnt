package ch.ffhs.webe.hs2023.viergewinnt.user;

import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.UserDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
public class RegistrationController {
    private final UserService userService;

    @Autowired
    public RegistrationController(final UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/registration")
    public String showRegistrationForm(final WebRequest request, final Model model) {
        final UserDto userDto = new UserDto();
        model.addAttribute("user", userDto);
        return "registration";
    }

    @PostMapping("/processRegistration")
    public ModelAndView registerUserAccount(
            @ModelAttribute("user") @Valid final UserDto userDto,
            final ModelAndView mav,
            final HttpServletRequest request,
            final Errors errors) {

        try {
            final User registered = this.userService.registerNewUserAccount(userDto);
            log.debug("Registered new user with email: {}", registered.getEmail());
        } catch (final VierGewinntException exception) {
            mav.addObject("message", exception.getErrorCode());
            return mav;
        }
        return new ModelAndView("redirect:/login?registered");
    }
}
