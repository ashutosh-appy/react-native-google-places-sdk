package com.googleplacessdk;

import android.os.Parcel;
import android.util.Patterns;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.AddressComponents;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.model.LocationRestriction;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlusCode;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.Review;
import com.google.android.libraries.places.api.model.AuthorAttribution;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.SearchByTextRequest;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

class GooglePlacesSdkUtils {
  static LatLng ParseCoordinates(ReadableMap coordinates) {
    if (!coordinates.hasKey("latitude") || !coordinates.hasKey("longitude")) {
      return null;
    }

    return new LatLng(coordinates.getDouble("latitude"), coordinates.getDouble("longitude"));
  }

  static RectangularBounds ParseLocationBounds(ReadableMap bounds) {
    if (!bounds.hasKey("northEast") || !bounds.hasKey("southWest")) {
      return null;
    }

    LatLng northEast = ParseCoordinates(bounds.getMap("northEast"));
    LatLng southWest = ParseCoordinates(bounds.getMap("southWest"));

    if (northEast == null || southWest == null) return null;

    return RectangularBounds.newInstance(southWest, northEast);
  }

  static SearchByTextRequest buildSearchByTextRequest(String query, ReadableMap options,
                                                      AutocompleteSessionToken sessionToken) {

    // Specify the list of fields to return.
    final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
      Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.TYPES,
      Place.Field.WEBSITE_URI, Place.Field.ADDRESS_COMPONENTS
    );

    SearchByTextRequest.Builder builder = SearchByTextRequest.builder(query, placeFields).setMaxResultCount(10);

