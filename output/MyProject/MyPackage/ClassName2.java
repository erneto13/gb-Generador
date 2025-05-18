//package
package com.MyProject.MyPackage;

//imports
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ClassName2")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassName2 {
    private String field3;
    private int field4;
}