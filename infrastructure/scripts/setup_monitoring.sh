#!/bin/bash
#
# setup_monitoring.sh - Automates the setup and configuration of monitoring services
# for the ScanMonitorApps application, including Firebase Crashlytics, Analytics,
# Performance Monitoring, and Datadog API integration.
#
# Usage: ./setup_monitoring.sh [options]
#
# Options:
#   -h, --help       Display this help message and exit
#   -v, --verbose    Enable verbose output
#   -p, --project    Specify Firebase project ID (optional)
#   -a, --app        Specify Android application package name (default: com.jump.scanmonitor)
#

# Exit on error, undefined variables, and pipe failures
set -euo pipefail

# Global variables
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
CONFIG_DIR="${SCRIPT_DIR}/../config"
FIREBASE_CONFIG="${CONFIG_DIR}/firebase_setup.md"
DATADOG_CONFIG="${CONFIG_DIR}/datadog_metrics.json"
PROJECT_ROOT="${SCRIPT_DIR}/../../"
ANDROID_DIR="${PROJECT_ROOT}/src/android"
APP_DIR="${ANDROID_DIR}/app"
GOOGLE_SERVICES_JSON="${APP_DIR}/google-services.json"
LOG_FILE="${SCRIPT_DIR}/monitoring_setup.log"

# Default values
VERBOSE=false
FIREBASE_PROJECT_ID=""
APP_PACKAGE="com.jump.scanmonitor"

# Function to display help information
show_help() {
    echo "Usage: $(basename "$0") [options]"
    echo ""
    echo "Automates the setup and configuration of monitoring services for the ScanMonitorApps application,"
    echo "including Firebase Crashlytics, Analytics, Performance Monitoring, and Datadog API integration."
    echo ""
    echo "Options:"
    echo "  -h, --help       Display this help message and exit"
    echo "  -v, --verbose    Enable verbose output"
    echo "  -p, --project    Specify Firebase project ID (optional)"
    echo "  -a, --app        Specify Android application package name (default: com.jump.scanmonitor)"
    echo ""
    echo "Example:"
    echo "  $(basename "$0") -p my-firebase-project -a com.jump.scanmonitor"
    echo ""
    echo "Note: This script requires Node.js, npm, jq, and firebase-tools."
}

# Function to log messages to both console and log file
log_message() {
    local message="$1"
    local level="${2:-INFO}"
    local timestamp=$(date "+%Y-%m-%d %H:%M:%S")
    local formatted_message="[$timestamp] [$level] $message"
    
    # Print to console with color based on level
    case "$level" in
        "INFO")
            echo -e "\033[0;32m$formatted_message\033[0m"
            ;;
        "WARN")
            echo -e "\033[0;33m$formatted_message\033[0m"
            ;;
        "ERROR")
            echo -e "\033[0;31m$formatted_message\033[0m"
            ;;
        *)
            echo "$formatted_message"
            ;;
    esac
    
    # Append to log file
    echo "$formatted_message" >> "$LOG_FILE"
}

# Function to check if required tools are installed
check_prerequisites() {
    log_message "Checking prerequisites..." "INFO"
    
    # Check for Node.js
    if ! command -v node &> /dev/null; then
        log_message "Node.js is not installed. Please install Node.js to continue." "ERROR"
        log_message "Visit https://nodejs.org/ for installation instructions." "INFO"
        return 1
    fi
    
    local node_version=$(node --version | cut -d 'v' -f 2)
    log_message "Node.js version $node_version is installed" "INFO"
    
    # Check for npm
    if ! command -v npm &> /dev/null; then
        log_message "npm is not installed. Please install npm to continue." "ERROR"
        return 1
    fi
    
    local npm_version=$(npm --version)
    log_message "npm version $npm_version is installed" "INFO"
    
    # Check for jq
    if ! command -v jq &> /dev/null; then
        log_message "jq is not installed. Attempting to install..." "WARN"
        
        if command -v apt-get &> /dev/null; then
            log_message "Installing jq using apt-get..." "INFO"
            sudo apt-get update && sudo apt-get install -y jq
        elif command -v brew &> /dev/null; then
            log_message "Installing jq using Homebrew..." "INFO"
            brew install jq
        else
            log_message "Unable to install jq automatically. Please install jq manually." "ERROR"
            log_message "Visit https://stedolan.github.io/jq/download/ for installation instructions." "INFO"
            return 1
        fi
    fi
    
    local jq_version=$(jq --version)
    log_message "jq $jq_version is installed" "INFO"
    
    # Check for firebase-tools
    if ! command -v firebase &> /dev/null; then
        log_message "firebase-tools is not installed. Attempting to install..." "WARN"
        npm install -g firebase-tools
    fi
    
    local firebase_version=$(firebase --version)
    log_message "firebase-tools version $firebase_version is installed" "INFO"
    
    log_message "All prerequisites are met!" "INFO"
    return 0
}

