package com.xiehn.reggie.dto;

import lombok.Data;
import java.util.List;
import com.xiehn.reggie.pojo.Orders;
import com.xiehn.reggie.pojo.OrderDetail;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
