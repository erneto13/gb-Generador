//package
package com.ola.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ola.luser.Ubicacion;

@Repository
public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {
}
