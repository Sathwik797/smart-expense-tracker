package com.sathwik.expensetracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping({"/", "/dashboard-ui"})
    public String dashboard() {
        return "dashboard";
    }

}
