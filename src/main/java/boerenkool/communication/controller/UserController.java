package boerenkool.communication.controller;

import boerenkool.business.model.User;
import boerenkool.business.service.UserService;
import boerenkool.communication.dto.UserDto;
import boerenkool.utilities.authorization.AuthorizationService;
import boerenkool.utilities.exceptions.MessageDoesNotExistException;
import boerenkool.utilities.exceptions.UserNotFoundException;
import boerenkool.utilities.exceptions.UserUpdateFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(value = "api/users")
public class UserController {

    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final AuthorizationService authorizationService;

    @Autowired
    public UserController(UserService userService, AuthorizationService authorizationService) {
        this.userService = userService;
        this.authorizationService = authorizationService;
        logger.info("New UserController created");
    }

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        List<User> users = userService.getAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<User> getOneById(@PathVariable("id") int id) {
        User user = userService.getOneById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found."));
        return ResponseEntity.ok(user);
    }

    @PutMapping("update")
    public ResponseEntity<?> updateUser(@RequestBody UserDto userDto, @RequestHeader("Authorization") String token) {
        Optional<User> userOpt = authorizationService.validate(UUID.fromString(token));
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getUsername().equals(userDto.getUsername())) {
                userService.updateOne(user);
                return ResponseEntity.ok("User updated successfully");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or username mismatch");
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteOne(@PathVariable("id") int id) {
        userService.getOneById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found."));
        userService.removeOneById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/username/{username}")
    public ResponseEntity<User> findOneByUsername(@PathVariable("username") String name) {
        User user = userService.findByUsername(name)
                .orElseThrow(() -> new UserNotFoundException("User with username '" + name + "' not found."));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile(@RequestHeader("Authorization") String token) {
        Optional<User> userOpt = authorizationService.validate(UUID.fromString(token));
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserDto userDto = new UserDto(user); // Verondersteld dat je een UserDto hebt die deze gegevens bevat
            return ResponseEntity.ok(userDto);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }

    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(@RequestBody UserDto userDto, @RequestHeader("Authorization") String token) {
        Optional<User> userOpt = authorizationService.validate(UUID.fromString(token));
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Prevent a "Verhuurder" from changing their type
            if ("Verhuurder".equals(user.getTypeOfUser()) && !"Verhuurder".equals(userDto.getTypeOfUser())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Verhuurder users cannot change their user type.");
            }
            if (user.getUsername().equals(userDto.getUsername())) {
                user.setEmail(userDto.getEmail());
                user.setPhone(userDto.getPhone());
                user.setFirstName(userDto.getFirstName());
                user.setInfix(userDto.getInfix());
                user.setLastName(userDto.getLastName());
                user.setTypeOfUser(userDto.getTypeOfUser());

                userService.updateOne(user);
                return ResponseEntity.ok("User updated successfully");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or username mismatch");
    }

    @DeleteMapping("/profile")
    public ResponseEntity<String> deleteProfile(@RequestHeader("Authorization") String token) {
        Optional<User> userOpt = authorizationService.validate(UUID.fromString(token));
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            userService.removeOneById(user.getUserId());
            return ResponseEntity.ok("User profile deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
    }

    @PutMapping("/update-coins")
    public ResponseEntity<String> updateBoerenkoolCoins(@RequestBody Map<String, Integer> updateData, @RequestHeader("Authorization") String token) {
        Optional<User> optionalUser = authorizationService.validate(UUID.fromString(token));

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            int additionalCoins = updateData.get("boerenkoolCoins");
            boolean success = userService.updateBoerenkoolcoins(user, additionalCoins);

            if (success) {
                return ResponseEntity.ok("Coin balance updated successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update coin balance.");
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
    }

    //code Bart
    @GetMapping(value = "/correspondents")
    public ResponseEntity<Object> getMapOfCorrespondents(@RequestHeader("Authorization") String token) {
        Optional<User> userOpt = authorizationService.validate(UUID.fromString(token));
        if (userOpt.isPresent()) {
            return new ResponseEntity<>(userService.getMapOfCorrespondents(userOpt.get().getUserId()), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @GetMapping(value = "/username{userid}")
    public ResponseEntity<String> getUsernameById(@RequestParam("userid") int userId) {
        String username = userService.getUsernameById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with userId '" + userId + "' not found."));
        return ResponseEntity.ok(username);
    }


}

