package com.ola.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ola.luser.Circulo;
import com.ola.services.CirculoService;

import java.util.List;

@RestController
@RequestMapping("/api/circulo")
public class CirculoController {

    @Autowired
    private CirculoService circuloservice;

    @GetMapping
    public List<Circulo> getAll() {
        return circuloservice.findAll();
    }

    @PostMapping
    public Circulo create(@RequestBody Circulo entity) {
        return circuloservice.save(entity);
    }

    @GetMapping("/{id}")
    public Circulo getById(@PathVariable Long id) {
        return circuloservice.findById(id);
    }

    @PutMapping("/{id}")
    public Circulo update(@PathVariable Long id, @RequestBody Circulo entity) {
        return circuloservice.update(id, entity);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        circuloservice.delete(id);
    }
}