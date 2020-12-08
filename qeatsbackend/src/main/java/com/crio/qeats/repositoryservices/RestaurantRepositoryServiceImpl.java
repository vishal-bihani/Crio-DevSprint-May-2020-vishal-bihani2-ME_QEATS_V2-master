/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.queries.GeoHashBoundingBoxQuery;

import com.crio.qeats.configs.RedisConfiguration;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.globals.GlobalConstants;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoLocation;
import com.crio.qeats.utils.GeoUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Provider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

@Service
public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {

  @Autowired
  private RedisConfiguration redisConfiguration;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private Provider<ModelMapper> modelMapperProvider;

  @Autowired
  private RestaurantRepository restaurantRepository;

  private boolean isOpenNow(LocalTime time, RestaurantEntity res) {
    LocalTime openingTime = LocalTime.parse(res.getOpensAt());
    LocalTime closingTime = LocalTime.parse(res.getClosesAt());

    return time.isAfter(openingTime) && time.isBefore(closingTime);
  }

  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objectives:
  // 1. Implement findAllRestaurantsCloseby.
  // 2. Remember to keep the precision of GeoHash in mind while using it as a key.
  // Check RestaurantRepositoryService.java file for the interface contract.

  public List<Restaurant> findAllRestaurantsCloseByUsingMongo(Double latitude, Double longitude, 
      LocalTime currentTime,
      Double servingRadiusInKms) {

    List<Restaurant> restaurants = new ArrayList<>();
    ModelMapper modelMapper = modelMapperProvider.get();
    // RestaurantRepository restaurantRepository;
    // List<RestaurantEntity> restaurantEntities =
    // mongoTemplate.findAll(RestaurantEntity.class);
    List<RestaurantEntity> restaurantEntities = restaurantRepository.findAll();

    // int count = 0;
    for (RestaurantEntity r : restaurantEntities) {
      // if (count == 50) {
      // break;
      // }
      if (isRestaurantCloseByAndOpen(r, currentTime, latitude, longitude, servingRadiusInKms)) {
        // r.setName(Normalizer.normalize(r.getName(), Form.NFD)
        // .replaceAll("\\p{InCombiningDiacriticalMarks}+", ""));

        // if (!isEnglish(r.getName())) {
        // // r.setName(r.getName().replaceAll("[[^a-zA-Z][\\D][\\S]]*", ""));
        // r.setName(r.getName().replaceAll("[^\\p{ASCII}]", "e"));
        // }

        restaurants.add(modelMapper.map(r, Restaurant.class));
        // count++;
      }
    }
    return restaurants;
  }

  public List<Restaurant> findAllRestaurantsCloseBy(Double latitude, Double longitude, 
      LocalTime currentTime,
      Double servingRadiusInKms) {

    List<Restaurant> restaurants = null;
    // TODO: CRIO_TASK_MODULE_REDIS
    // We want to use cache to speed things up. Write methods that perform the same
    // functionality,
    // but using the cache if it is present and reachable.
    // Remember, you must ensure that if cache is not present, the queries are
    // directed at the
    // database instead.

    int numberOfCharacters = 7;
    String geoHash = GeoHash.geoHashStringWithCharacterPrecision(latitude, longitude, 
        numberOfCharacters);
    Jedis jedis = null;

    try {
      jedis = redisConfiguration.getJedisPool().getResource();
      ObjectMapper objectMapper = new ObjectMapper();
      if (jedis != null) {
        if (jedis.exists(geoHash)) {
          restaurants = objectMapper.readValue(jedis.get(geoHash), 
              new TypeReference<List<Restaurant>>() {
            });
          jedis.close();
          jedis.disconnect();
        } else {
          restaurants = findAllRestaurantsCloseByUsingMongo(latitude, longitude, currentTime, 
              servingRadiusInKms);
          jedis.set(geoHash, objectMapper.writeValueAsString(restaurants));
        }
      } else {
        restaurants = findAllRestaurantsCloseByUsingMongo(latitude, longitude, currentTime, 
            servingRadiusInKms);
      }
    } catch (JedisConnectionException j) {
      restaurants = findAllRestaurantsCloseByUsingMongo(latitude, longitude, currentTime, 
          servingRadiusInKms);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return restaurants;
  }

  // CHECKSTYLE:OFF
  // CHECKSTYLE:ON

  // private boolean isEnglish(String string) {

  // String regex = "[[a-zA-Z][\\d][\\s]]*";
  // if (Pattern.matches(regex, string)) {
  // return true;
  // }

  // return false;
  // }

  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objective:
  // 1. Check if a restaurant is nearby and open. If so, it is a candidate to be
  // returned.
  // NOTE: How far exactly is "nearby"?

  /**
   * Utility method to check if a restaurant is within the serving radius at a
   * given time.
   * 
   * @return boolean True if restaurant falls within serving radius and is open,
   *         false otherwise
   */
  private boolean isRestaurantCloseByAndOpen(RestaurantEntity restaurantEntity, 
      LocalTime currentTime, Double latitude,
      Double longitude, Double servingRadiusInKms) {
    if (isOpenNow(currentTime, restaurantEntity)) {
      return GeoUtils.findDistanceInKm(latitude, longitude, restaurantEntity.getLatitude(),
          restaurantEntity.getLongitude()) < servingRadiusInKms;
    }

    return false;
  }

}
