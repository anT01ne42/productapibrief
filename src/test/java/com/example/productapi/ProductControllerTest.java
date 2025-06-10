/**
 * productapi / com.example.productapi
 * toine
 * 🧚
 */
package com.example.productapi;

import com.example.productapi.controller.ProductController;
import com.example.productapi.model.Product;
import com.example.productapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProductControllerTest {

   @Mock
   private ProductRepository repository;

   @InjectMocks
   private ProductController controller;

   private Product testProduct;
   private Product testProduct2;

   @BeforeEach
   void setUp() {
	  MockitoAnnotations.openMocks(this);

	  // Initialize test products
	  testProduct = new Product();
	  testProduct.setId(1L);
	  testProduct.setName("Testing Product A");
	  testProduct.setPrice(5.0);

	  testProduct2 = new Product();
	  testProduct2.setId(2L);
	  testProduct2.setName("Testing Product B");
	  testProduct2.setPrice(30.0);
   }

   @Test
   void getAllProducts() {
	  List<Product> products = Arrays.asList(testProduct, testProduct2);
	  when(repository.findAll()).thenReturn(products);

	  List<Product> result = controller.getAll();

	  assertEquals(2, result.size());
	  verify(repository).findAll();
   }

   @Test
   void getProductById() {
	  when(repository.findById(1L)).thenReturn(Optional.of(testProduct));

	  Product result = controller.getById(1L);

	  assertNotNull(result);
	  assertEquals("Testing Product A", result.getName());
	  verify(repository).findById(1L);
   }

   @Test
   void getProductByIdNotFound() {
	  when(repository.findById(999L)).thenReturn(Optional.empty());

	  assertThrows(Exception.class, () -> controller.getById(999L));
   }

   @Test
   void createProduct() {
	  when(repository.save(any(Product.class))).thenReturn(testProduct);

	  Product result = controller.create(testProduct);

	  assertNotNull(result);
	  assertEquals("Testing Product A", result.getName());
	  verify(repository).save(any(Product.class));
   }

   @Test
   void updateProduct() {
	  Product updatedProduct = new Product();
	  updatedProduct.setName("Updated Product");
	  updatedProduct.setPrice(15.0);

	  when(repository.findById(1L)).thenReturn(Optional.of(testProduct));
	  when(repository.save(any(Product.class))).thenReturn(updatedProduct);

	  Product result = controller.update(1L, updatedProduct);

	  assertEquals("Updated Product", result.getName());
	  assertEquals(15.0, result.getPrice());
	  verify(repository).save(any(Product.class));
   }

   @Test
   void deleteProduct() {
	  doNothing().when(repository).deleteById(1L);

	  controller.delete(1L);

	  verify(repository).deleteById(1L);
   }

   @Test
   void duplicateProduct() {
	  Product duplicatedProduct = new Product();
	  duplicatedProduct.setName("Testing Product A copy");
	  duplicatedProduct.setPrice(5.0);

	  when(repository.findById(1L)).thenReturn(Optional.of(testProduct));
	  when(repository.save(any(Product.class))).thenReturn(duplicatedProduct);

	  Product result = controller.duplicate(1L);

	  assertEquals("Testing Product A copy", result.getName());
	  assertEquals(5.0, result.getPrice());
	  verify(repository).save(any(Product.class));
   }

   @Test
   void createBundleSuccess() {
	  when(repository.findById(1L)).thenReturn(Optional.of(testProduct));
	  when(repository.findById(2L)).thenReturn(Optional.of(testProduct2));
	  when(repository.findAll()).thenReturn(Arrays.asList(testProduct, testProduct2));

	  Product expectedBundle = new Product();
	  expectedBundle.setName("Testing Product A + Testing Product B + ");
	  expectedBundle.setPrice(35.0);
	  expectedBundle.setSources(Arrays.asList(testProduct, testProduct2));

	  when(repository.save(any(Product.class))).thenReturn(expectedBundle);

	  Product result = controller.createBundle(new Long[]{1L, 2L});

	  assertNotNull(result);
	  assertEquals(35.0, result.getPrice());
	  assertEquals("Testing Product A + Testing Product B + ", result.getName());
   }
}