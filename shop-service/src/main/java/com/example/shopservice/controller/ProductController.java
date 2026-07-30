package com.example.shopservice.controller;

import com.example.shopservice.entity.Product;
import com.example.shopservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.shopservice.dto.response.ProductResponse;
import com.example.shopservice.dto.response.ApiResponse;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import org.springframework.web.multipart.MultipartFile;
@RestController
@RequestMapping("/api/shop/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // GET /api/shop/products (with optional UI filters)
    @GetMapping
    public ResponseEntity<ApiResponse> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String audience,
            @RequestParam(required = false) Boolean isBestSeller,
            @RequestParam(required = false) Boolean isPopular,
            @RequestParam(required = false) Boolean isNew) {
            
        List<Product> products = productService.getProducts(search, audience, isBestSeller, isPopular, isNew);
        List<ProductResponse> responseList = products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse(true, "Products retrieved successfully", responseList));
    }

    // GET /api/shop/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(product -> ResponseEntity.ok(new ApiResponse(true, "Product retrieved successfully", mapToResponse(product))))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Product not found", null)));
    }

    // POST /api/shop/products 
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse> createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("category") String category,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        
        if (role == null) {
            System.out.println("DEBUG: X-User-Role header is MISSING!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "Unauthorized", null));
        }
        if (!"ADMIN".equals(role)) {
            System.out.println("DEBUG: Role is " + role + " - Kicking them out!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, "Forbidden", null));
        }

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);

        Product savedProduct = productService.createProduct(product, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Product created successfully", mapToResponse(savedProduct)));
    }

    // PUT /api/shop/products/{id}
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse> updateProduct(
            @PathVariable Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "price", required = false) BigDecimal price,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        
        if (role == null || !"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, "Forbidden", null));
        }

        Product productUpdates = new Product();
        if (name != null) productUpdates.setName(name);
        if (description != null) productUpdates.setDescription(description);
        if (price != null) productUpdates.setPrice(price);
        if (category != null) productUpdates.setCategory(category);

        Product updatedProduct = productService.updateProduct(id, productUpdates, image);
        return ResponseEntity.ok(new ApiResponse(true, "Product updated successfully", mapToResponse(updatedProduct)));
    }

    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getImageUrl(),
                product.getTargetAudience(),
                product.isBestSeller(),
                product.isMostPopular(),
                product.isNewArrival()
        );
    }
}
