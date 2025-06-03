package ru.utalieva.victorina.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@SuppressWarnings("unused")
public class PageController {
    @GetMapping(value = {"/", "/{path:^(?!api).*$}/**"})
    public String forwardToReactApp() {
        return "forward:/index.html";
    }
} 