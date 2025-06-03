package ru.utalieva.victorina.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {
    
    @GetMapping({"/", "/quiz/**", "/quizzes/**", "/login", "/register", "/profile", "/rating", "/create-quiz", "/my-quizzes", "/friend-feed"})
    public String forwardToReactApp() {
        return "forward:/index.html";
    }
} 