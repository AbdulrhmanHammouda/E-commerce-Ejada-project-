package com.example.shopservice.controller;

import com.example.shopservice.entity.Product;
import com.example.shopservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.math.BigDecimal;
import org.springframework.web.multipart.MultipartFile;
import com.example.shopservice.service.ImageUploadService;
@RestController
@RequestMapping("/api/shop/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // GET /api/shop/products (with optional UI filters)
    @GetMapping
    public ResponseEntity<List<Product>> getProducts(
            @RequestParam(required = false) String audience,
            @RequestParam(required = false) Boolean isBestSeller,
            @RequestParam(required = false) Boolean isPopular,
            @RequestParam(required = false) Boolean isNew) {
            
        List<Product> products = productService.getProducts(audience, isBestSeller, isPopular, isNew);
        return ResponseEntity.ok(products);
    }

    // GET /api/shop/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Autowired
    private ImageUploadService imageUploadService;

    // POST /api/shop/products 
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Product> createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("category") String category,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        
        if (role == null) {
            System.out.println("DEBUG: X-User-Role header is MISSING!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!"ADMIN".equals(role)) {
            System.out.println("DEBUG: Role is " + role + " - Kicking them out!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);

        // Upload image to Cloudinary if provided
        if (image != null && !image.isEmpty()) {
            String imageUrl = imageUploadService.uploadImage(image);
            product.setImageUrl(imageUrl);
        }

        Product savedProduct = productService.createProduct(product);
        return ResponseEntity.ok(savedProduct);
    }
}
