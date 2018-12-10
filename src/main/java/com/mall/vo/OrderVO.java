package com.mall.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter@Setter
public class OrderVO {
    private Long orderNo;

    private BigDecimal payment;

    private Integer paymentType;

    private String paymentTypeDesc;
    private Integer postage;

    private Integer status;


    private String statusDesc;

    private String paymentTime;

    private String sendTime;

    private String endTime;

    private String closeTime;

    private String createTime;

    //订单的明细
    private List<OrderItemVO> orderItemVoList;

    private String imageHost;
    private Integer shippingId;
    private String receiverName;

    private ShippingVO shippingVo;
}
