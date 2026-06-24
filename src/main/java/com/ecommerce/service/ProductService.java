package com.ecommerce.service;

import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    @Cacheable(value = "products")
    public List<Product> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll();
    }

    @Cacheable(value = "product", key = "#id")
    public Optional<Product> getProductById(Integer id) {
        log.info("Fetching product with id: {}", id);
        return productRepository.findById(id);
    }

    @Cacheable(value = "categoryProducts", key = "#category")
    public List<Product> getProductsByCategory(String category) {
        log.info("Fetching products for category: {}", category);
        return productRepository.findByCategory(category);
    }

    public List<Product> searchProducts(String name) {
        log.info("Searching products with name: {}", name);
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Product> getAvailableProducts() {
        log.info("Fetching available products");
        return productRepository.findAvailableProducts();
    }

    public List<Product> getLowStockProducts(Integer threshold) {
        log.info("Fetching low stock products (threshold: {})", threshold);
        return productRepository.findLowStockProducts(threshold);
    }

    @Transactional
    @CacheEvict(value = {"products", "categoryProducts"}, allEntries = true)
    public Product saveProduct(Product product) {
        log.info("Saving product: {}", product.getName());
        if (product.getId() == null) {
            product.setCreatedAt(LocalDateTime.now());
        }
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    @Transactional
    @CacheEvict(value = {"products", "product", "categoryProducts"}, allEntries = true)
    public void deleteProduct(Integer id) {
        log.info("Deleting product: {}", id);
        productRepository.deleteById(id);
    }

    @Transactional
    public Product updateStock(Integer productId, Integer quantity) {
        log.info("Updating stock for product: {} with quantity: {}", productId, quantity);
        return productRepository.findById(productId)
                .map(product -> {
                    product.setStock(product.getStock() + quantity);
                    return productRepository.save(product);
                })
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
}