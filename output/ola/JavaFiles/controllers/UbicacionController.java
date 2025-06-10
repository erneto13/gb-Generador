package com.ola.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ola.luser.Ubicacion;
import com.ola.services.UbicacionService;

import java.util.List;

@RestController
@RequestMapping("/api/ubicacion")
public class UbicacionController {

    @Autowired
    private UbicacionService ubicacionservice;

    @GetMapping
    public List<Ubicacion> getAll() {
        return ubicacionservice.findAll();
    }

    @PostMapping
    public Ubicacion create(@RequestBody Ubicacion entity) {
        return ubicacionservice.save(entity);
    }

    @GetMapping("/{id}")
    public Ubicacion getById(@PathVariable Long id) {
        return ubicacionservice.findById(id);
    }

    @PutMapping("/{id}")
    public Ubicacion update(@PathVariable Long id, @RequestBody Ubicacion entity) {
        return ubicacionservice.update(id, entity);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        ubicacionservice.delete(id);
    }
}