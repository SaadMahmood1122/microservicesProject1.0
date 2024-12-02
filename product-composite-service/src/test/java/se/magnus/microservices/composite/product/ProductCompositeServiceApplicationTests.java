package se.magnus.microservices.composite.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.microservices.api.composite.product.ProductAggregate;
import se.magnus.microservices.api.composite.product.RecommendationSummary;
import se.magnus.microservices.api.core.product.Product;
import se.magnus.microservices.api.core.recommendation.Recommendation;
import se.magnus.microservices.api.core.review.Review;
import se.magnus.microservices.api.exception.InvalidInputException;
import se.magnus.microservices.api.exception.NotFoundException;
import se.magnus.microservices.composite.product.service.ProductCompositeIntegration;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = RANDOM_PORT,classes = ProductCompositeServiceApplication.class)
class ProductCompositeServiceApplicationTests {
	private static final int PRODUCT_ID_OK = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 2;
	private static final int PRODUCT_ID_INVALID = 3;
	@Autowired
	private WebTestClient client;
   @MockitoBean
    private ProductCompositeIntegration productCompositeIntegration;
	@BeforeEach
	void setUp() {



		when(productCompositeIntegration.getProduct(PRODUCT_ID_OK))
				.thenReturn(new Product(PRODUCT_ID_OK, "name", 1, "mock-address"));
		when(productCompositeIntegration.getRecommendations(PRODUCT_ID_OK))
				.thenReturn(Collections.singletonList(new Recommendation(PRODUCT_ID_OK, 1,"author", 1, "content", "mock address")));
		when(productCompositeIntegration.getReviews(PRODUCT_ID_OK))
				.thenReturn(Collections.singletonList(new Review(PRODUCT_ID_OK, 1, "author",
						"subject", "content", "mock address")));

		when(productCompositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
				.thenThrow(new NotFoundException("NOT FOUND: "+PRODUCT_ID_NOT_FOUND));

		when(productCompositeIntegration.getProduct(PRODUCT_ID_INVALID))
				.thenThrow(new InvalidInputException("INVALID ID"));
	}
	@Test
	void getProductById(){

		client.get()
				.uri("/product-composite/"+PRODUCT_ID_OK)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
				.jsonPath("$.recommendations.length()").isEqualTo(1)
				.jsonPath("$.reviews.length()").isEqualTo(1);
				

	}
	@Test
	void getProductIdNotFound(){
		client.get()
				.uri("/product-composite/"+PRODUCT_ID_NOT_FOUND)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isNotFound()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.path").isEqualTo("/product-composite/"+PRODUCT_ID_NOT_FOUND)
				.jsonPath("$.message").isEqualTo("NOT FOUND: "+PRODUCT_ID_NOT_FOUND);

	}

	@Test
	void invalidProductId(){
		client.get()
				.uri("/product-composite/"+PRODUCT_ID_INVALID)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.path").isEqualTo("/product-composite/"+PRODUCT_ID_INVALID)
				.jsonPath("$.message").isEqualTo("INVALID ID");
	}

	@Test
	void contextLoads() {
	}

}
