//package
package com.ola.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ola.luser.Rectangulo;

@Repository
public interface RectanguloRepository extends JpaRepository<Rectangulo, Long> {
}
