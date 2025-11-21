# Mobile App UI Guide

## Login Screen

The login screen provides a simple authentication flow:

**Features:**
- Technician ID input field
- Name input field  
- Login button
- Demo credentials display for quick testing

**UI Elements:**
- App logo (🔧)
- App title: "FSM Mobile"
- Subtitle: "Field Service Management"
- Two input fields with labels
- Blue primary action button
- Light blue info box with demo credentials

**Demo Credentials:**
- Technician ID: `tech-001`
- Name: `John Smith`

---

## Task List Screen

The main task list screen displays all assigned tasks for the logged-in technician.

**Header Section:**
- Blue header bar with app title "My Tasks"
- Subtitle showing "Welcome, [Technician Name]"

**Task Cards:**
Each task is displayed as a card with the following information:

1. **Priority Badge** (Top Left)
   - CRITICAL: Red background
   - HIGH: Orange background
   - MEDIUM: Yellow background
   - LOW: Green background
   - Badge displays priority text in white

2. **Task Title** (Bold, Large Text)
   - Example: "Fix HVAC System"

3. **Client Address** (With location icon 📍)
   - Example: "123 Main St, Springfield, IL 62701"

4. **Footer Section**
   - **Duration** (Left side with clock icon ⏱️)
     - Example: "2h" or "1h 30m"
   - **Status Badge** (Right side)
     - ASSIGNED: Blue background
     - IN_PROGRESS: Yellow background
     - COMPLETED: Green background
     - Status text in white

**Task Sorting:**
- Tasks are automatically sorted by priority (highest to lowest)
- Within same priority, sorted by creation date (newest first)

**Pull to Refresh:**
- Pull down on the task list to refresh
- Loading spinner appears during refresh
- Tasks update when refresh completes

**Empty State:**
- Shows when no tasks are available
- Displays 📋 icon
- Message: "No Tasks Found"
- Subtext: "You don't have any assigned tasks at the moment."

**Error State:**
- Shows when API connection fails
- Displays ⚠️ icon
- Message: "Connection Error"
- Shows fallback to cached data if available
- Yellow warning banner at top if showing cached data

**Loading State:**
- Shows spinner with "Loading tasks..." text
- Appears during initial load

---

## Color Scheme

### Priority Colors
- **CRITICAL**: `#dc3545` (Red)
- **HIGH**: `#fd7e14` (Orange)
- **MEDIUM**: `#ffc107` (Yellow)
- **LOW**: `#28a745` (Green)

### Status Colors
- **ASSIGNED**: `#007bff` (Blue)
- **IN_PROGRESS**: `#ffc107` (Yellow)
- **COMPLETED**: `#28a745` (Green)
- **UNASSIGNED**: `#6c757d` (Gray)

### App Colors
- **Primary**: `#007bff` (Blue) - Used for headers, buttons
- **Background**: `#f5f5f5` (Light Gray)
- **Card Background**: `#fff` (White)
- **Text Primary**: `#333` (Dark Gray)
- **Text Secondary**: `#666` (Medium Gray)
- **Text Tertiary**: `#999` (Light Gray)

---

## Sample Tasks Display Order

Given these tasks:
1. Fix HVAC System - CRITICAL
2. Replace Water Heater - HIGH
3. Install New Thermostat - MEDIUM
4. Routine Maintenance Check - LOW

They will appear in this exact order (CRITICAL > HIGH > MEDIUM > LOW).

---

## Interaction Flow

1. **Launch App** → Login Screen
2. **Enter Credentials** → Tap Login Button
3. **Authentication** → Credentials saved to AsyncStorage
4. **Navigate** → Task List Screen
5. **View Tasks** → Scroll through sorted task cards
6. **Refresh** → Pull down to fetch latest tasks
7. **Tap Task** → (Future: Navigate to task detail view)

---

## Technical Implementation

### Components
- `LoginScreen.js`: Authentication UI
- `TaskListScreen.js`: Main task list with refresh
- `TaskCard.js`: Individual task card component
- `AuthContext.js`: Authentication state management
- `taskService.js`: API integration and utilities

### State Management
- Uses React Context API for authentication
- Local component state for task list
- AsyncStorage for persistence

### API Integration
- Endpoint: `GET /api/technicians/:id/tasks`
- Falls back to mock data when API unavailable
- Shows cached data with warning banner on error

### Responsive Design
- Uses React Native's Flexbox layout
- Properly handles different screen sizes
- Touch-optimized card interactions
- Native pull-to-refresh behavior
