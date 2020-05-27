package com.example.demo.controllers;


import com.example.demo.config.CountryContext;
import com.example.demo.domain.Order;
import com.example.demo.domain.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.sql.Date;

@AllArgsConstructor
@Controller
public class OrderController {
    private final OrderRepository orderRepository;



    @RequestMapping(path = "/orders", method= RequestMethod.POST)
    public ResponseEntity<?> createSampleOrder(@RequestHeader("X-TenantID") String tenantName) {
        CountryContext.setCurrentCountry(tenantName);

        Order newOrder = new Order(new Date(System.currentTimeMillis()));
        orderRepository.save(newOrder);

        return ResponseEntity.ok(newOrder);
    }
}
