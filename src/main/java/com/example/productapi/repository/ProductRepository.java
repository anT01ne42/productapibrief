/**
 * productapi / com.example.productapi.repository
 * toine
 * 🧚
 */
package com.example.productapi.repository;
import com.example.productapi.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