# Function to set up Firebase services
setup_firebase() {
    log_message "Setting up Firebase services..." "INFO"
    
    # Check if user is authenticated with Firebase
    if ! firebase login:list | grep -q "No need for authentication"; then
        log_message "You need to authenticate with Firebase. Launching login..." "INFO"
        firebase login
    fi
    
    # Check if Firebase project exists or needs to be created
    if [ -z "$FIREBASE_PROJECT_ID" ]; then
        log_message "No Firebase project ID specified. Creating a new project..." "INFO"
        
        # Create a new Firebase project with a default name
        local project_name="ScanMonitorApps-$(date +%s)"
        log_message "Creating Firebase project: $project_name" "INFO"
        
        # Create project and extract project ID
        FIREBASE_PROJECT_ID=$(firebase projects:create --display-name "$project_name" | grep "Project ID" | awk '{print $3}')
        
        if [ -z "$FIREBASE_PROJECT_ID" ]; then
            log_message "Failed to create Firebase project" "ERROR"
            return 1
        fi
        
        log_message "Firebase project created with ID: $FIREBASE_PROJECT_ID" "INFO"
    else
        log_message "Using existing Firebase project: $FIREBASE_PROJECT_ID" "INFO"
    fi
    
    # Set default project
    firebase use "$FIREBASE_PROJECT_ID"
    
    # Register Android app with Firebase if not already registered
    if ! firebase apps:list ANDROID | grep -q "$APP_PACKAGE"; then
        log_message "Registering Android app ($APP_PACKAGE) with Firebase..." "INFO"
        firebase apps:create ANDROID "$APP_PACKAGE" "ScanMonitorApps"
        
        # Get the App ID
        local app_id=$(firebase apps:list ANDROID | grep "$APP_PACKAGE" | awk '{print $4}')
        log_message "Android app registered with ID: $app_id" "INFO"
    else
        log_message "Android app already registered with Firebase" "INFO"
    fi
    
    # Download google-services.json
    log_message "Downloading google-services.json..." "INFO"
    firebase apps:sdkconfig ANDROID --out "$GOOGLE_SERVICES_JSON"
    
    if [ ! -f "$GOOGLE_SERVICES_JSON" ]; then
        log_message "Failed to download google-services.json" "ERROR"
        return 1
    fi
    
    log_message "google-services.json downloaded and placed in $APP_DIR" "INFO"
    
    # Enable Firebase services
    log_message "Enabling Firebase Crashlytics..." "INFO"
    firebase --project="$FIREBASE_PROJECT_ID" crashlytics:enable
    
    log_message "Enabling Firebase Analytics..." "INFO"
    firebase --project="$FIREBASE_PROJECT_ID" analytics:enable
    
    log_message "Enabling Firebase Performance Monitoring..." "INFO"
    firebase --project="$FIREBASE_PROJECT_ID" perf:enable
    
    log_message "Firebase services setup completed successfully!" "INFO"
    return 0
}

