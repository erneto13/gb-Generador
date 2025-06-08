package com.generador.Generador.service;

import com.generador.Generador.entity.Project;
import com.generador.Generador.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public Optional<Project> findById(Integer id) {
        return projectRepository.findById(id);
    }

    public Project save(Project project) {
        return projectRepository.save(project);
    }

    // TODO: el update pa luego, nomas rompe los huevos por los dto

    public void deleteById(Integer id) {
        projectRepository.deleteById(id);
    }
}
