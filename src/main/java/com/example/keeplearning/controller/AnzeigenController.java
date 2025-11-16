package com.example.keeplearning.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/anzeigen")
public class AnzeigenController {
    /*@GetMapping
    public String anzeigenListe() {
        // Hier wird später die Liste geladen
        return "anzeigen_liste";
    }*/

    @GetMapping("/neu")
    public String anzeigenNeuForm() {
        return "anzeige_erstellen";
    }

    //wenn man auf eine anzeige clickt sieht man auf einer neuen seite alle details und nur die anzeige
    /*
    @GetMapping("/details")
    public String anzeigenDetails() {
        return "anzeige_details";
    }*/
}
