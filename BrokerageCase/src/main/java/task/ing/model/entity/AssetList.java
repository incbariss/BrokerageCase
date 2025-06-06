package task.ing.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "assetlist")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String assetName;

    private String assetFullName;

    private BigDecimal currentPrice;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "assetList")
    private List<Asset> assets;

}
