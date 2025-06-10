//package
package com.ola.services;

import com.ola.repository.FormaRepository;
import com.ola.luser.Forma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FormaService {

    @Autowired
    private FormaRepository repository;

    public List<Forma> findAll() {
        return repository.findAll();
    }

    public Forma save(Forma entity) {
        return repository.save(entity);
    }

    public Forma findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
