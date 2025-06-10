//package
package com.ola.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ola.luser.Circulo;

@Repository
public interface CirculoRepository extends JpaRepository<Circulo, Long> {
}
