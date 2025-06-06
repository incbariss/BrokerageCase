package task.ing.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String surname;

    private String email;

    private String username;

    private String password;

    @OneToMany(mappedBy = "customer")
    private List<Asset> assets;

    @OneToMany(mappedBy = "customer")
    private List<Order> orders;

    @Column(nullable = false)
    private boolean isDeleted = false;


}