# Function to configure Datadog API monitoring
configure_datadog_monitoring() {
    log_message "Configuring Datadog API monitoring..." "INFO"
    
    # Check if Datadog configuration file exists
    if [ ! -f "$DATADOG_CONFIG" ]; then
        log_message "Datadog configuration file not found: $DATADOG_CONFIG" "ERROR"
        return 1
    fi
    
    # Check if API keys are available from environment variables
    if [ -z "${DATADOG_API_KEY:-}" ] || [ -z "${DATADOG_APP_KEY:-}" ]; then
        log_message "Datadog API keys not found in environment variables" "WARN"
        log_message "Please set DATADOG_API_KEY and DATADOG_APP_KEY environment variables" "INFO"
        log_message "Example: export DATADOG_API_KEY=your_api_key" "INFO"
        log_message "         export DATADOG_APP_KEY=your_app_key" "INFO"
        log_message "Continuing with setup, but verification may fail..." "WARN"
    else
        log_message "Datadog API keys found in environment variables" "INFO"
    fi
    
    # Extract API settings from configuration
    local api_base_url=$(jq -r '.api_settings.base_url' "$DATADOG_CONFIG")
    local api_endpoint=$(jq -r '.api_settings.endpoint' "$DATADOG_CONFIG")
    local query=$(jq -r '.metrics[0].query' "$DATADOG_CONFIG")
    
    log_message "Datadog API configuration:" "INFO"
    log_message "  Base URL: $api_base_url" "INFO"
    log_message "  Endpoint: $api_endpoint" "INFO"
    log_message "  Query: $query" "INFO"
    
    # Validate BuildConfig has Datadog API keys configuration
    local build_gradle_path="$APP_DIR/build.gradle.kts"
    if [ ! -f "$build_gradle_path" ]; then
        log_message "Android app build.gradle.kts not found: $build_gradle_path" "ERROR"
        return 1
    fi
    
    if ! grep -q "DATADOG_API_KEY" "$build_gradle_path"; then
        log_message "Datadog API key configuration not found in build.gradle.kts" "ERROR"
        log_message "Please ensure the BuildConfig fields are properly set up" "INFO"
        return 1
    fi
    
    log_message "Datadog API monitoring configuration verified" "INFO"
    return 0
}

# Function to verify monitoring setup
verify_monitoring_setup() {
    log_message "Verifying monitoring setup..." "INFO"
    local verification_status=0
    
    # Verify Firebase project and configuration
    log_message "Verifying Firebase configuration..." "INFO"
    
    # Check if google-services.json exists
    if [ ! -f "$GOOGLE_SERVICES_JSON" ]; then
        log_message "google-services.json file not found at: $GOOGLE_SERVICES_JSON" "ERROR"
        verification_status=1
    else
        log_message "google-services.json file is present" "INFO"
        
        # Verify package name in google-services.json
        local package_name=$(jq -r '.client[0].client_info.android_client_info.package_name' "$GOOGLE_SERVICES_JSON")
        if [ "$package_name" != "$APP_PACKAGE" ]; then
            log_message "Package name mismatch in google-services.json: Expected '$APP_PACKAGE', found '$package_name'" "ERROR"
            verification_status=1
        else
            log_message "Package name in google-services.json is correct: $package_name" "INFO"
        fi
    fi
    
    # Verify Firebase project
    if [ -n "$FIREBASE_PROJECT_ID" ]; then
        local project_verification=$(firebase projects:list | grep "$FIREBASE_PROJECT_ID" || echo "")
        if [ -z "$project_verification" ]; then
            log_message "Unable to verify Firebase project: $FIREBASE_PROJECT_ID" "ERROR"
            verification_status=1
        else
            log_message "Firebase project verified: $FIREBASE_PROJECT_ID" "INFO"
        fi
    else
        log_message "Firebase project ID is not set, skipping project verification" "WARN"
    fi
    
    # Verify Datadog API integration if keys are available
    log_message "Verifying Datadog API integration..." "INFO"
    if [ -n "${DATADOG_API_KEY:-}" ] && [ -n "${DATADOG_APP_KEY:-}" ]; then
        # Perform a test query to Datadog API
        local api_base_url=$(jq -r '.api_settings.base_url' "$DATADOG_CONFIG")
        local api_endpoint=$(jq -r '.api_settings.endpoint' "$DATADOG_CONFIG")
        local query=$(jq -r '.metrics[0].query' "$DATADOG_CONFIG")
        local now=$(date +%s)
        local one_hour_ago=$((now - 3600))
        
        log_message "Testing Datadog API query..." "INFO"
        local api_url="${api_base_url}${api_endpoint}?query=${query}&from=${one_hour_ago}&to=${now}"
        
        # Use curl to query the API
        local response=$(curl -s -X GET "$api_url" \
            -H "DD-API-KEY: $DATADOG_API_KEY" \
            -H "DD-APPLICATION-KEY: $DATADOG_APP_KEY" || echo '{"status":"error"}')
        
        # Check if the response contains a series or metadata field, indicating success
        if echo "$response" | jq -e '.series != null or .metadata != null' > /dev/null; then
            log_message "Datadog API query successful!" "INFO"
        else
            log_message "Datadog API query failed. Response: $(echo "$response" | jq -c .)" "ERROR"
            verification_status=1
        fi
    else
        log_message "Datadog API keys not available, skipping API verification" "WARN"
    fi
    
    # Verify Android app build configuration
    log_message "Verifying Android app build configuration..." "INFO"
    local build_gradle_path="$APP_DIR/build.gradle.kts"
    
    # Check Firebase dependencies
    if grep -q "firebase-crashlytics-ktx" "$build_gradle_path" && \
       grep -q "firebase-analytics-ktx" "$build_gradle_path"; then
        log_message "Firebase dependencies properly configured in build.gradle.kts" "INFO"
    else
        log_message "Firebase dependencies might be missing in build.gradle.kts" "WARN"
    fi
    
    # Check for com.google.gms.google-services plugin
    if grep -q "id(\"com.google.gms.google-services\")" "$build_gradle_path"; then
        log_message "Google Services plugin properly configured in build.gradle.kts" "INFO"
    else
        log_message "Google Services plugin might be missing in build.gradle.kts" "WARN"
        verification_status=1
    fi
    
    # Check for com.google.firebase.crashlytics plugin
    if grep -q "id(\"com.google.firebase.crashlytics\")" "$build_gradle_path"; then
        log_message "Firebase Crashlytics plugin properly configured in build.gradle.kts" "INFO"
    else
        log_message "Firebase Crashlytics plugin might be missing in build.gradle.kts" "WARN"
        verification_status=1
    fi
    
    # Final verification status
    if [ $verification_status -eq 0 ]; then
        log_message "Monitoring services verification completed successfully!" "INFO"
    else
        log_message "Monitoring services verification completed with issues. Check the log for details." "WARN"
    fi
    
    return $verification_status
}

