package task.ing.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import task.ing.model.enums.OrderSide;
import task.ing.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;

    private String assetName;

    private double size;

    private BigDecimal price;

    private LocalDateTime createDate;

    @Enumerated(EnumType.STRING)
    private OrderSide orderSide;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @ManyToOne
    @JoinColumn(name = "asset_list_id")
    private AssetList assetList;

    //Customer ileride
}
