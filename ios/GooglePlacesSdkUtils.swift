//
//  GooglePlacesSdkUtils.swift
//  GooglePlacesSdk
//
//  Created by Farid Ansari on 26/01/23.
//  Copyright © 2023 Facebook. All rights reserved.
//

import Foundation
import GooglePlaces

struct LocationBounds {
  let northEast: CLLocationCoordinate2D
  let southWest: CLLocationCoordinate2D
}

func ParseCoordinates(_ coordinates: NSDictionary) -> CLLocationCoordinate2D? {
  
  if (coordinates["latitude"] == nil || coordinates["longitude"] ==  nil) {
    return nil
  }
  
  let latitude = coordinates["latitude"] as! Double
  let longitude = coordinates["longitude"] as! Double
  
  return CLLocationCoordinate2DMake(latitude, longitude)
}

func ParseLocationBounds(_ location: NSDictionary) -> LocationBounds? {
  if (location["northEast"] == nil || location["southWest"] == nil) {
    return nil
  }

  let northEast = ParseCoordinates(location["northEast"] as! NSDictionary)
  let southWest = ParseCoordinates(location["southWest"] as! NSDictionary)
  
  if (northEast == nil || southWest == nil) {
    return nil
  }
  
  return LocationBounds(
    northEast: northEast!,
    southWest: southWest!
  )
}

func ParseLocationBias(_ locationBias: NSDictionary) -> GMSPlaceLocationBias? {
  guard let locationBounds = ParseLocationBounds(locationBias) else {
    return nil
  }
  
  return GMSPlaceRectangularLocationOption(
    locationBounds.northEast,
    locationBounds.southWest
  )
}

func ParseLocationRestriction(_ locationRestriction: NSDictionary) -> GMSPlaceLocationRestriction? {
  guard let locationBounds = ParseLocationBounds(locationRestriction) else {
    return nil
  }
  
  return GMSPlaceRectangularLocationOption(
    locationBounds.northEast,
    locationBounds.southWest
  )
}

func ParseOrigin(_ originCoordinates: NSDictionary) -> CLLocation? {
  guard let coordinates = ParseCoordinates(originCoordinates) else {
    return nil
  }
  
  return CLLocation(
    latitude: coordinates.latitude,
    longitude:  coordinates.longitude
  )
}

func AutocompleteFilterFromOptions(_ filterOptions: NSDictionary) -> GMSAutocompleteFilter {
  let filter = GMSAutocompleteFilter()
  if let types = filterOptions["types"] as? Array<String> {
    filter.types = types
  }
  if let countries = filterOptions["countries"] as? Array<String> {
    filter.countries = countries
  }
  
  if let locationBias = filterOptions["locationBias"] as? NSDictionary {
    let parsedLocationBias = ParseLocationBias(locationBias)
    if (parsedLocationBias != nil) {
      filter.locationBias = parsedLocationBias
    }
  }
  
  if let locationRestriction = filterOptions["locationRestriction"] as? NSDictionary {
    let parsedLocationRestriction = ParseLocationRestriction(locationRestriction)
    if (parsedLocationRestriction != nil) {
      filter.locationRestriction = parsedLocationRestriction
    }
  }
  
  if let origin = filterOptions["origin"] as? NSDictionary {
    let parsedOrigin = ParseOrigin(origin)
    if (parsedOrigin != nil) {
      filter.origin = parsedOrigin
    }
  }
  
  return filter
}

func GMSPlaceFieldsFromFields(fields: NSArray) -> GMSPlaceField {
  var parsedFields: GMSPlaceField = []
  for field in fields {
    if let parsedField = PLACE_FIELD_MAP[field] as? GMSPlaceField {
      parsedFields.insert(parsedField)
    }
  }
  
  if (parsedFields.isEmpty) {
    for field in PLACE_FIELD_MAP.allValues {
      if let parsedField = field as? GMSPlaceField {
        parsedFields.insert(parsedField)
      }
    }
  }
  
  return parsedFields
}

func ParseBooleanPlaceAttribute(val: GMSBooleanPlaceAttribute) -> String {
  if (val == GMSBooleanPlaceAttribute.true) {
    return "TRUE";
  }
  
  if (val == GMSBooleanPlaceAttribute.false) {
    return "FALSE";
  }
  
  return "UNKNOWN"
}

func ParseBusinessStatus(val: GMSPlacesBusinessStatus) -> String {
  if (val == GMSPlacesBusinessStatus.closedPermanently) {
    return "CLOSED_PERMANENTLY";
  }
  
  if (val == GMSPlacesBusinessStatus.closedTemporarily) {
    return "CLOSED_TEMPORARILY";
  }
  
  if (val == GMSPlacesBusinessStatus.operational) {
    return "OPERATIONAL";
  }
  
  return "UNKNOWN";
}

