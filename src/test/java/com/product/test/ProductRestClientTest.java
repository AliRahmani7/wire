package com.product.test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.serviceUnavailable;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.product.test.exception.ProductException;
import com.product.test.model.Product;
import com.product.test.service.ProductRestClient;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "server.baseuri=http://localhost:8080" })
public class ProductRestClientTest
{
  @Autowired
  private ProductRestClient productRestClient;

  private List<Product> availableProducts = new ArrayList<>();

  private static int counter = 1;

  private static Product buildProduct(String name, String description) {
    Product product = new Product();
    product.setId(counter);
    product.setName(name);
    product.setDescription(description);

    counter++;

    return product;
  }

  @Before
  public void initializeProducts() {
    Product product1 = buildProduct("Laptop", "Ultra-light weight body with Ultra portability Laptop");
    Product product2 = buildProduct("Shoe", "Comfortable fabric lining and lightly-padded tongue for added support");
    Product product3 = buildProduct("iPhon", "The silky, soft-touch finish of the silicone exterior feels great in your hand");
    Product product4 = buildProduct("Pillow", "The poly fiber filling and the top quality materia");
    Product product5 = buildProduct("Fridge", "Energy saving and environmentally friendly");
    Product product6 = buildProduct("Desk", "Table with 2 open shelves ideal for study, bedroom, living room");

    availableProducts.addAll(Arrays.asList(product1, product2, product3, product4, product5, product6));

  }

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().port(8080).httpsPort(9999)
      .notifier(new ConsoleNotifier(true)).extensions(new ResponseTemplateTransformer(true)));

  @Test
  public void allProductForAnyURL() {

    wireMockRule.stubFor(get(anyUrl()).willReturn(aResponse().withStatus(200)
        .withHeader("Content-Type", "application/json").withBody(Json.write(availableProducts))));

    List<Product> resultFromService = productRestClient.products();

    assertEquals(resultFromService.size(), 6);

  }

  @Test
  public void allProductForEactURLPath() {

    wireMockRule.stubFor(get(urlPathEqualTo("/products")).willReturn(aResponse().withStatus(200)
        .withHeader("Content-Type", "application/json").withBody(Json.write(availableProducts))));

    List<Product> resultFromService = productRestClient.products();

    assertEquals(resultFromService.size(), 6);
    verify(exactly(1), getRequestedFor(urlPathEqualTo("/products")));
  }

  @Test
  public void getProductById() {

    Product product = new Product();
    product.setId(Integer.MAX_VALUE);
    product.setName("Monitor");
    product.setDescription("144Hz refresh rate");

    wireMockRule.stubFor(get(urlPathMatching("/products/[1-5]")).willReturn(
        aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(Json.write(product))));

    for (int i = 1; i < 6; i++) {

      Product resultProduct = productRestClient.getById(1);

      assertNotNull(resultProduct);
      assertEquals(Integer.MAX_VALUE, resultProduct.getId());
      assertEquals("Monitor", resultProduct.getName());
      assertEquals("144Hz refresh rate", resultProduct.getDescription());

    }
  }

  @Test
  public void allProductFromFile() {

    wireMockRule.stubFor(get(urlPathEqualTo("/products")).willReturn(aResponse().withStatus(200)
        .withHeader("Content-Type", "application/json").withBodyFile("allProducts.json")));

    List<Product> resultFromService = productRestClient.products();

    assertEquals(resultFromService.size(), 2);

    Product product = resultFromService.get(0);

    if (product.getId() == 1) {
      assertEquals("Monitor", product.getName());
      assertEquals("144Hz refresh rate", product.getDescription());
    } else if (product.getId() == 2) {
      assertEquals("TV", product.getName());
      assertEquals("Revolutionary TV processing technology", product.getDescription());
    }

    verify(exactly(1), getRequestedFor(urlPathEqualTo("/products")));
  }

  @Test
  public void getProductByIdUsingResponseTemplate() {

    wireMockRule.stubFor(get(urlPathMatching("/products/3")).willReturn(aResponse().withStatus(200)
        .withHeader("Content-Type", "application/json").withBodyFile("productByIdTemplate.json")));

    Product resultProduct = productRestClient.getById(3);

    assertNotNull(resultProduct);
    assertEquals(3, resultProduct.getId());
    assertEquals("Monitor", resultProduct.getName());
    assertEquals("144Hz refresh rate", resultProduct.getDescription());

    verify(exactly(1), getRequestedFor(urlPathMatching("/products/3")));

  }

  @Test(expected = ProductException.class)
  public void getProductByIdNotFound() {

    int productId = 123;

    wireMockRule.stubFor(get(urlPathMatching("/products/" + productId))
        .willReturn(aResponse().withStatus(404).withHeader("Content-Type", "application/json")));

    productRestClient.getById(123);

    verify(exactly(1), getRequestedFor(urlPathMatching("/products/" + productId)));

  }

  @Test(expected = ProductException.class)
  public void getProductByIdNotFoundReturnErrorPayloadFromFile() {

    int productId = 456;

    wireMockRule.stubFor(get(urlPathMatching("/products/" + productId)).willReturn(
        aResponse().withStatus(404).withHeader("Content-Type", "application/json").withBodyFile("404.json")));

    productRestClient.getById(456);

    verify(exactly(1), getRequestedFor(urlPathMatching("/products/" + productId)));

  }

 
  @Test(expected = ProductException.class)
  public void simulate_500() {

    Product product = new Product();
    product.setId(1);
    product.setName("Camera");
    product.setDescription("World’s first 61MP full-frame back");

    wireMockRule.stubFor(get(urlPathMatching("/products/1")).willReturn(serverError()));

    productRestClient.getById(1);

  }

  @Test(expected = ProductException.class)
  public void simulate_serverError_500() {

    Product product = new Product();
    product.setId(1);
    product.setName("Camera");
    product.setDescription("World’s first 61MP full-frame back");

    wireMockRule.stubFor(get(urlPathMatching("/products/1")).willReturn(serverError().withStatus(500)));

    productRestClient.getById(1);

  }

  @Test(expected = ProductException.class)
  public void simulate_serviceUnavailableError_503() {

    Product product = new Product();
    product.setId(1);
    product.setName("Camera");
    product.setDescription("World’s first 61MP full-frame back");

    wireMockRule.stubFor(get(urlPathMatching("/products/1")).willReturn(serviceUnavailable()));

    productRestClient.getById(1);

  }

  @Test(expected = ProductException.class)
  public void simulate_fault_randomDataThenClose() {

    Product product = new Product();
    product.setId(1);
    product.setName("Camera");
    product.setDescription("World’s first 61MP full-frame back");

    wireMockRule.stubFor(get(urlPathMatching("/products/1"))
        .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

    productRestClient.getById(1);

  }

  @Test(expected = ProductException.class)
  public void injectFixedDelay() {

    Product product = new Product();
    product.setId(1);
    product.setName("Camera");
    product.setDescription("World’s first 61MP full-frame back");

    wireMockRule.stubFor(get(urlPathMatching("/products/1")).willReturn(aResponse().withFixedDelay(9000)));

    productRestClient.getById(1);
  }

  @Test(expected = ProductException.class)
  public void injectUniformRandomDelay() {

    Product product = new Product();
    product.setId(1);
    product.setName("Camera");
    product.setDescription("World’s first 61MP full-frame back");

    wireMockRule.stubFor(
        get(urlPathMatching("/products/1")).willReturn(aResponse().withUniformRandomDelay(6000, 9000)));

    productRestClient.getById(1);
  }

}
