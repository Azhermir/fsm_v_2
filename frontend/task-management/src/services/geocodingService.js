/**
 * Mock Geocoding Service
 * In production, this would integrate with Google Maps Places API or similar service.
 * For now, this is a simplified implementation that simulates geocoding.
 */

// Mock address database for demonstration
const MOCK_ADDRESSES = [
  {
    address: '123 Main St, New York, NY',
    lat: 40.7128,
    lng: -74.0060,
  },
  {
    address: '456 Oak Ave, Los Angeles, CA',
    lat: 34.0522,
    lng: -118.2437,
  },
  {
    address: '789 Pine Rd, Chicago, IL',
    lat: 41.8781,
    lng: -87.6298,
  },
  {
    address: '321 Elm St, Houston, TX',
    lat: 29.7604,
    lng: -95.3698,
  },
  {
    address: '555 Maple Dr, Phoenix, AZ',
    lat: 33.4484,
    lng: -112.0740,
  },
];

/**
 * Search for address suggestions based on input
 * @param {string} input - The address search query
 * @returns {Promise<Array>} - Array of matching addresses
 */
export const searchAddresses = async (input) => {
  // Simulate API delay
  await new Promise((resolve) => setTimeout(resolve, 200));

  if (!input || input.trim().length < 3) {
    return [];
  }

  const query = input.toLowerCase();
  return MOCK_ADDRESSES.filter((item) =>
    item.address.toLowerCase().includes(query)
  ).map((item) => ({
    description: item.address,
    placeId: item.address, // Using address as ID for mock
  }));
};

/**
 * Geocode an address to get coordinates
 * @param {string} address - The address to geocode
 * @returns {Promise<Object>} - Object with lat and lng coordinates
 */
export const geocodeAddress = async (address) => {
  // Simulate API delay
  await new Promise((resolve) => setTimeout(resolve, 300));

  const match = MOCK_ADDRESSES.find(
    (item) => item.address.toLowerCase() === address.toLowerCase()
  );

  if (match) {
    return {
      lat: match.lat,
      lng: match.lng,
      success: true,
    };
  }

  // If address not in mock database, generate random coordinates
  // In production, this would throw an error for invalid addresses
  return {
    lat: 40.0 + Math.random() * 10,
    lng: -100.0 - Math.random() * 20,
    success: true,
  };
};

/**
 * Validate if an address can be geocoded
 * @param {string} address - The address to validate
 * @returns {Promise<boolean>} - True if address is valid
 */
export const validateAddress = async (address) => {
  if (!address || address.trim().length < 5) {
    return false;
  }

  try {
    const result = await geocodeAddress(address);
    return result.success;
  } catch (error) {
    return false;
  }
};
