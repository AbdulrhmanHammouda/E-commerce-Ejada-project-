package com.example.shopservice.config;

import com.example.shopservice.entity.Product;
import com.example.shopservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() == 0) {
            Product p1 = new Product();
            p1.setName("Slick formal sneaker shoe");
            p1.setDescription("Slick formal sneaker shoe.");
            p1.setPrice(new BigDecimal("2999.00"));
            p1.setCategory("Shoes");
            p1.setImageUrl("");
            p1.setTargetAudience("Man");
            p1.setBestSeller(true);
            p1.setNewArrival(true);

            Product p2 = new Product();
            p2.setName("Slick trendy sneaker shoe");
            p2.setDescription("Trending sneakers for women.");
            p2.setPrice(new BigDecimal("2799.00"));
            p2.setCategory("Shoes");
            p2.setImageUrl("");
            p2.setTargetAudience("Woman");
            p2.setMostPopular(true);
            p2.setNewArrival(true);

            Product p3 = new Product();
            p3.setName("Running sport shoe");
            p3.setDescription("Popular running shoes for boys.");
            p3.setPrice(new BigDecimal("3999.00"));
            p3.setCategory("Shoes");
            p3.setImageUrl("");
            p3.setTargetAudience("Boy");
            p3.setMostPopular(true);
            p3.setBestSeller(true);

            productRepository.saveAll(List.of(p1, p2, p3));
            System.out.println("Test shoe products successfully saved to database!");
        }
    }
}
