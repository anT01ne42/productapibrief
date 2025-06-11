/**
 * productapi / com.example.productapi
 * toine
 * 🧚
 */
package com.example.productapi;

import com.example.productapi.model.Product;
import com.example.productapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springdoc.core.service.GenericResponseService;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

   @Autowired
   private ProductRepository repository;
   @Autowired
   private MockMvc mockMvc;
   @Autowired
   private ObjectMapper objectMapper;
   @Autowired
   private GenericResponseService genericResponseService;

   final private String URL = "http://localhost:8080/";

   private Product testProduct;
   private Product testProduct2;

   @BeforeEach
   void setUp() {
	  testProduct = new Product();
	  testProduct.setName("Testing Product A");
	  testProduct.setPrice(5.0);

	  testProduct2 = new Product();
	  testProduct2.setName("Testing Product B");
	  testProduct2.setPrice(30.0);

	  repository.deleteAll();
   }

   //Test get all products (products/)
   @Test
   void getAllProducts() throws Exception{
	  Product p1 = repository.save(testProduct);
	  Product p2 = repository.save(testProduct2);

	  mockMvc.perform(get("/products"))
			  .andExpect(status().isOk())
			  .andExpect(content().contentType(MediaType.APPLICATION_JSON))
			  .andExpect(jsonPath("$", hasSize(2)))
			  .andExpect(jsonPath("$[0].name").value(p1.getName()))
			  .andExpect(jsonPath("$[0].price").value(p1.getPrice()))
			  .andExpect(jsonPath("$[1].name").value(p2.getName()))
			  .andExpect(jsonPath("$[1].price").value(p2.getPrice()));
   }

   //Test get 1 product by id (products/{id})
   @Test
   void getProductById() throws Exception {
	  Product p = repository.save(testProduct);

	  mockMvc.perform(get(URL + "products/" + p.getId()))
			  .andExpect(status().isOk())
			  .andExpect(jsonPath("$.name").value(containsString(p.getName())));
   }

   //Test get 1 product - id not found
   @Test
   void getProductByIdNotFound() throws Exception {
	  mockMvc.perform(get(URL + "products/999"))
			  .andExpect(status().isNotFound());
   }

   //Test create 1 product (products/)
   @Test
   void createProduct() throws Exception {
	  mockMvc.perform(post(URL + "products")
					  .contentType(MediaType.APPLICATION_JSON)
					  .content(objectMapper.writeValueAsString(testProduct)))
			  .andExpect(status().isOk())
			  .andExpect(jsonPath("$.id").exists())
			  .andExpect(jsonPath("$.name").value(containsString(testProduct.getName())));
   }

   //Test update a product (products/{id})
   @Test
   void updateProduct() throws Exception {
	  //Takes product A
	  Product p = repository.save(testProduct);

	  //Updates as product B
	  mockMvc.perform(put(URL + "products/" + p.getId())
					  .contentType(MediaType.APPLICATION_JSON)
					  .content(objectMapper.writeValueAsString(testProduct2)))
			  .andExpect(status().isOk())
			  .andExpect(jsonPath("$.id").value(p.getId()))
			  .andExpect(jsonPath("$.name").value(containsString(testProduct2.getName())));
   }

   //Test delete a product (products/{id})
   @Test
   void deleteProduct() throws Exception {
	  Product p = repository.save(testProduct);

	  mockMvc.perform(delete(URL + "products/" + p.getId()))
			  .andExpect(status().isOk());

	  //Once deleted, tests if it exists
	  mockMvc.perform(get(URL + "products/" + p.getId()))
			  .andExpect(status().isNotFound());
   }

   // Test if duplicating products works(products/)
   @Test
   void duplicateProduct() throws Exception {
	  Product p = repository.save(testProduct);

	  // Tests duplicates (does it have the same id and name)
	  mockMvc.perform(post(URL + "products/" + p.getId() + "/duplicate"))
			  .andExpect(status().isOk())
			  .andExpect(jsonPath("$.id").exists())
			  .andExpect(jsonPath("$.name").value(containsString(testProduct.getName() + " copy")));

	  // Tests if copy exists
	  mockMvc.perform(get(URL + "products"))
			  .andExpect(status().isOk())
			  .andExpect(jsonPath("$", hasSize(2)));
   }

   // Test bundle creation (products/bundle/)
   @Test
   void createBundle() throws Exception {
	  Product p1 = repository.save(testProduct);
	  Product p2 = repository.save(testProduct2);

	  // Tests after creating: exists, contains all names, added values
	  mockMvc.perform(post(URL + "products/bundle")
					  .contentType(MediaType.APPLICATION_JSON)
					  .content(objectMapper.writeValueAsString(new Long[]{p1.getId(), p2.getId()})))
			  .andExpect(status().isOk())
			  .andExpect(jsonPath("$.id").exists())
			  .andExpect(jsonPath("$.name").value(containsString(testProduct.getName() + " + " + testProduct2.getName())))
			  .andExpect(jsonPath("$.price").value(35.0))
			  .andExpect(jsonPath("$.sources", hasSize(2)))
			  .andExpect(jsonPath("$.sources[0].id").value(p1.getId()))
			  .andExpect(jsonPath("$.sources[1].id").value(p2.getId()));
   }

   // Test bundle creation with a product already in a bundle; uses `verifyIds`
   @Test
   void createBundleWithProductAlreadyInBundle() throws Exception {
	  Product savedProduct2 = repository.save(testProduct2);
	  repository.save(testProduct);

	  mockMvc.perform(post(URL + "products/bundle")
					  .contentType(MediaType.APPLICATION_JSON)
					  .content(objectMapper.writeValueAsString(new Long[]{savedProduct2.getId()})))
			  .andExpect(status().isOk());

	  // Try to create another bundle using the same product2 - should fail
	  mockMvc.perform(post(URL + "products/bundle")
					  .contentType(MediaType.APPLICATION_JSON)
					  .content(objectMapper.writeValueAsString(new Long[]{savedProduct2.getId()})))
			  .andExpect(status().isBadRequest())  // Changed from isInternalServerError
			  .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException().getMessage()
					  .contains("Bundle with these products already exists!")));
   }
}