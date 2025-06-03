package task.ing.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "assets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String assetName;

    private double size;

    private double usableSize;

    @ManyToOne
    @JoinColumn(name = "asset_list_id")
    private AssetList assetList;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
