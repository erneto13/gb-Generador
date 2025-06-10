//package
package com.ola.services;

import com.ola.repository.RectanguloRepository;
import com.ola.luser.Rectangulo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RectanguloService {

    @Autowired
    private RectanguloRepository repository;

    public List<Rectangulo> findAll() {
        return repository.findAll();
    }

    public Rectangulo save(Rectangulo entity) {
        return repository.save(entity);
    }

    public Rectangulo findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
