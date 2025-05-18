package com.generador.Generador.Controllers;

import Util.ClassGenerator;
import org.json.JSONArray;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class MainController {
    @PostMapping("/test")
    public ResponseEntity<String> test(@RequestBody String payload) {
        ClassGenerator classGenerator = new ClassGenerator(new JSONArray(payload));
        classGenerator.generate();
        return ResponseEntity.ok(payload);
    }
}
