package com.ola.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ola.luser.Rectangulo;
import com.ola.services.RectanguloService;

import java.util.List;

@RestController
@RequestMapping("/api/rectangulo")
public class RectanguloController {

    @Autowired
    private RectanguloService rectanguloservice;

    @GetMapping
    public List<Rectangulo> getAll() {
        return rectanguloservice.findAll();
    }

    @PostMapping
    public Rectangulo create(@RequestBody Rectangulo entity) {
        return rectanguloservice.save(entity);
    }

    @GetMapping("/{id}")
    public Rectangulo getById(@PathVariable Long id) {
        return rectanguloservice.findById(id);
    }

    @PutMapping("/{id}")
    public Rectangulo update(@PathVariable Long id, @RequestBody Rectangulo entity) {
        return rectanguloservice.update(id, entity);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        rectanguloservice.delete(id);
    }
}