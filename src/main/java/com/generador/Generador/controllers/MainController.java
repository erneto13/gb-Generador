package com.generador.Generador.controllers;

import util.ClassGenerator;
import org.json.JSONArray;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MainController {

    @PostMapping("/generate")
    public ResponseEntity<String> test(@RequestBody String payload) {
        ClassGenerator classGenerator = new ClassGenerator(new JSONArray(payload));
        classGenerator.generate();
        return ResponseEntity.ok(payload);
    }
}
