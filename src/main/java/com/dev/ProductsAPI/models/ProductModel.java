package com.dev.ProductsAPI.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "\"tb products\"")
@AllArgsConstructor
@NoArgsConstructor
public class ProductModel extends RepresentationModel<ProductModel> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id_product")
    private UUID idProduct;

    private String name;
    private BigDecimal value;

    @Override
    public String toString() {
        return "ProductModel{" +
                "idProduct=" + idProduct +
                ", name='" + name + '\'' +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ProductModel that = (ProductModel) o;
        return Objects.equals(idProduct, that.idProduct) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), idProduct, name);
    }
}
