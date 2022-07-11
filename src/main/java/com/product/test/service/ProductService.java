package com.product.test.service;

import java.util.List;
import com.product.test.model.Product;

public interface ProductService {
	public List<Product> products();
	public Product getById(int id);
	public List<Product> containsName(String name);
	public Product addProduct(Product product);
	public Product updateProduct(int id, Product product);
	public Product deleteProduct(int id);
}

