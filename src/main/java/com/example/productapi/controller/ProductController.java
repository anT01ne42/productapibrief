/**
 * productapi / com.example.productapi.controller
 * toine
 * 🧚
 */
package com.example.productapi.controller;

import com.example.productapi.model.Product;
import com.example.productapi.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/products")
public class ProductController {
   private final ProductRepository repository;
   public ProductController(ProductRepository repository) {
	  this.repository = repository;
   }
   @GetMapping
   public List<Product> getAll() {
	  return repository.findAll();
   }
   @GetMapping("/{id}")
   public Product getById(@PathVariable Long id) {
	  return repository.findById(id)
			  .orElseThrow(() -> new ResponseStatusException(
					  HttpStatus.NOT_FOUND,
					  "Product with ID " + id + " not found"
			  ));
   }
   @PostMapping
   public Product create(@RequestBody Product product) {
	  return repository.save(product);
   }
   @PutMapping("/{id}")
   public Product update(@PathVariable Long id, @RequestBody Product product) {
	  Product existing = repository.findById(id).orElseThrow();
	  existing.setName(product.getName());
	  existing.setPrice(product.getPrice());
	  return repository.save(existing);
   }
   @DeleteMapping("/{id}")
   public void delete(@PathVariable Long id) {
	  repository.deleteById(id);
   }
   // Ajout méthode DUPLICATE
   @PostMapping("/{id}/duplicate")
   public Product duplicate(@PathVariable Long id) {
	  Product existing_product = repository.findById(id).orElseThrow();
	  Product copy = new Product();
	  copy.setName(existing_product.getName() + " copy");
	  copy.setPrice(existing_product.getPrice());
	  return create(copy);
   }

   //Pour le bundle (messages d'erreurs):
   @ResponseStatus(HttpStatus.BAD_REQUEST)
   @ExceptionHandler(IllegalStateException.class)
	  public String handleIllegalState(IllegalStateException ex) {
		 return ex.getMessage();
	  }

   //Ajout méthode BUNDLE et prévention des cycles
   @PostMapping("/bundle")
   public Product createBundle(@RequestBody Long[] productIds) {
	  if (verifyIds(productIds)) {
		 throw new IllegalStateException("Bundle with these products already exists!");
	  }

	  Product bundle = new Product();
	  StringBuilder new_name = new StringBuilder();
	  double total_price = 0;

	  List<Product> existing_products = new ArrayList<>();
	  for (Long id : productIds) {
		 existing_products.add(getById(id));
	  }
	  for (Product product : existing_products) {
		 new_name.append(product.getName()).append(" + ");
		 total_price += product.getPrice();
	  }
	  bundle.setName(new_name.toString().trim());
	  bundle.setPrice(total_price);
	  bundle.setSources(existing_products);
	  return create(bundle);
   }
   
   private boolean verifyIds(Long[] productIds) {
	  List<Product> allProducts = repository.findAll();

	  Set<Long> inputProductIds = new HashSet<>(Arrays.asList(productIds));

	  for (Product product : allProducts) {
		 if (product.getSources() != null && !product.getSources().isEmpty()) {
			for (Product source : product.getSources()) {
			   if (inputProductIds.contains(source.getId())) {
				  return true;
			   }
			}
		 }
	  }
	  return false;
}
}