# FSM Mobile App - Task Management

This is the React Native mobile application for the Field Service Management (FSM) system, designed for field technicians to view and manage their assigned tasks.

## Features

### Implemented
- ✅ Mobile authentication flow with technician ID and name
- ✅ Task list view with task cards displaying:
  - Task title
  - Priority (with color coding)
  - Client address
  - Estimated duration
  - Status badge
- ✅ Pull-to-refresh functionality
- ✅ Tasks sorted by priority (CRITICAL > HIGH > MEDIUM > LOW)
- ✅ Responsive design with proper empty and error states
- ✅ Offline support with cached data display
- ✅ Mock data fallback for development
- ✅ Push notifications for task assignments
- ✅ Deep linking to task details from notifications

### Planned
- 🔜 Task detail view
- 🔜 Status update functionality
- 🔜 Integration with backend API (depends on issue #116)
- 🔜 Real-time task updates

## Technology Stack

- **React Native**: 0.81.5
- **Expo**: ~54.0.25
- **React**: 19.1.0
- **React Navigation**: Stack Navigator for screen navigation
- **AsyncStorage**: Local storage for user data persistence
- **Expo Notifications**: Push notification support with FCM integration

## Project Structure

```
mobile/
├── src/
│   ├── components/
│   │   └── TaskCard.js          # Task card component
│   ├── screens/
│   │   ├── LoginScreen.js       # Authentication screen
│   │   └── TaskListScreen.js    # Task list view
│   ├── services/
│   │   ├── taskService.js       # API integration and utilities
│   │   └── notificationService.js # Push notification integration
│   ├── context/
│   │   └── AuthContext.js       # Authentication context
│   └── utils/                   # Utility functions
├── App.js                       # Main app component
├── app.json                     # Expo configuration
└── package.json                 # Dependencies
```

## Getting Started

### Prerequisites

- Node.js 20.x or higher
- npm 10.x or higher
- Expo CLI (installed automatically)
- iOS Simulator (macOS only) or Android Emulator
- Alternatively, use Expo Go app on a physical device

### Installation

```bash
# Navigate to mobile directory
cd mobile

# Install dependencies
npm install
```

### Running the App

#### Start Development Server

```bash
npm start
```

This will start the Expo development server and display a QR code.

#### Run on iOS (macOS only)

```bash
npm run ios
```

#### Run on Android

```bash
npm run android
```

#### Run on Web

```bash
npm run web
```

#### Using Expo Go App

1. Install Expo Go on your iOS or Android device
2. Scan the QR code displayed in the terminal
3. The app will load on your device

## Usage

### Login

1. Open the app
2. Enter your Technician ID (e.g., `tech-001`)
3. Enter your Name (e.g., `John Smith`)
4. Tap "Login"

**Demo Credentials:**
- Technician ID: `tech-001`
- Name: `John Smith`

### View Tasks

1. After login, you'll see the "My Tasks" screen
2. Tasks are displayed as cards with:
   - Priority badge (color-coded)
   - Task title
   - Client address with location icon
   - Estimated duration
   - Status badge
3. Tasks are sorted by priority (CRITICAL > HIGH > MEDIUM > LOW)

### Refresh Tasks

- Pull down the task list to refresh
- The app will fetch the latest tasks from the backend
- If the API is unavailable, mock data will be displayed

## API Integration

The app integrates with the backend API at `http://localhost:8080/api`.

### Endpoints Used

- **GET /api/technicians/:id/tasks** - Fetch tasks assigned to a technician
- **POST /api/device-tokens** - Register device token for push notifications
- **DELETE /api/device-tokens/:userId/:deviceId** - Unregister device token
- **GET /api/device-tokens/:userId** - Get active device tokens

**Note:** The technician tasks endpoint is being implemented in issue #116. Until then, the app uses mock data for development and testing.

### Mock Data

When the API is unavailable, the app displays mock tasks including:
- Fix HVAC System (CRITICAL priority)
- Replace Water Heater (HIGH priority)
- Install New Thermostat (MEDIUM priority)
- Routine Maintenance Check (LOW priority)

## Push Notifications

The mobile app supports push notifications for task assignments using Firebase Cloud Messaging (FCM) via Expo's push notification service.

### How It Works

1. **Registration**: When a user logs in, the app automatically:
   - Requests notification permissions from the user
   - Obtains an Expo push token
   - Registers the device token with the backend API

2. **Notification Delivery**: When a task is assigned to a technician:
   - The backend sends a push notification to all registered devices for that technician
   - Notification includes task ID, priority, and location
   - Technician receives notification on their device

3. **Deep Linking**: When a technician taps a notification:
   - The app opens automatically
   - User is navigated directly to the task detail screen
   - Task information is immediately available

### Setup for Development

#### On Physical Devices

Push notifications work best on physical devices. To test:

1. Run the app on a physical iOS or Android device via Expo Go or standalone build
2. Log in with a technician ID
3. Grant notification permissions when prompted
4. The device token will be registered automatically

#### Firebase Configuration (Production)

For production builds, you'll need to configure Firebase:

1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add your iOS and Android apps
3. Download `google-services.json` (Android) and place in the `mobile/` directory
4. Download `GoogleService-Info.plist` (iOS) and add to the iOS project
5. Update `app.json` with your Firebase configuration

### Testing Notifications

To test the notification flow:

1. Start the backend notification service
2. Log into the mobile app on a device
3. Assign a task to the technician via the backend API
4. The notification should appear on the device
5. Tap the notification to navigate to the task detail

### Notification Payload

Notifications include the following data:
```json
{
  "taskId": "123",
  "type": "TASK_ASSIGNED",
  "title": "New Task Assigned",
  "body": "Fix HVAC System - HIGH Priority"
}
```

### Permissions

The app requests notification permissions on login. If denied:
- User can still use the app normally
- No push notifications will be received
- Permissions can be granted later in device settings

### Troubleshooting

**Notifications not received:**
- Ensure device token was registered (check backend logs)
- Verify notification permissions are granted
- Check that backend notification service is running
- For iOS, ensure you're using a physical device (not simulator)

**Deep linking not working:**
- Ensure the notification includes a valid `taskId` in the data payload
- Check that the task detail screen exists and is accessible

## Domain Concepts

### Priority Levels

- **CRITICAL**: Red badge - Urgent, immediate attention required
- **HIGH**: Orange badge - High priority, address soon
- **MEDIUM**: Yellow badge - Normal priority
- **LOW**: Green badge - Low priority, can wait

### Task Status

- **ASSIGNED**: Blue badge - Task assigned to technician
- **IN_PROGRESS**: Yellow badge - Work in progress
- **COMPLETED**: Green badge - Task completed
- **UNASSIGNED**: Gray badge - Task not yet assigned

### Duration Display

- Durations are displayed in hours and minutes format
- Examples: `1h 30m`, `45m`, `2h`

## Configuration

### API Base URL

The API base URL can be configured using environment variables:

1. Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```

2. Edit `.env` and set the API URL:
```bash
# For localhost (emulator/simulator)
EXPO_PUBLIC_API_BASE_URL=http://localhost:8080/api

# For physical device (use your computer's IP)
EXPO_PUBLIC_API_BASE_URL=http://192.168.1.100:8080/api
```

Alternatively, edit the default value in `/src/services/taskService.js`:

```javascript
const API_BASE_URL = process.env.EXPO_PUBLIC_API_BASE_URL || 'http://localhost:8080/api';
```

### App Configuration

App settings can be modified in `app.json`:
- App name
- App icon
- Splash screen
- Orientation settings

## Troubleshooting

### Metro Bundler Issues

Clear the cache and restart:

```bash
npx expo start -c
```

### Port Already in Use

Kill the process and restart:

```bash
# Find the process
lsof -ti:8081

# Kill it
kill -9 <PID>

# Restart
npm start
```

### Unable to Connect to Backend

1. Ensure the backend is running on `http://localhost:8080`
2. For physical devices, use your computer's IP address instead of `localhost`
3. Update the `API_BASE_URL` in `taskService.js`

Example for physical device:
```javascript
const API_BASE_URL = 'http://192.168.1.100:8080/api';
```

### iOS Simulator Issues

Reset the simulator:

```bash
xcrun simctl erase all
```

### Android Emulator Issues

Cold boot the emulator from Android Studio's AVD Manager.

## Development Notes

### Authentication

The current authentication is simplified for development:
- User credentials are stored in AsyncStorage
- No token-based authentication yet
- Production version should implement proper JWT/OAuth flow

### Offline Support

- User data is persisted in AsyncStorage
- Tasks are cached locally
- App displays cached data when API is unavailable
- Shows a warning banner when offline

### Error Handling

- Network errors are caught and displayed to the user
- Fallback to mock data for development
- Pull-to-refresh allows retry

## Future Enhancements

- [ ] Task detail view with full information
- [ ] Task status update functionality (start, complete)
- [ ] Integration with maps for navigation
- [ ] Real-time updates using WebSockets
- [ ] Push notifications for new tasks
- [ ] Offline-first architecture with sync
- [ ] Photo capture for task completion
- [ ] Digital signature capture
- [ ] Time tracking functionality
- [ ] Dark mode support

## Related Issues

- **Story**: #23 - View Assigned Tasks on Mobile
- **Current Task**: #115 - Create Mobile Task List UI
- **Dependency**: #116 - Create Mobile Task List API (Backend)

## Contributing

1. Create a feature branch
2. Make your changes
3. Test on both iOS and Android
4. Ensure code follows React Native best practices
5. Submit a pull request

## License

Proprietary - Internal Use Only
