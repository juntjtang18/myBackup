package com.myBackup.ui.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.myBackup.client.Utility;

@Controller
public class DashboardController {
    @Autowired
    private Utility utility;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        String clientID = utility.getMACAddress(); // Or however you retrieve the clientID
        model.addAttribute("clientID", clientID);
        return "dashboard";
    }
}
