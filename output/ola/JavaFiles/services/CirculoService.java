//package
package com.ola.services;

import com.ola.repository.CirculoRepository;
import com.ola.luser.Circulo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CirculoService {

    @Autowired
    private CirculoRepository repository;

    public List<Circulo> findAll() {
        return repository.findAll();
    }

    public Circulo save(Circulo entity) {
        return repository.save(entity);
    }

    public Circulo findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
