package task.ing.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import task.ing.model.enums.OrderSide;
import task.ing.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "order")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String assetName;

    private double size;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private OrderSide orderSide;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @ManyToOne
    @JoinColumn(name = "asset_list_id")
    private AssetList assetList;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @CreatedDate
    @Column(updatable = false)
    private LocalDate createdDate;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedDate
    private LocalDate lastModifiedDate;

    @LastModifiedBy
    private String lastModifiedBy;

}
