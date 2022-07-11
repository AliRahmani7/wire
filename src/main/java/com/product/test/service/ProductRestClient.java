package com.product.test.service;

import java.util.List;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import com.product.test.exception.ProductException;
import com.product.test.model.Product;
import com.product.test.service.ProductService;

public class ProductRestClient implements ProductService {

	private WebClient webClient;

	public ProductRestClient(WebClient webClient) {
		this.webClient = webClient;
	}

	@Override
	public List<Product> products() {
		return webClient.get().uri("/products").retrieve().bodyToFlux(Product.class).collectList().block();
	}

	@Override
	public Product getById(int id) {
		try {
			return webClient.get().uri("/products/" + id).retrieve().bodyToMono(Product.class).block();
		}catch(WebClientResponseException e) {
			throw new ProductException(e.getMessage() + "," + e.getRawStatusCode());
		}catch(Exception e) {
			throw new ProductException(e.getMessage());
		}	
	}

	@Override
	public List<Product> containsName(String name) {
		String uriToHit = UriComponentsBuilder.fromUriString("/products/by-name/").queryParam("productName", name)
				.buildAndExpand().toString();

		return webClient.get().uri(uriToHit).retrieve().bodyToFlux(Product.class).collectList().block();
	}

	@Override
	public Product addProduct(Product product) {
		return webClient.post().uri("/products").syncBody(product).retrieve().bodyToMono(Product.class).block();
	}

	@Override
	public Product updateProduct(int id, Product product) {
		return webClient.put().uri("/products/" + id).syncBody(product).retrieve().bodyToMono(Product.class)
				.block();
	}

	@Override
	public Product deleteProduct(int id) {
		return webClient.delete().uri("/products/" + id).retrieve().bodyToMono(Product.class).block();
	}
}

