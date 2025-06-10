package com.ola.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ola.luser.Forma;
import com.ola.services.FormaService;

import java.util.List;

@RestController
@RequestMapping("/api/forma")
public class FormaController {

    @Autowired
    private FormaService formaservice;

    @GetMapping
    public List<Forma> getAll() {
        return formaservice.findAll();
    }

    @PostMapping
    public Forma create(@RequestBody Forma entity) {
        return formaservice.save(entity);
    }

    @GetMapping("/{id}")
    public Forma getById(@PathVariable Long id) {
        return formaservice.findById(id);
    }

    @PutMapping("/{id}")
    public Forma update(@PathVariable Long id, @RequestBody Forma entity) {
        return formaservice.update(id, entity);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        formaservice.delete(id);
    }
}