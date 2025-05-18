//package
package com.MyProject.MyPackage;

//imports
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Person")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Person {
        @Id
                @GeneratedValue(strategy = GenerationType.AUTO)
            private int id;
    private int field2;
    private String Name;
    private int Age;
}