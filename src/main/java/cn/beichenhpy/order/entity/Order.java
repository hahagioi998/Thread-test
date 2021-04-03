package cn.beichenhpy.order.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class Order implements Serializable {
    private String id;
    private String name;
    private BigDecimal price;
}
