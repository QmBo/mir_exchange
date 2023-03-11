package ru.qmbo.mirexchange.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.qmbo.mirexchange.service.UserService;

@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/add")
    @ResponseBody
    public String addUser(@RequestParam String chatId) {
        return this.userService.subscribe(chatId);
    }

    @GetMapping("/dell")
    @ResponseBody
    public String dellUser(@RequestParam String chatId) {
        return this.userService.unsubscribe(chatId);
    }
}
