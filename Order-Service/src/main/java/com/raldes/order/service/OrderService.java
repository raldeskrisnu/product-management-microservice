package com.raldes.order.service;

import com.raldes.order.dto.InventoryResponse;
import com.raldes.order.dto.OrderLineItemsDto;
import com.raldes.order.dto.OrderRequest;
import com.raldes.order.exception.NoProductFoundException;
import com.raldes.order.model.Order;
import com.raldes.order.model.OrderLineItems;
import com.raldes.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItemsList = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItemsList);

        List<String> skuCodeList =
                order.getOrderLineItemsList()
                        .stream()
                        .map(OrderLineItems::getSkuCode)
                        .toList();

        InventoryResponse[] result = webClientBuilder.build().get()
                .uri("http://intentory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodeList).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        if(result != null && result.length != 0) {
            boolean allProductsInStock = Arrays.stream(result)
                    .allMatch(InventoryResponse::isIsinStock);
            if(allProductsInStock) {
                orderRepository.save(order);
                return "Order Successfully Create";
            }
        }

        throw new IllegalArgumentException("Product is not in stock");
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
