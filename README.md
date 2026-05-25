[![image (9)](https://github.com/user-attachments/assets/8e6e7b15-ee07-46fd-be01-1aee7393b7a7)](https://www.ycombinator.com/companies/landeed/jobs/1RGlF1W-founding-fullstack-engineer-react-react-native-any-backend-india)



# Google Places SDK for React Native

Google Places SDK for React Native. Places SDK allows you to build location aware apps that responds contextutally to the local businesses and other places near the user's device.

[![CI](https://github.com/Kroniac/react-native-google-places-sdk/actions/workflows/ci.yml/badge.svg)](https://github.com/Kroniac/react-native-google-places-sdk/actions/workflows/ci.yml)
[![Licence](https://img.shields.io/github/license/Kroniac/react-native-google-places-sdk)](https://opensource.org/licenses/MIT)

## Table of contents

- [Requirements](#requirements)
  - [Minimum Platform Version](#minimum-platform-version)
  - [Google Places API Key](#google-places-api-key)
- [Installation](#installation)
- [Usage](#usage)
  - [Initialize SDK](#initialize-sdk)
    - [Initialize SDK](#initialize-sdk)
  - [Fetch Predictions](#fetch-predictions)
    - [Sample Implementation](#sample-implementation)
  - [Fetch Place By ID](#fetch-place-by-id)
    - [Sample Implementation](#sample-implementation-1)
  - [Search By Text](#search-by-text)
    - [Sample Implementation](#sample-implementation-2)
  - [Search Nearby](#search-nearby)
    - [Sample Implementation](#sample-implementation-3)
- [Contributing](#contributing)
- [Licence](#license)

## Requirements

### Minimum Platform Version

- Android: 21
- iOS: 15

### Google Places API Key

- [Get Android API Key](https://developers.google.com/maps/documentation/places/android-sdk/get-api-key)
- [Get iOS API Key](https://developers.google.com/maps/documentation/places/ios-sdk/get-api-key)

## Installation

```sh
npm install react-native-google-places-sdk
#OR
yarn add react-native-google-places-sdk
```

## Usage

### Initialize SDK

#### initialize(apiKey: string): void

SDK needs to be initialize only once per App start before using any other functions. Preferably in the root file, e.g., App.tsx.

```ts
import GooglePlacesSDK from 'react-native-google-places-sdk';

const GOOGLE_PLACES_API_KEY = ""; // add your Places API key
GooglePlacesSDK.initialize(GOOGLE_PLACES_API_KEY);
```

### Fetch Predictions

#### fetchPredictions(query: string, filters?: PredictionFiltersParam): Promise<PlacePrediction[]>

#### PredictionFiltersParams

```ts
type PredictionFiltersParam = {
  types?: string[];
  countries?: string[];
  locationBias?: LocationBounds;
  locationRestriction?: LocationBounds;
  origin?: LatLng;
};
```

#### PlacePrediction

```ts
type PlacePrediction = {
  description: string;
  placeID: string;
  primaryText: string;
  secondaryText: string;
  types: string[];
  distanceMeters: number;
}
```

#### Sample Output

```json
{
  "description": "Mumbai, Maharashtra, India",
  "distanceMeters": null,
  "placeID": "ChIJwe1EZjDG5zsRaYxkjY_tpF0",
  "primaryText": "Mumbai",
  "secondaryText": "Maharashtra, India",
  "types": [
    "locality",
    "political",
    "geocode"
  ]
}
```

#### Sample Implementation

```ts
import GooglePlacesSDK, { PLACE_FIELDS } from 'react-native-google-places-sdk';

GooglePlacesSDK.fetchPredictions(
  "Mumbai", // query
  { countries: ["in", "us"] } // filters
)
  .then((predictions) => console.log(predictions));
  .catch((error) => console.log(error));

// ...
```

### Fetch Place By ID

#### fetchPlaceByID(placeID: string, fields?: FieldsParam): Promise\<Place\>

#### FieldsParam

- Allowed Fields: Refer PLACE_FIELDS in 'react-native-google-sdk'

- If no fields or empty array is passed, then all fields will be fetched for given the place ID.

```ts
// type
string[]

// Example
import { PLACE_FIELDS } from 'react-native-google-places-sdk';

const fields = [PLACE_FIELDS.NAME, PLACE_FIELDS.PLACE_ID, PLACE_FIELDS.ADDRESS_COMPONENTS]
```

#### Place

```ts
type Place = {
  name: string | null;
  placeID: string | null;
  plusCode: string | null;
  coordinate: LatLng | null;
  openingHours: string | null;
  phoneNumber: string | null;
  types: string[] | null;
  priceLevel: number | null;
  website: string | null;
  viewport: (LocationBounds & { valid: boolean }) | null;
  formattedAddress: string | null;
  addressComponents:
    | {
        types: string[];
        name: string;
        shortName: string;
      }[]
    | null;
  attributions: string | null;
  rating: number;
  userRatingsTotal: number;
  utcOffsetMinutes: number | null;
  iconImageURL: string | null;
  businessStatus: BusinessStatus;
  dineIn: AtmosphereCategoryStatus;
  takeout: AtmosphereCategoryStatus;
  delivery: AtmosphereCategoryStatus;
  curbsidePickup: AtmosphereCategoryStatus;
  photos: {
    attributions: {
      url: string;
      name: string;
    };
    reference: string;
    width: number;
    height: number;
  }[];
};
```

#### Sample Implementation

```ts
import GooglePlacesSDK, { PLACE_FIELDS } from 'react-native-google-places-sdk';

GooglePlacesSDK.fetchPlaceByID(
  placeID = "ChIJwe1EZjDG5zsRaYxkjY_tpF0",
  fields = [PLACE_FIELDS.NAME, PLACE_FIELDS.TYPES]
)
  .then((place) => console.log(place));
  .catch((error) => console.log(error));
// ...
```

### Search By Text

#### searchByText(query: string, filters?: PredictionFiltersParam): Promise\<PlacePrediction[]\>

Searches for places using a free-form text query (e.g. "Pizza in Mumbai") and returns
the matching places. Backed by the [Text Search](https://developers.google.com/maps/documentation/places/android-sdk/text-search)
(New) endpoint. A session token is created automatically if one is not already active.

> **Note:** This method requires the new Places API to be enabled for your API key.

#### PredictionFiltersParams

```ts
type PredictionFiltersParam = {
  types?: string[];
  countries?: string[];
  locationBias?: LocationBounds;
  locationRestriction?: LocationBounds;
  origin?: LatLng;
};
```

#### Sample Output

```json
[
  {
    "name": "Trishna",
    "placeID": "ChIJ-yRniTbO5zsRGFuFGRTTFCo",
    "formattedAddress": "7, Rope Walk Ln, Kala Ghoda, Fort, Mumbai, Maharashtra 400001, India",
    "location": { "lat": 18.927591, "lng": 72.832327 },
    "types": ["restaurant", "food", "point_of_interest", "establishment"]
  }
]
```

> **Note:** The result shape differs slightly between platforms. iOS returns
> `placeId`, `name`, `formattedAddress`, `types`, `url` and `location`, while Android
> additionally returns `description`, `phoneNumber`, `websiteUri`, `coordinate` and
> `addressComponents`. Read only the fields you need for cross-platform parity.

#### Sample Implementation

```ts
import GooglePlacesSDK from 'react-native-google-places-sdk';

GooglePlacesSDK.searchByText(
  'Pizza in Mumbai', // query
  { countries: ['in'] } // filters (optional)
)
  .then((places) => console.log(places))
  .catch((error) => console.log(error));

// ...
```

### Search Nearby

#### searchNearby(options: { latitude: number; longitude: number; radius: number }, includedTypes?: string[]): Promise\<PlacePrediction[]\>

Searches for places within a circular area defined by a center coordinate and a
`radius` (in meters). Backed by the [Nearby Search](https://developers.google.com/maps/documentation/places/android-sdk/nearby-search)
(New) endpoint. Optionally pass `includedTypes` to restrict results to specific
[place types](https://developers.google.com/maps/documentation/places/web-service/supported_types)
(e.g. `['restaurant', 'cafe']`).

> **Note:** This method requires the new Places API to be enabled for your API key.

#### Options

```ts
type SearchNearbyOptions = {
  latitude: number;
  longitude: number;
  radius: number; // in meters
};
```

#### Sample Output

```json
[
  {
    "name": "Leopold Cafe",
    "placeID": "ChIJ7Qk5gjDG5zsRR4yQ7l4nQ8c",
    "formattedAddress": "Colaba Causeway, Mumbai, Maharashtra 400001, India",
    "location": { "lat": 18.922474, "lng": 72.831993 },
    "types": ["cafe", "restaurant", "food", "point_of_interest", "establishment"]
  }
]
```

> **Note:** As with `searchByText`, the result shape differs slightly between iOS and
> Android. Read only the fields you need for cross-platform parity.

#### Sample Implementation

```ts
import GooglePlacesSDK from 'react-native-google-places-sdk';

GooglePlacesSDK.searchNearby(
  { latitude: 18.922474, longitude: 72.831993, radius: 1000 }, // options
  ['restaurant', 'cafe'] // includedTypes (optional)
)
  .then((places) => console.log(places))
  .catch((error) => console.log(error));

// ...
```

## Example Project


## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
