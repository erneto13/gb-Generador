//package
package com.ola.luser;

//imports
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ubicacion {
    private double x;
    private double y;
}