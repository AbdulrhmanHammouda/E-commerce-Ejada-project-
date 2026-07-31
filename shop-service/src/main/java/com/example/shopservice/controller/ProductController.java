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
import com.example.shopservice.mapper.DtoMapper;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.List;
import java.math.BigDecimal;
import org.springframework.web.multipart.MultipartFile;
import com.example.shopservice.client.InventoryClient;

@RestController
@RequestMapping("/api/shop/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryClient inventoryClient;

    // GET /api/shop/products (with optional UI filters and pagination)
    @GetMapping
    public ResponseEntity<ApiResponse> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String audience,
            @RequestParam(required = false) Boolean isBestSeller,
            @RequestParam(required = false) Boolean isPopular,
            @RequestParam(required = false) Boolean isNew,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
            
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Product> productPage = productService.getProducts(search, audience, isBestSeller, isPopular, isNew, pageable);
        
        List<Product> products = productPage.getContent();
        List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
        
        Map<Long, Integer> stockMap = Map.of();
        try {
            if (!productIds.isEmpty()) {
                stockMap = inventoryClient.getBulkStock(productIds);
            }
        } catch (Exception e) {
            System.err.println("Could not fetch bulk stock: " + e.getMessage());
        }

        final Map<Long, Integer> finalStockMap = stockMap;
        List<ProductResponse> responseList = products.stream()
                .map(product -> DtoMapper.mapToProductResponse(product, finalStockMap.get(product.getId())))
                .collect(Collectors.toList());
                
        com.example.shopservice.dto.response.PaginatedResponse<ProductResponse> paginatedResponse = 
            new com.example.shopservice.dto.response.PaginatedResponse<>(
                responseList,
                productPage.getNumber(),
                productPage.getTotalPages(),
                productPage.getTotalElements()
            );
            
        return ResponseEntity.ok(new ApiResponse(true, "Products retrieved successfully", paginatedResponse));
    }

    // GET /api/shop/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(product -> {
                    Integer stock = null;
                    try {
                        Map<Long, Integer> stockMap = inventoryClient.getBulkStock(List.of(id));
                        stock = stockMap.get(id);
                    } catch (Exception e) {
                        System.err.println("Could not fetch stock: " + e.getMessage());
                    }
                    return ResponseEntity.ok(new ApiResponse(true, "Product retrieved successfully", DtoMapper.mapToProductResponse(product, stock)));
                })
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
            @RequestParam(value = "initialQuantity", defaultValue = "0") int initialQuantity,
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

        try {
            Product savedProduct = productService.createProduct(product, image, initialQuantity);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Product created successfully", DtoMapper.mapToProductResponse(savedProduct, initialQuantity)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, e.getMessage(), null));
        }
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
        
        Integer stock = null;
        try {
            Map<Long, Integer> stockMap = inventoryClient.getBulkStock(List.of(id));
            stock = stockMap.get(id);
        } catch (Exception e) {
            System.err.println("Could not fetch stock: " + e.getMessage());
        }

        return ResponseEntity.ok(new ApiResponse(true, "Product updated successfully", DtoMapper.mapToProductResponse(updatedProduct, stock)));
    }

    // POST /api/shop/products/{id}/stock
    @PostMapping("/{id}/stock")
    public ResponseEntity<ApiResponse> addStock(
            @PathVariable Long id,
            @RequestParam("quantity") int quantity,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        
        if (role == null || !"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, "Forbidden", null));
        }

        try {
            productService.addStock(id, quantity);
            return ResponseEntity.ok(new ApiResponse(true, "Stock added successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}