# Main function
main() {
    # Create log file or truncate it if it exists
    > "$LOG_FILE"
    
    log_message "Starting ScanMonitorApps monitoring setup..." "INFO"
    log_message "Log file: $LOG_FILE" "INFO"
    
    # Parse command-line arguments
    while [[ $# -gt 0 ]]; do
        case "$1" in
            -h|--help)
                show_help
                exit 0
                ;;
            -v|--verbose)
                VERBOSE=true
                shift
                ;;
            -p|--project)
                FIREBASE_PROJECT_ID="$2"
                shift 2
                ;;
            -a|--app)
                APP_PACKAGE="$2"
                shift 2
                ;;
            *)
                log_message "Unknown option: $1" "ERROR"
                show_help
                exit 1
                ;;
        esac
    done
    
    log_message "Configuration:" "INFO"
    log_message "  Android app package: $APP_PACKAGE" "INFO"
    log_message "  Firebase project ID: ${FIREBASE_PROJECT_ID:-Auto-generate}" "INFO"
    log_message "  Verbose mode: $VERBOSE" "INFO"
    
    # Check prerequisites
    if ! check_prerequisites; then
        log_message "Failed to meet prerequisites" "ERROR"
        exit 1
    fi
    
    # Setup Firebase services
    if ! setup_firebase; then
        log_message "Failed to set up Firebase services" "ERROR"
        exit 1
    fi
    
    # Configure Datadog monitoring
    if ! configure_datadog_monitoring; then
        log_message "Failed to configure Datadog monitoring" "ERROR"
        exit 1
    fi
    
    # Verify monitoring setup
    if ! verify_monitoring_setup; then
        log_message "Monitoring setup verification failed" "WARN"
        # We don't exit with error here to allow proceeding with partial setup
    fi
    
    log_message "Monitoring setup completed!" "INFO"
    log_message "Next steps:" "INFO"
    log_message "1. Ensure DATADOG_API_KEY and DATADOG_APP_KEY are set in your environment" "INFO"
    log_message "2. Build the Android app to verify Firebase and Datadog integration" "INFO"
    log_message "3. Check Firebase console to confirm services are enabled" "INFO"
    log_message "   Firebase console: https://console.firebase.google.com/project/$FIREBASE_PROJECT_ID" "INFO"
    
    return 0
}

# Script entry point
main "$@"