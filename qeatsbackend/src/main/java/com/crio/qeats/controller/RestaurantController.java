/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.controller;

import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.exchanges.GetRestaurantsRequest;
import com.crio.qeats.exchanges.GetRestaurantsResponse;
import com.crio.qeats.services.RestaurantService;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
import org.springframework.web.server.ResponseStatusException;

// TODO: CRIO_TASK_MODULE_RESTAURANTSAPI
// Implement Controller using Spring annotations.
// Remember, annotations have various "targets". They can be class level, method level or others.

@Log4j2
@RestController
@RequestMapping(RestaurantController.RESTAURANT_API_ENDPOINT)


public class RestaurantController {

  public static final String RESTAURANT_API_ENDPOINT = "/qeats/v1";
  public static final String RESTAURANTS_API = "/restaurants";
  public static final String MENU_API = "/menu";
  public static final String CART_API = "/cart";
  public static final String CART_ITEM_API = "/cart/item";
  public static final String CART_CLEAR_API = "/cart/clear";
  public static final String POST_ORDER_API = "/order";
  public static final String GET_ORDERS_API = "/orders";

  @Autowired
  private RestaurantService restaurantService;

  // @GetMapping(GREETING)
  // public String greetings() {
  // return
  // }

  @GetMapping(RESTAURANTS_API)
  public ResponseEntity<GetRestaurantsResponse> getRestaurants(GetRestaurantsRequest 
      getRestaurantsRequest,
      @RequestParam Double latitude, @RequestParam Double longitude,
      @RequestParam(value = "searchFor", required = false) String searchFor) {

    if ((latitude > 90 || latitude < -90) || (longitude < -180 || longitude > 180)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid co-ordinates");
    }

    try {
      getRestaurantsRequest = new GetRestaurantsRequest(latitude, longitude, searchFor);

      log.info("getRestaurants called with {}", getRestaurantsRequest);
      GetRestaurantsResponse getRestaurantsResponse;

      // CHECKSTYLE:OFF
      long startTimeInMillis = System.currentTimeMillis();
      getRestaurantsResponse = restaurantService.findAllRestaurantsCloseBy(getRestaurantsRequest, 
          LocalTime.now());
      long endTimeInMillis = System.currentTimeMillis();
      System.out.println("ServiceImpl Time Taken: " + (endTimeInMillis - startTimeInMillis));
      log.info("getRestaurants returned ", getRestaurantsResponse);
      // CHECKSTYLE:ON



      List<Restaurant> restaurant;
      restaurant = getRestaurantsResponse.getRestaurants();

      for (Restaurant r : restaurant) {
        if (!isEnglish(r.getName())) {
          // r.setName(r.getName().replaceAll("[[^a-zA-Z][\\D][\\S]]*", ""));
          r.setName(r.getName().replaceAll("[^\\p{ASCII}]", "e"));
        }
      }
      getRestaurantsResponse.setRestaurants(restaurant);

      return ResponseEntity.ok().body(getRestaurantsResponse);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "BadHttpRequest", e);
    }
  }

  // @PostMapping(RESTAURANTS_API)
  // public void getCoordinates(
  // @RequestParam Double latitude,
  // @RequestParam Double longitude,
  // @RequestParam Map<String, String> searchFor) {

  // GetRestaurantsRequest getRestaurantRequest = new GetRestaurantsRequest(
  // latitude, longitude, searchFor);
  // }

  // TIP(MODULE_MENUAPI): Model Implementation for getting menu given a
  // restaurantId.
  // Get the Menu for the given restaurantId
  // API URI: /qeats/v1/menu?restaurantId=11
  // Method: GET
  // Query Params: restaurantId
  // Success Output:
  // 1). If restaurantId is present return Menu
  // 2). Otherwise respond with BadHttpRequest.
  //
  // HTTP Code: 200
  // {
  // "menu": {
  // "items": [
  // {
  // "attributes": [
  // "South Indian"
  // ],
  // "id": "1",
  // "imageUrl": "www.google.com",
  // "itemId": "10",
  // "name": "Idly",
  // "price": 45
  // }
  // ],
  // "restaurantId": "11"
  // }
  // }
  // Error Response:
  // HTTP Code: 4xx, if client side error.
  // : 5xx, if server side error.
  // Eg:
  // curl -X GET "http://localhost:8081/qeats/v1/menu?restaurantId=11"

  // GetRestaurantsRequest getRestaurantsRequest) {

  // log.info("getRestaurants called with {}", getRestaurantsRequest);
  // GetRestaurantsResponse getRestaurantsResponse;

  // getRestaurantsResponse = restaurantService
  // .findAllRestaurantsCloseBy(getRestaurantsRequest, LocalTime.now());
  // log.info("getRestaurants returned {}", getRestaurantsResponse);

  // return ResponseEntity.ok().body(getRestaurantsResponse);
  // }

  private boolean isEnglish(String string) {

    String regex = "[[a-zA-Z][\\d][\\s]]*";
    if (Pattern.matches(regex, string)) {
      return true;
    }

    return false;
  }

}