    return builder
      .build();
  }

  static SearchNearbyRequest buildSearchNearByRequest(ReadableMap options,
                                                      AutocompleteSessionToken sessionToken) {

    // Specify the list of fields to return.
    final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
      Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.TYPES,
       Place.Field.WEBSITE_URI, Place.Field.ADDRESS_COMPONENTS
     );

    LatLng center = new LatLng(options.getDouble("latitude"), options.getDouble("longitude"));
    CircularBounds circle = CircularBounds.newInstance(center, /* radius = */  options.getDouble("radius"));

    SearchNearbyRequest.Builder builder = SearchNearbyRequest.builder(circle, placeFields).setMaxResultCount(10);

    return builder
      .build();
  }

  static FindAutocompletePredictionsRequest buildPredictionRequest(String query, ReadableMap options,
                                                                   AutocompleteSessionToken sessionToken) {
    FindAutocompletePredictionsRequest.Builder builder = FindAutocompletePredictionsRequest.builder();
    if (options.hasKey("types")) {
      ArrayList types = options.getArray("types").toArrayList();
      builder.setTypesFilter(types);
    }

    if (options.hasKey("countries")) {
      ArrayList countries = options.getArray("countries").toArrayList();
      builder.setCountries(countries);
    }

    if (options.hasKey("locationBias")) {
      RectangularBounds bounds = ParseLocationBounds(options.getMap("locationBias"));
      if (bounds != null) builder.setLocationBias(bounds);
    }

    if (options.hasKey("locationRestriction")) {
      RectangularBounds bounds = ParseLocationBounds(options.getMap("locationRestriction"));
      if (bounds != null) builder.setLocationRestriction(bounds);
    }

    if (options.hasKey("origin")) {
      LatLng origin = ParseCoordinates(options.getMap("origin"));
      if (origin != null) builder.setOrigin(origin);
    }

    if (sessionToken != null) {
      builder.setSessionToken(sessionToken);
    }

    return builder
      .setQuery(query)
      .build();
  }

  static WritableArray ParsePlaceTypes(List<Place.Type> types) {
    WritableArray parsedTypes = Arguments.createArray();
    for (Place.Type placeType : types) {
      parsedTypes.pushString(placeType.toString().toLowerCase(Locale.ROOT));
    }

    return parsedTypes;
  }

  static WritableMap ParseAutocompletePrediction(AutocompletePrediction prediction) {
    WritableMap map = Arguments.createMap();

    map.putString("placeID", prediction.getPlaceId());
    map.putString("description", prediction.getFullText(null).toString());
    map.putString("primaryText", prediction.getPrimaryText(null).toString());
    map.putArray("types", ParsePlaceTypes(prediction.getPlaceTypes()));

    if (prediction.getDistanceMeters() != null) {
      map.putDouble("distanceMeters", prediction.getDistanceMeters());
    } else map.putNull("distanceMeters");

    if (prediction.getSecondaryText(null) != null) {
      map.putString("secondaryText", prediction.getSecondaryText(null).toString());
    } else map.putNull("secondaryText");

    return map;
  }

  static WritableMap ParseSearchByText(Place place) {
    WritableMap map = Arguments.createMap();


    map.putString("name", place.getName());
    map.putString("placeID", place.getId());
    map.putString("phoneNumber", place.getPhoneNumber());
    map.putString("formattedAddress", place.getAddress());
    map.putString("description", place.getAddress());
    map.putString("websiteUri", place.getWebsiteUri() != null ? place.getWebsiteUri().toString() : "");

    if (place.getLatLng() != null) {
      LatLng latLng = place.getLatLng();
      map.putMap("coordinate", ParseLatLngSearchText(latLng));
      map.putMap("location", ParseLatLngSearchText(latLng));
    }

    if (place.getTypes() != null) {
      map.putArray("types", ParsePlaceTypes(place.getTypes()));
    } else map.putNull("types");

    if (place.getAddressComponents() != null) {
      map.putArray("addressComponents", ParseAddressComponents(place.getAddressComponents()));
    }

    return map;
  }

  static WritableArray ParseAutocompletePredictions(List<AutocompletePrediction> predictions) {
    WritableArray parsedPredictions = Arguments.createArray();
    for (AutocompletePrediction prediction : predictions) {
      parsedPredictions.pushMap(ParseAutocompletePrediction(prediction));
    }

    return parsedPredictions;
  }

  static WritableArray ParseSearchByTexts(List<Place> places) {
    WritableArray parsesPlaces = Arguments.createArray();
    for (Place place : places) {
      parsesPlaces.pushMap(ParseSearchByText(place));
    }

    return parsesPlaces;
  }

  static List<Place.Field> ParsePlaceFields(ReadableArray fields) {
    ArrayList<Place.Field> placeFields = new ArrayList<>();
    for (int i = 0; i < fields.size(); i++) {
      String field = fields.getString(i);
      if (GooglePlacesSdkConstants.PLACE_FIELD_MAP.containsKey(field)) {
        placeFields.add(GooglePlacesSdkConstants.PLACE_FIELD_MAP.get(field));
      }
    }

    if (placeFields.size() == 0) {
      for (Place.Field field : GooglePlacesSdkConstants.PLACE_FIELD_MAP.values()) {
        placeFields.add(field);
      }
    }

    return placeFields;
  }

  static WritableMap ParseLatLng(LatLng latLng) {
    WritableMap map = Arguments.createMap();
    map.putDouble("latitude", latLng.latitude);
    map.putDouble("longitude", latLng.longitude);

    return map;
  }

  static WritableMap ParseLatLngSearchText(LatLng latLng) {
    WritableMap map = Arguments.createMap();
    map.putDouble("lat", latLng.latitude);
    map.putDouble("lng", latLng.longitude);

    return map;
  }


  static WritableArray ParseAddressComponents(AddressComponents addressComponents) {
    WritableArray components = Arguments.createArray();
    for (AddressComponent addressComponent : addressComponents.asList()) {
      WritableMap componentMap = Arguments.createMap();
      componentMap.putArray("types", Arguments.fromList(addressComponent.getTypes()));
      componentMap.putString("name", addressComponent.getName());
      componentMap.putString("shortName", addressComponent.getShortName());
      components.pushMap(componentMap);
    }

    return components;
  }

  static ArrayList<String> ParseUrls(String str) {
    Matcher webMatcher = Patterns.WEB_URL.matcher(str);
    ArrayList<String> hyperLinks = new ArrayList<>();

    while (webMatcher.find()) {
      String res = webMatcher.group();
      hyperLinks.add(res);
    }

    return hyperLinks;
  }

  static String ParsePhotoUrl(String str) {
    ArrayList<String> urls = ParseUrls(str);
    if (urls.size() == 0) return "";

    return urls.get(0);
  }

  static WritableMap ParsePhotoAttributions(String attributions) {
    String result = attributions.replaceAll("<[^>]*>", "");
    WritableMap map = Arguments.createMap();
    map.putString("url", ParsePhotoUrl(attributions));
    map.putString("name", result);

    return map;
  }

  static WritableArray ParsePhotos(List<PhotoMetadata> photos) {
    WritableArray components = Arguments.createArray();
    for (PhotoMetadata photo : photos) {
      WritableMap componentMap = Arguments.createMap();
      componentMap.putMap("attributions", ParsePhotoAttributions(photo.getAttributions()));
      componentMap.putDouble("width", photo.getWidth());
      componentMap.putDouble("height", photo.getHeight());
      componentMap.putString("data", photo.toString());
      componentMap.putString("reference", photo.zza());
      components.pushMap(componentMap);
    }

    return components;
  }

  static WritableMap ParsePlusCode(PlusCode plusCode) {
    WritableMap map = Arguments.createMap();
    map.putString("compoundCode", plusCode.getCompoundCode());
    map.putString("globalCode", plusCode.getGlobalCode());

    return map;
  }

  static WritableArray ParseReviews(List<Review> reviews) {
    WritableArray parsedReviews = Arguments.createArray();
    for (Review review : reviews) {
      WritableMap reviewMap = Arguments.createMap();
      if (review.getAuthorAttribution() != null) {
        WritableMap authorMap = Arguments.createMap();
        AuthorAttribution attribution = review.getAuthorAttribution();
        authorMap.putString("name", attribution.getName());
        authorMap.putString("uri", attribution.getUri());
        authorMap.putString("photoUri", attribution.getPhotoUri());
        reviewMap.putMap("authorAttribution", authorMap);
      }
      
      reviewMap.putDouble("rating", review.getRating());
      if (review.getText() != null) reviewMap.putString("text", review.getText());
      if (review.getPublishTime() != null) reviewMap.putString("publishTime", review.getPublishTime());
      if (review.getRelativePublishTimeDescription() != null) reviewMap.putString("relativePublishTimeDescription", review.getRelativePublishTimeDescription());
      
      parsedReviews.pushMap(reviewMap);
    }
    return parsedReviews;
  }

  static WritableMap ParsePlace(Place place) {
    WritableMap placeInfo = Arguments.createMap();

    if (place.getName() != null) placeInfo.putString("name", place.getName());
    if (place.getId() != null) placeInfo.putString("placeID", place.getId());
    if (place.getPhoneNumber() != null) placeInfo.putString("phoneNumber", place.getPhoneNumber());
    if (place.getAddress() != null) placeInfo.putString("formattedAddress", place.getAddress());
    if (place.getBusinessStatus() != null && !place.getBusinessStatus().toString().equals("UNKNOWN")) {
      placeInfo.putString("businessStatus", place.getBusinessStatus().toString());
    }
    if (place.getTakeout() != null && !place.getTakeout().toString().equals("UNKNOWN")) placeInfo.putString("takeout", place.getTakeout().toString());
    if (place.getDelivery() != null && !place.getDelivery().toString().equals("UNKNOWN")) placeInfo.putString("delivery", place.getDelivery().toString());
    if (place.getDineIn() != null && !place.getDineIn().toString().equals("UNKNOWN")) placeInfo.putString("dineIn", place.getDineIn().toString());
    if (place.getCurbsidePickup() != null && !place.getCurbsidePickup().toString().equals("UNKNOWN")) placeInfo.putString("curbsidePickup", place.getCurbsidePickup().toString());

    if (place.getReservable() != null && !place.getReservable().toString().equals("UNKNOWN")) placeInfo.putString("reservable", place.getReservable().toString());
    if (place.getServesBreakfast() != null && !place.getServesBreakfast().toString().equals("UNKNOWN")) placeInfo.putString("servesBreakfast", place.getServesBreakfast().toString());
    if (place.getServesLunch() != null && !place.getServesLunch().toString().equals("UNKNOWN")) placeInfo.putString("servesLunch", place.getServesLunch().toString());
    if (place.getServesDinner() != null && !place.getServesDinner().toString().equals("UNKNOWN")) placeInfo.putString("servesDinner", place.getServesDinner().toString());
    if (place.getServesBeer() != null && !place.getServesBeer().toString().equals("UNKNOWN")) placeInfo.putString("servesBeer", place.getServesBeer().toString());
    if (place.getServesWine() != null && !place.getServesWine().toString().equals("UNKNOWN")) placeInfo.putString("servesWine", place.getServesWine().toString());
    if (place.getServesBrunch() != null && !place.getServesBrunch().toString().equals("UNKNOWN")) placeInfo.putString("servesBrunch", place.getServesBrunch().toString());
    if (place.getServesVegetarianFood() != null && !place.getServesVegetarianFood().toString().equals("UNKNOWN")) placeInfo.putString("servesVegetarianFood", place.getServesVegetarianFood().toString());
    if (place.getWheelchairAccessibleEntrance() != null && !place.getWheelchairAccessibleEntrance().toString().equals("UNKNOWN")) placeInfo.putString("wheelchairAccessibleEntrance", place.getWheelchairAccessibleEntrance().toString());

    if (place.getReviews() != null) {
      placeInfo.putArray("reviews", ParseReviews(place.getReviews()));
    }

    if (place.getCurrentOpeningHours() != null && place.getCurrentOpeningHours().getWeekdayText() != null) {
      placeInfo.putString("currentOpeningHours", place.getCurrentOpeningHours().getWeekdayText().toString());
    }

    if (place.getSecondaryOpeningHours() != null) {
      placeInfo.putString("secondaryOpeningHours", place.getSecondaryOpeningHours().toString());
    }

    if (place.getIconBackgroundColor() != null) {
      placeInfo.putString("iconBackgroundColor", String.format("#%06X", (0xFFFFFF & place.getIconBackgroundColor())));
    }

    if (place.getPhotoMetadatas() != null) {
      placeInfo.putArray("photos", ParsePhotos(place.getPhotoMetadatas()));
    }

    if (place.getAttributions() != null) {
      placeInfo.putString("attributions", place.getAttributions().toString());
    }

    if (place.getPlusCode() != null) {
      placeInfo.putMap("plusCode", ParsePlusCode(place.getPlusCode()));
    }

    if (place.getWebsiteUri() != null) {
      placeInfo.putString("website", place.getWebsiteUri().toString());
    }

    if (place.getRating() != null) {
      placeInfo.putDouble("rating", place.getRating());
    }

    if (place.getUserRatingsTotal() != null) {
      placeInfo.putInt("userRatingsTotal", place.getUserRatingsTotal());
    }

    if (place.getPriceLevel() != null) {
      placeInfo.putInt("priceLevel", place.getPriceLevel());
    }

    if (place.getOpeningHours() != null && place.getOpeningHours().getWeekdayText() != null) {
      placeInfo.putString("openingHours", place.getOpeningHours().getWeekdayText().toString());
    }

    if (place.getTypes() != null) {
      placeInfo.putArray("types", ParsePlaceTypes(place.getTypes()));
    }

    if (place.getAddressComponents() != null) {
      placeInfo.putArray("addressComponents", ParseAddressComponents(place.getAddressComponents()));
    }

    if (place.getLatLng() != null) {
      WritableMap coordinate = ParseLatLng(place.getLatLng());
      placeInfo.putMap("coordinate", coordinate);
    }

    LatLngBounds viewport = place.getViewport();
    if (viewport != null) {
      WritableMap viewportMap = Arguments.createMap();
      if (viewport.northeast != null) viewportMap.putMap("northEast", ParseLatLng(viewport.northeast));
      if (viewport.southwest != null) viewportMap.putMap("southWest", ParseLatLng(viewport.southwest));
      placeInfo.putMap("viewport", viewportMap);
    }

    return placeInfo;
  }
}