func ParseUrls(str: String) -> Array<String> {
  let detector = try! NSDataDetector(types: NSTextCheckingResult.CheckingType.link.rawValue)
  let matches = detector.matches(in: str, options: [], range: NSRange(location: 0, length: str.utf16.count))

  var urls: Array<String> = []
  for match in matches {
      guard let range = Range(match.range, in: str) else { continue }
      let url = str[range]
      urls.append(String(url))
  }
  
  return urls;
}

func ParsePhotoUrl(str: String) -> String {
  let urls = ParseUrls(str: str);
  if urls.isEmpty {
    return "";
  }
  
  return urls[0];
}

func ParseSuggesttions(suggestions: Array<GMSAutocompleteSuggestion>) -> NSMutableArray {
    let predictions: NSMutableArray = []
    for suggestion in suggestions {
        let dict: NSMutableDictionary = [
            "placeID": suggestion.placeSuggestion?.placeID,
            "description": suggestion.placeSuggestion?.attributedFullText.string,
            "primaryText": suggestion.placeSuggestion?.attributedPrimaryText.string,
            "secondaryText": suggestion.placeSuggestion?.attributedSecondaryText?.string ?? NSNull(),
            "types": suggestion.placeSuggestion?.types,
            "distanceMeters": suggestion.placeSuggestion?.distanceMeters?.intValue ?? NSNull()
        ]
        predictions.add(dict)
    }
    return predictions
}

func ParsePlace(place: GMSPlace) -> NSDictionary {
  let addressComponents = place.addressComponents?.compactMap{ [
    "types": $0.types,
    "name": $0.name,
    "shortName": $0.shortName ?? "",
  ]}
    
    let plusCode = place.plusCode.map{[
      "compoundCode": $0.compoundCode,
      "globalCode": $0.globalCode,
    ]}
    
    let photos = place.photos?.compactMap{[
      "attributions": [
        "url": ParsePhotoUrl(str: $0.attributions?.description ?? ""),
        "name": $0.attributions?.string ?? "",
      ],
      "width": $0.maxSize.width,
      "height": $0.maxSize.height,
      "reference": "",
    ]}

  var viewport: [String : Any]? = nil
  if let viewportInfo = place.viewportInfo{
     viewport = [
      "northEast": [
        "latitude": viewportInfo.northEast.latitude,
        "longitude": viewportInfo.northEast.longitude
      ],
      "southWest": [
        "latitude": viewportInfo.southWest.latitude,
        "longitude": viewportInfo.southWest.longitude
      ],
      "valid": viewportInfo.isValid,
    ]
  }
  
  let coordinate = [
      "latitude": place.coordinate.latitude,
      "longitude": place.coordinate.longitude
  ]
  
  
  var result = [String: Any]()
  if let name = place.name { result["name"] = name }
  if let placeID = place.placeID { result["placeID"] = placeID }
  if let plusCode = plusCode { result["plusCode"] = plusCode }
  
  if place.coordinate.latitude != -180.0 || place.coordinate.longitude != -180.0 {
      result["coordinate"] = coordinate
  }
  
  if let openingHours = place.openingHours?.weekdayText { result["openingHours"] = openingHours }
  if let phoneNumber = place.phoneNumber, !phoneNumber.isEmpty { result["phoneNumber"] = phoneNumber }
  if let types = place.types { result["types"] = types }
  
  if place.priceLevel != .unknown { result["priceLevel"] = place.priceLevel.rawValue }
  if let website = place.website?.absoluteString { result["website"] = website }
  if let viewport = viewport { result["viewport"] = viewport }
  if let formattedAddress = place.formattedAddress { result["formattedAddress"] = formattedAddress }
  if let addressComponents = addressComponents { result["addressComponents"] = addressComponents }
  
  if place.rating > 0 { result["rating"] = place.rating }
  if place.userRatingsTotal > 0 { result["userRatingsTotal"] = place.userRatingsTotal }
  if let utcOffset = place.utcOffsetMinutes { result["utcOffsetMinutes"] = utcOffset }
  
  let businessStatus = ParseBusinessStatus(val: place.businessStatus)
  if businessStatus != "UNKNOWN" { result["businessStatus"] = businessStatus }
  
  if let iconImageURL = place.iconImageURL?.absoluteString { result["iconImageURL"] = iconImageURL }
  
  let takeout = ParseBooleanPlaceAttribute(val: place.takeout)
  if takeout != "UNKNOWN" { result["takeout"] = takeout }
  
  let delivery = ParseBooleanPlaceAttribute(val: place.delivery)
  if delivery != "UNKNOWN" { result["delivery"] = delivery }
  
  let dineIn = ParseBooleanPlaceAttribute(val: place.dineIn)
  if dineIn != "UNKNOWN" { result["dineIn"] = dineIn }
  
  let curbsidePickup = ParseBooleanPlaceAttribute(val: place.curbsidePickup)
  if curbsidePickup != "UNKNOWN" { result["curbsidePickup"] = curbsidePickup }
  
  if let attributions = place.attributions?.string { result["attributions"] = attributions }
  if let photos = photos { result["photos"] = photos }
  
  return result as NSDictionary
}
