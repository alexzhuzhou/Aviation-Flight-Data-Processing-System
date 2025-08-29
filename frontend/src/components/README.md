# Components Documentation

This folder contains reusable Vue.js components used throughout the Aviation Flight Dashboard.

## Components Overview

### `FlightDetailsModal.vue`
**Comprehensive Flight Information Modal**

**Purpose**: Displays detailed information about a selected flight in a modal dialog

**Key Features:**
- **Flight Overview**: Basic flight information (planId, indicative, aircraft type)
- **Tracking Points**: Complete list of flight tracking data with timestamps
- **Route Information**: Departure and arrival points with timing
- **Performance Metrics**: Flight statistics and analysis data
- **Interactive Elements**: Expandable sections and data tables

**Props:**
```javascript
{
  isOpen: Boolean,        // Controls modal visibility
  flight: Object,         // Flight data object
  onClose: Function       // Close modal callback
}
```

**Usage Example:**
```vue
<FlightDetailsModal
  :isOpen="showModal"
  :flight="selectedFlight"
  @close="showModal = false"
/>
```

**Data Structure Expected:**
```javascript
{
  planId: Number,
  indicative: String,
  aircraftType: String,
  airline: String,
  trackingPoints: Array,
  eobt: String,
  eta: String,
  startPointIndicative: String,
  endPointIndicative: String,
  totalTrackingPoints: Number,
  hasTrackingData: Boolean
}
```

**Sections Displayed:**
1. **Flight Header**: planId, indicative, aircraft type, airline
2. **Flight Details**: EOBT, ETA, origin, destination
3. **Tracking Points Table**: Timestamp, coordinates, altitude, speed
4. **Statistics**: Total points, data availability status

**Styling:**
- Uses Tailwind CSS for responsive design
- Headless UI for accessible modal behavior
- Heroicons for consistent iconography
- Dark/light theme support

**Interactions:**
- **Close Modal**: Click outside, ESC key, or close button
- **Scroll Tracking Points**: Paginated table with search
- **Copy Data**: Click to copy coordinates or other data
- **Export**: Download flight data as JSON/CSV

## Component Guidelines

### Creating New Components

#### 1. Component Structure
```vue
<template>
  <!-- Component template -->
</template>

<script>
import { ref, computed } from 'vue'

export default {
  name: 'ComponentName',
  props: {
    // Define props with types and defaults
  },
  emits: ['event-name'],
  setup(props, { emit }) {
    // Composition API logic
    return {
      // Reactive data and methods
    }
  }
}
</script>

<style scoped>
/* Component-specific styles */
</style>
```

#### 2. Naming Conventions
- **PascalCase** for component names
- **kebab-case** for props and events
- **Descriptive names** that indicate purpose

#### 3. Props Guidelines
- **Define types** for all props
- **Provide defaults** where appropriate
- **Use validators** for complex props
- **Document expected structure** for object props

#### 4. Event Handling
- **Emit events** for parent communication
- **Use descriptive event names**
- **Pass relevant data** with events

### Styling Guidelines

#### Tailwind CSS Classes
- **Responsive design**: Use `sm:`, `md:`, `lg:` prefixes
- **Dark mode**: Use `dark:` prefix for dark theme styles
- **Consistent spacing**: Use standard Tailwind spacing scale
- **Color palette**: Stick to defined color scheme

#### Common Patterns
```css
/* Modal backdrop */
.modal-backdrop {
  @apply fixed inset-0 bg-black bg-opacity-50 z-40;
}

/* Card container */
.card {
  @apply bg-white dark:bg-gray-800 rounded-lg shadow-md p-6;
}

/* Button styles */
.btn-primary {
  @apply bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded;
}
```

### Accessibility Guidelines

#### ARIA Labels
- **Descriptive labels** for interactive elements
- **Role attributes** for custom components
- **Screen reader** friendly content

#### Keyboard Navigation
- **Tab order** for focusable elements
- **ESC key** to close modals/dropdowns
- **Enter/Space** for button activation

#### Color Contrast
- **WCAG AA compliance** for text contrast
- **Alternative indicators** beyond color
- **Focus indicators** for keyboard users

## Best Practices

### Performance
- **Lazy loading** for heavy components
- **v-show vs v-if** based on toggle frequency
- **Computed properties** for derived data
- **Event listener cleanup** in unmounted hook

### Reusability
- **Generic props** instead of hardcoded values
- **Slot support** for flexible content
- **Configurable styling** through props
- **Minimal dependencies** on parent components

### Testing
- **Unit tests** for component logic
- **Props validation** testing
- **Event emission** testing
- **Accessibility** testing

### Documentation
- **JSDoc comments** for complex methods
- **Props documentation** with examples
- **Usage examples** in component comments
- **Change log** for component updates

## Future Components

### Planned Components
- **FlightMap**: Interactive map component for trajectory visualization
- **ChartContainer**: Reusable chart wrapper for analytics
- **DataTable**: Generic table component with sorting/filtering
- **LoadingSpinner**: Consistent loading indicators
- **ErrorBoundary**: Error handling component
- **NotificationToast**: User feedback notifications

### Component Architecture
- **Atomic design** principles (atoms, molecules, organisms)
- **Composition over inheritance**
- **Single responsibility** principle
- **Loose coupling** between components
