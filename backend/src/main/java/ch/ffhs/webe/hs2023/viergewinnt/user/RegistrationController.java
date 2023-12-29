package ch.ffhs.webe.hs2023.viergewinnt.user;

import ch.ffhs.webe.hs2023.viergewinnt.base.VierGewinntException;
import ch.ffhs.webe.hs2023.viergewinnt.user.dto.LoginDto;
import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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
    public String showRegistrationForm(final Model model) {
        final LoginDto loginDto = new LoginDto();
        model.addAttribute("user", loginDto);
        return "registration";
    }

    @PostMapping("/processRegistration")
    public ModelAndView registerUserAccount(@ModelAttribute("user") @Valid final LoginDto loginDto, final BindingResult bindingResult, final ModelAndView mav) {
        if (bindingResult.hasErrors()) {
            mav.addObject("user", loginDto);
            mav.addObject("message", bindingResult.getFieldError().getDefaultMessage());
            mav.setViewName("registration");
            return mav;
        }

        try {
            final User registered = this.userService.registerNewUserAccount(loginDto);
            log.debug("Registered new user with email: {}", registered.getEmail());
        } catch (final VierGewinntException exception) {
            mav.addObject("globalError", exception.getErrorCode().getInternationalizedMessageKey());
            mav.addObject("user", loginDto);
            mav.setViewName("registration");
            return mav;
        }
        return new ModelAndView("redirect:/login?registered");
    }
}
