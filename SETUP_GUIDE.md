# Replay Analysis Tool - Setup Guide

This project consists of two main components:
1. **Spring Boot Backend** - Handles YAML parsing, validation, and file comparison
2. **Next.js Frontend** - React UI for uploading files, managing comparison rules, and viewing results

## Prerequisites

- Java 17 or higher
- Maven 3.8.1 or higher
- Node.js 18+ and npm/pnpm
- Two YAML replay files to compare

## Backend Setup (Spring Boot)

### 1. Build the Spring Boot Application

```bash
cd /vercel/share/v0-project
mvn clean install
```

### 2. Run the Backend Server

```bash
mvn spring-boot:run
```

The server will start on `http://localhost:8081` with context path `/api`.

**Expected output:**
```
Started ReplayAnalyzerApplication in X.XXX seconds
```

### API Endpoints

- **POST** `/api/replay/parse` - Parse a single YAML file
- **POST** `/api/replay/compare` - Compare two YAML files with comparison rules
- **GET** `/api/replay/health` - Health check

### Example Compare Request

```bash
curl -X POST http://localhost:8081/api/replay/compare \
  -H "Content-Type: application/json" \
  -d '{
    "file1": {
      "content": "- isin: US1234567890\n  mandate: true",
      "fileName": "file1.yaml"
    },
    "file2": {
      "content": "- isin: US1234567890\n  mandate: false",
      "fileName": "file2.yaml"
    },
    "rules": {
      "checkMissingFields": true,
      "checkValueMismatches": true,
      "validateISINFormat": true,
      "validateDateFormat": true,
      "validateTrackerIdFormat": true,
      "checkDecisionConsistency": true
    }
  }'
```

## Frontend Setup (Next.js)

### 1. Install Dependencies

```bash
# From the project root
pnpm install
```

### 2. Run the Development Server

```bash
pnpm dev
```

The frontend will start on `http://localhost:3000`.

## Using the Application

### Step 1: Upload Files
- Click on the upload areas or drag-and-drop YAML files
- Both "First File" and "Second File" must be uploaded

### Step 2: Configure Rules
In the right panel, enable/disable comparison checks:
- **Check Missing Fields** - Detect fields present in one file but not the other
- **Check Value Mismatches** - Find different values for the same field
- **Validate ISIN Format** - Ensure ISIN codes follow proper format
- **Validate Date Format** - Check date field formats
- **Validate Tracker ID Format** - Validate tracker ID patterns
- **Check Decision Consistency** - Find inconsistent boolean values across records

### Step 3: Compare
Click "Compare Files" to run the analysis. Results will appear below with multiple views:
- **Summary** - Overview statistics
- **Discrepancies** - Field differences between files
- **Validation** - Format validation errors
- **Raw JSON** - Full analysis result as JSON

### Step 4: Export Results
Click "Export JSON" to download the complete analysis result as a JSON file.

## Project Structure

### Backend
```
src/main/java/com/replay/
├── ReplayAnalyzerApplication.java    # Spring Boot entry point
├── controller/
│   └── ReplayController.java          # REST API endpoints
├── service/
│   ├── YamlParserService.java        # Lenient YAML parsing
│   ├── ValidationService.java        # Format validation
│   └── ComparisonService.java        # File comparison logic
└── model/
    ├── ReplayFile.java               # Parsed file model
    ├── ReplayRecord.java             # Individual record model
    ├── ComparisonResult.java         # Analysis result model
    └── ComparisonRules.java          # Comparison configuration
```

### Frontend
```
app/
├── page.tsx                          # Main application page
├── layout.tsx                        # Root layout with metadata
└── globals.css                       # Theme and styling

components/
├── FileUploader.tsx                  # File drag-drop upload
├── RuleManager.tsx                   # Comparison rules UI
└── ResultsViewer.tsx                 # Results display with tabs
```

## Troubleshooting

### Backend won't start
- Ensure Java 17+ is installed: `java -version`
- Check if port 8081 is available
- Try: `mvn clean install -U` to update dependencies

### Frontend API errors
- Verify backend is running on `http://localhost:8081`
- Check browser console for CORS errors
- Ensure both services have CORS enabled

### YAML Parsing Errors
- The parser handles malformed YAML leniently
- Check the "Validation" tab in results for specific field errors
- Common issues: missing colons, typos, inconsistent field names

## Configuration

### Backend (application.yml)
- Server port: `8081`
- Context path: `/api`
- CORS allowed origins: `http://localhost:3000`, `http://localhost:8080`

### Frontend
- Backend API URL: `http://localhost:8081/api` (defined in page.tsx)
- Update this if running backend on different host/port

## Sample Files

The project includes sample malformed YAML files for testing:
- Records may be missing keys
- Typos in field names (e.g., `SLING` vs `SUNG`)
- Inconsistent field presence across records
- Different value formats for the same field

These samples demonstrate the parser's ability to handle real-world malformed data.

## Next Steps

- Modify validation rules in `ValidationService.java` to add custom checks
- Extend comparison rules in `ComparisonService.java` for domain-specific logic
- Customize UI colors and layout in `globals.css` and component files
- Deploy backend as Docker container for production use
