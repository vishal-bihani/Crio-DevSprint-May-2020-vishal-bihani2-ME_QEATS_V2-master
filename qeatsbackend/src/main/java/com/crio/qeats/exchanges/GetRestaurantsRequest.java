/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.exchanges;

import java.util.Map;
import java.util.Optional;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

// TODO: CRIO_TASK_MODULE_RESTAURANTSAPI
//  Implement GetRestaurantsRequest.
//  Complete the class such that it is able to deserialize the incoming query params from
//  REST API clients.
//  For instance, if a REST client calls API
//  /qeats/v1/restaurants?latitude=28.4900591&longitude=77.536386&searchFor=tamil,
//  this class should be able to deserialize lat/long and optional searchFor from that.

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class GetRestaurantsRequest {

  @NonNull
  private Double latitude;
  @NonNull
  private Double longitude;

  @Nullable
  private String searchFor;

  public GetRestaurantsRequest(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  // public GetRestaurantsRequest(Double latitude, Double longitude, String searchFor) {

  // this.latitude = latitude;
  // this.longitude = longitude;
  // this.searchFor = searchFor;
  // }

  // public Double getLatitude() {
  // return this.latitude;
  // }

  // public Double getLongitude() {
  // return this.longitude;
  // }

}


