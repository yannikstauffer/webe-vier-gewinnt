package ch.ffhs.webe.hs2023.viergewinnt.user;

import ch.ffhs.webe.hs2023.viergewinnt.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping("/4gewinnt/currentUserId")
    public ResponseEntity<Integer> getCurrentUserId() {
        try {
            User currentUser = userService.getCurrentlyAuthenticatedUser();
            return ResponseEntity.ok(currentUser.getId());
        } catch (Exception e) {
            log.error("Error fetching current user ID", e);
            return ResponseEntity.badRequest().build();
        }
    }
}