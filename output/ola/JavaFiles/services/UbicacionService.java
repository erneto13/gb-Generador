//package
package com.ola.services;

import com.ola.repository.UbicacionRepository;
import com.ola.luser.Ubicacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UbicacionService {

    @Autowired
    private UbicacionRepository repository;

    public List<Ubicacion> findAll() {
        return repository.findAll();
    }

    public Ubicacion save(Ubicacion entity) {
        return repository.save(entity);
    }

    public Ubicacion findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
