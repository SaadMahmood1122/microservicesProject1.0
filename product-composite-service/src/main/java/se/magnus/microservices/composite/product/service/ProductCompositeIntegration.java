package se.magnus.microservices.composite.product.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import se.magnus.microservices.api.core.product.Product;
import se.magnus.microservices.api.core.product.ProductService;
import se.magnus.microservices.api.core.recommendation.Recommendation;
import se.magnus.microservices.api.core.recommendation.RecommendationService;
import se.magnus.microservices.api.core.review.Review;
import se.magnus.microservices.api.core.review.ReviewService;
import se.magnus.microservices.api.exception.InvalidInputException;
import se.magnus.microservices.api.exception.NotFoundException;

import java.net.URI;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;


    String productServiceHost;

    int productServicePort;


    String  recommendationServiceHost;

    int  recommendationServicePort;


    String reviewServiceHost;

    int  reviewServicePort;
   @Autowired
    public ProductCompositeIntegration(RestTemplate restTemplate,
                                       ObjectMapper objectMapper,

                                       @Value("${app.product-service.host}") String productServiceHost,
                                       @Value("${app.product-service.port}") int productServicePort,
                                       @Value("${app.recommendation-service.host}") String recommendationServiceHost,
                                       @Value("${app.recommendation-service.port}") int recommendationServicePort,
                                       @Value("${app.review-service.host}") String reviewServiceHost,
                                       @Value("${app.review-service.port}") int reviewServicePort) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.productServiceHost = productServiceHost;
        this.productServicePort = productServicePort;
        this.recommendationServiceHost = recommendationServiceHost;
        this.recommendationServicePort = recommendationServicePort;
        this.reviewServiceHost = reviewServiceHost;
        this.reviewServicePort = reviewServicePort;

            this.productServiceUrl = "http://" +this.productServiceHost + ":" + this.productServicePort + "/product";
       this.recommendationServiceUrl = "http://" + this.recommendationServiceHost + ":" + this.recommendationServicePort+"/recommendation";
       this.reviewServiceUrl = "http://" + this.reviewServiceHost + ":" + this.reviewServicePort+"/review";
    }



    @Override
    public Product getProduct(int productId) {
        Product product=null;
        try {

            String url = productServiceUrl + "/" + productId;
            LOG.debug("Will call getProduct API on URL: {}", url);
           product  = restTemplate.getForObject(url, Product.class);
            LOG.debug("Found product with id: {}",product.getProductId());
            LOG.debug("product : {}",product);

        }catch (HttpClientErrorException e) {

            switch (HttpStatus.resolve(e.getStatusCode().value())){
                case NOT_FOUND:
                    LOG.atError().log("Product with id: {} not found", productId);
                    throw new NotFoundException("Product with id " + productId + " not found");
                case UNPROCESSABLE_ENTITY:
                    LOG.atError().log("Product with id: {} unprocessable entity", productId);
                    throw new InvalidInputException("Product with id " + productId + " is unprocessable");
            }
        }


        return product;
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        List<Recommendation> recommendations=null;
        try{
            String url= UriComponentsBuilder.fromUriString(recommendationServiceUrl)
                    .queryParam("productId",productId)
                    .toUriString();

            LOG.debug("Will call getRecommendations API on URL: {}", url);
           recommendations = restTemplate.
                    exchange(url,GET,null,new ParameterizedTypeReference<List<Recommendation>>() {})
                    .getBody();
           LOG.debug("Found recommendations : {}", recommendations);
        } catch (HttpClientErrorException e) {
            switch (HttpStatus.resolve(e.getStatusCode().value())){
                    case NOT_FOUND:
                    LOG.atError().log("Recommendation with id: {} not found", productId);
                    throw new NotFoundException("Recommendation with id " + productId + " not found");
                    case UNPROCESSABLE_ENTITY:
                        LOG.atError().log("Recommendation with id: {} unprocessable entity", productId);
                        throw new InvalidInputException("Recommendation with id " + productId + " is unprocessable");
            }
        }


        return  recommendations;
    }

    @Override
    public List<Review> getReviews(int productId) {
        List<Review> reviews=null;
        try {
            String url = UriComponentsBuilder.fromUriString(reviewServiceUrl)
                    .queryParam("productId",productId)
                    .toUriString();
            //String url = reviewServiceUrl + "/" + productId;
            LOG.debug("Will call getReviews API on URL: {}", url);
            reviews= restTemplate.exchange(url,GET,null,new ParameterizedTypeReference<List<Review>>() {})
                    .getBody();
            LOG.debug("Found reviews : {}", reviews);
        }catch (HttpClientErrorException e) {
            switch (HttpStatus.resolve(e.getStatusCode().value())){
                case NOT_FOUND:
                    LOG.atError().log("Review with id: {} not found", productId);
                    throw new NotFoundException("Review with id " + productId + " not found");
                case UNPROCESSABLE_ENTITY:
                    LOG.atError().log("Review with id: {} unprocessable entity", productId);
                    throw new InvalidInputException("Review with id " + productId + " is unprocessable");
            }

        }


        return reviews;
    }
}
