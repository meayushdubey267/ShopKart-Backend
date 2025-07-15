package com.luv2code.ecommerce.service;

import com.luv2code.ecommerce.dao.CustomerRepository;
import com.luv2code.ecommerce.dto.PaymentInfo;
import com.luv2code.ecommerce.dto.Purchase;
import com.luv2code.ecommerce.dto.PurchaseResponse;
import com.luv2code.ecommerce.entity.Customer;
import com.luv2code.ecommerce.entity.Order;
import com.luv2code.ecommerce.entity.OrderItem;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CheckoutServiceImpl implements CheckoutService{

    private CustomerRepository customerRepository;

    public CheckoutServiceImpl (CustomerRepository customerRepository,
                                @Value("${stripe.key.secret}") String secretKey){
        this.customerRepository = customerRepository ;

//        initialize stripe API with the secret key
        Stripe.apiKey = secretKey;
    }

    @Override
    @Transactional
    public PurchaseResponse placeOrder(Purchase purchase) {

        //        Retrieve the order info from dto
            Order order = purchase.getOrder();

        //        generate tracking number
            String orderTrackingNumber = generateOrderTrackingNumber();
            order.setOrderTrackingNumber(orderTrackingNumber);

        //        populate order with orderItems
        Set<OrderItem> orderItems = purchase.getOrderItems();
        orderItems.forEach(item -> order.add(item));

        //        populate order with billing and shipping address
        order.setBillingAddress(purchase.getBillingAddress());
        order.setShippingAddress(purchase.getShippingAddress());

        //        populate customer with order
        Customer customer = purchase.getCustomer();

//        Check if this is an existing customer or not
        String theEmail = customer.getEmail();

        Customer customerFromDB = customerRepository.findByEmail(theEmail);

        if(customerFromDB!=null){
//            we  found the new  customer , now lets assign
            customer = customerFromDB;
        }

        customer.add(order);

        //        save to the database
        customerRepository.save(customer);

        //        return a response;

        return  new PurchaseResponse (orderTrackingNumber);
    }

    @Override
    public PaymentIntent createPaymentIntent(PaymentInfo paymentInfo) throws StripeException{

        List<String> paymentMethodTypes = new ArrayList<>();
        paymentMethodTypes.add("card");

        Map<String,Object> params = new HashMap<>();
        params.put("amount", paymentInfo.getAmount());
        params.put("currency", paymentInfo.getCurrency());
        params.put("payment_method_types" , paymentMethodTypes);
        params.put("description","ShopKart Purchase");
        params.put("receipt_email",paymentInfo.getReceiptEmail());

        return PaymentIntent.create(params);

    }

    private String generateOrderTrackingNumber() {

//        generate a random UUID number (Universally Unigue Identifier)

        return UUID.randomUUID().toString();
    }
}
