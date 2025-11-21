# FSM Frontend - Task Management

This is the frontend application for the Field Service Management (FSM) system, implementing a micro-frontend architecture for task management.

## Architecture

The frontend is organized as a Lerna monorepo with the following structure:

```
frontend/
├── shell/              # Main container app (hosts micro-frontends)
├── task-management/    # Task management micro-frontend
├── package.json        # Root package.json with workspaces
└── lerna.json         # Lerna configuration
```

### Micro-Frontend Architecture

- **Shell Container** (`frontend/shell/`): Main container application that hosts other micro-frontends in iframes
- **Task Management** (`frontend/task-management/`): Standalone micro-frontend for task creation and management
- Each micro-frontend is independently deployable and testable
- Communication between micro-frontends is isolated (no shared code imports)

## Technology Stack

- **React 18.3** - UI framework
- **Vite 6.0** - Build tool and dev server
- **Vitest 2.1** - Testing framework
- **React Testing Library** - Component testing utilities
- **Lerna 8.1** - Monorepo management
- **CSS** - Styling (no framework dependencies)

## Getting Started

### Prerequisites

- Node.js 20.x or higher
- npm 10.x or higher

### Installation

```bash
# From repository root
cd frontend
npm install
```

This will install dependencies for all workspaces (shell, task-management).

### Development

#### Run All Apps

```bash
cd frontend
npm run dev
```

This starts all micro-frontends in parallel:
- Shell: http://localhost:3000
- Task Management: http://localhost:3001

#### Run Individual Apps

**Shell Container:**
```bash
cd frontend/shell
npm run dev
```

**Task Management:**
```bash
cd frontend/task-management
npm run dev
```

### Testing

#### Run All Tests

```bash
cd frontend
npm test
```

#### Run Tests with Coverage

```bash
cd frontend/task-management
npm run test:coverage
```

**Current Coverage:**
- Overall: 94.28%
- CreateTaskForm: 100%
- TaskList: 99%
- App: 100%

### Building for Production

```bash
# Build all apps
cd frontend
npm run build

# Build individual app
cd frontend/task-management
npm run build
```

## Task Management Micro-Frontend

### Features

#### Task List View
- Displays all service tasks in a sortable table
- Tasks sorted by priority (CRITICAL > HIGH > MEDIUM > LOW) and creation date
- Color-coded priority and status badges
- Refresh button to reload tasks
- Handles empty, loading, and error states

#### Create Task Form
- Form fields: title, description, client address, priority, estimated duration
- Client-side validation with required field indicators
- Success/error message display
- Form reset after successful submission
- Priority dropdown with domain-constrained values (Low, Medium, High, Critical)

### API Integration

The task management app integrates with the backend API:

**Base URL:** `http://localhost:8080`

**Endpoints:**
- `GET /api/tasks` - Fetch all tasks
- `POST /api/tasks` - Create new task

**Example Request (Create Task):**
```json
{
  "title": "Fix HVAC System",
  "description": "Air conditioning not working",
  "clientAddress": "123 Main St, Springfield, IL 62701",
  "priority": "HIGH",
  "estimatedDuration": 120
}
```

**Example Response:**
```json
{
  "id": 1,
  "title": "Fix HVAC System",
  "description": "Air conditioning not working",
  "clientAddress": "123 Main St, Springfield, IL 62701",
  "priority": "HIGH",
  "estimatedDuration": 120,
  "status": "UNASSIGNED",
  "createdAt": "2025-11-20T22:00:00"
}
```

### Component Structure

```
src/
├── components/
│   ├── CreateTaskForm.jsx      # Task creation form
│   ├── CreateTaskForm.css      # Form styles
│   ├── CreateTaskForm.test.jsx # Form tests
│   ├── TaskList.jsx            # Task list display
│   ├── TaskList.css            # List styles
│   └── TaskList.test.jsx       # List tests
├── App.jsx                      # Main app with tab navigation
├── App.css                      # App styles
├── App.test.jsx                # App tests
├── main.jsx                     # Entry point
├── index.css                    # Global styles
└── setupTests.js               # Test configuration
```

## Code Quality

### Testing Standards

- Minimum 85% code coverage required
- Unit tests for all components
- Integration tests for user flows
- Mock API calls in tests

### Test Commands

```bash
# Run tests
npm test

# Run tests in watch mode
npm run test:watch

# Run tests with coverage
npm run test:coverage

# Run tests for specific file
npx vitest run -t "ComponentName"
```

### Linting

```bash
npm run lint
```

## Domain Concepts

### Priority Levels
- **CRITICAL**: Urgent, immediate attention required
- **HIGH**: High priority, address soon
- **MEDIUM**: Normal priority
- **LOW**: Low priority, can wait

### Task Status
- **UNASSIGNED**: Task created but not assigned to technician
- **ASSIGNED**: Task assigned to technician
- **IN_PROGRESS**: Work in progress
- **COMPLETED**: Task completed

### Estimated Duration
- Stored in minutes in the backend
- Displayed and input in hours in the UI
- Automatically converted between formats

## Troubleshooting

### Port Already in Use

If you see "Port 3001 is already in use", kill the process:
```bash
# Find process
lsof -ti:3001

# Kill process
kill -9 <PID>
```

### API Connection Refused

Ensure the backend service is running on port 8080:
```bash
cd backend/task-svc
mvn spring-boot:run
```

### Tests Failing

Clear test cache and reinstall:
```bash
rm -rf node_modules coverage
npm install
npm test
```

## Future Enhancements

- [ ] Additional micro-frontends for other bounded contexts
- [ ] User authentication and authorization
- [ ] Real-time updates via WebSocket
- [ ] Offline support with service workers
- [ ] Advanced filtering and search
- [ ] Task assignment workflow
- [ ] Dashboard with analytics
- [ ] Mobile responsive improvements
- [ ] Internationalization (i18n)
- [ ] Accessibility (a11y) improvements

## Contributing

1. Create a feature branch
2. Make your changes
3. Ensure tests pass (`npm test`)
4. Ensure coverage meets minimum 85% (`npm run test:coverage`)
5. Submit a pull request

## License

Proprietary - Internal Use Only
