package com.googleplacessdk;

import com.google.android.libraries.places.api.model.Place;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class GooglePlacesSdkConstants {
  static final Map<String, Place.Field> PLACE_FIELD_MAP = createPlaceFieldMap();

  private static Map<String, Place.Field> createPlaceFieldMap() {
    Map<String, Place.Field> fieldMap = new HashMap<>();
    fieldMap.put("name", Place.Field.NAME);
    fieldMap.put("placeID", Place.Field.ID);
    fieldMap.put("plusCode", Place.Field.PLUS_CODE);
    fieldMap.put("coordinate", Place.Field.LAT_LNG);
    fieldMap.put("openingHours", Place.Field.OPENING_HOURS);
    fieldMap.put("phoneNumber", Place.Field.PHONE_NUMBER);
    fieldMap.put("types", Place.Field.TYPES);
    fieldMap.put("priceLevel", Place.Field.PRICE_LEVEL);
    fieldMap.put("website", Place.Field.WEBSITE_URI);
    fieldMap.put("viewport", Place.Field.VIEWPORT);
    fieldMap.put("formattedAddress", Place.Field.ADDRESS);
    fieldMap.put("addressComponents", Place.Field.ADDRESS_COMPONENTS);
    fieldMap.put("rating", Place.Field.RATING);
    fieldMap.put("userRatingsTotal", Place.Field.USER_RATINGS_TOTAL);
    fieldMap.put("utcOffsetMinutes", Place.Field.UTC_OFFSET);
    fieldMap.put("businessStatus", Place.Field.BUSINESS_STATUS);
    fieldMap.put("iconImageURL", Place.Field.ICON_URL);
    fieldMap.put("takeout", Place.Field.TAKEOUT);
    fieldMap.put("delivery", Place.Field.DELIVERY);
    fieldMap.put("dineIn", Place.Field.DINE_IN);
    fieldMap.put("curbsidePickup", Place.Field.CURBSIDE_PICKUP);
    fieldMap.put("photos", Place.Field.PHOTO_METADATAS);
    fieldMap.put("reservable", Place.Field.RESERVABLE);
    fieldMap.put("servesBreakfast", Place.Field.SERVES_BREAKFAST);
    fieldMap.put("servesLunch", Place.Field.SERVES_LUNCH);
    fieldMap.put("servesDinner", Place.Field.SERVES_DINNER);
    fieldMap.put("servesBeer", Place.Field.SERVES_BEER);
    fieldMap.put("servesWine", Place.Field.SERVES_WINE);
    fieldMap.put("servesBrunch", Place.Field.SERVES_BRUNCH);
    fieldMap.put("servesVegetarianFood", Place.Field.SERVES_VEGETARIAN_FOOD);
    fieldMap.put("wheelchairAccessibleEntrance", Place.Field.WHEELCHAIR_ACCESSIBLE_ENTRANCE);
    fieldMap.put("reviews", Place.Field.REVIEWS);
    fieldMap.put("currentOpeningHours", Place.Field.CURRENT_OPENING_HOURS);
    fieldMap.put("secondaryOpeningHours", Place.Field.SECONDARY_OPENING_HOURS);
    fieldMap.put("iconBackgroundColor", Place.Field.ICON_BACKGROUND_COLOR);
    
    return Collections.unmodifiableMap(fieldMap);
  }
}
