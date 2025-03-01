#!/bin/bash
#
# build_release.sh - Build script for ScanMonitorApps Android application
#
# This script automates the build process for release versions of the ScanMonitorApps
# Android application. It handles environment setup, build configuration, signing,
# and artifact generation, providing a consistent and reliable build process.
#
# Usage: ./build_release.sh [options]
#

# Exit on error
set -e

# Script directory and project paths
SCRIPT_DIR=$(dirname "${BASH_SOURCE[0]}")
PROJECT_ROOT=$(cd "$SCRIPT_DIR/../../" && pwd)
ANDROID_DIR="$PROJECT_ROOT/src/android"
APP_MODULE="app"
BUILD_TYPE="release"
OUTPUT_DIR="$PROJECT_ROOT/build-output"
KEYSTORE_PATH="${KEYSTORE_PATH:-"$PROJECT_ROOT/keystore.jks"}"

# Default values
VERSION_NAME=""
VERSION_CODE=""
BUILD_AAB=false
SKIP_TESTS=false
VERBOSE=false

# Print usage information
print_usage() {
    echo "Usage: $(basename "$0") [options]"
    echo ""
    echo "Build script for ScanMonitorApps Android application"
    echo ""
    echo "Options:"
    echo "  -v VERSION_NAME   Semantic version name (e.g., 1.0.0)"
    echo "  -c VERSION_CODE   Numeric version code (e.g., 1)"
    echo "  -k KEYSTORE_PATH  Path to the Android keystore file"
    echo "  -o OUTPUT_DIR     Directory where build artifacts will be stored"
    echo "  -b                Build Android App Bundle (AAB) instead of APK"
    echo "  -s                Skip running tests"
    echo "  -V                Verbose output"
    echo "  -h                Display this help message"
    echo ""
    echo "Environment variables:"
    echo "  ANDROID_HOME      Path to the Android SDK"
    echo "  KEYSTORE_PASSWORD Password for the keystore"
    echo "  KEY_ALIAS         Alias name for the key in the keystore"
    echo "  KEY_PASSWORD      Password for the key"
    echo "  DATADOG_API_KEY   API key for Datadog authentication"
    echo "  DATADOG_APP_KEY   Application key for Datadog authentication"
    echo ""
    echo "Examples:"
    echo "  $(basename "$0") -v 1.0.0 -c 1"
    echo "  $(basename "$0") -v 1.0.0 -c 1 -k /path/to/keystore.jks -b -s"
}

# Parse command-line arguments
parse_arguments() {
    local OPTIND
    while getopts "v:c:k:o:bsVh" opt; do
        case "$opt" in
        v)
            VERSION_NAME=$OPTARG
            ;;
        c)
            VERSION_CODE=$OPTARG
            ;;
        k)
            KEYSTORE_PATH=$OPTARG
            ;;
        o)
            OUTPUT_DIR=$OPTARG
            ;;
        b)
            BUILD_AAB=true
            ;;
        s)
            SKIP_TESTS=true
            ;;
        V)
            VERBOSE=true
            ;;
        h)
            print_usage
            exit 0
            ;;
        \?)
            echo "Invalid option: -$OPTARG" >&2
            print_usage
            return 1
            ;;
        esac
    done

    # Validate parameters
    if [[ -z "$KEYSTORE_PATH" ]]; then
        echo "Error: Keystore path is required" >&2
        return 1
    fi

    return 0
}

# Log a message with timestamp
log_message() {
    local timestamp
    timestamp=$(date +"%Y-%m-%d %H:%M:%S")
    echo "[$timestamp] $1"
    
    if [[ "$VERBOSE" == "true" && -n "$2" ]]; then
        echo "           $2"
    fi
}

# Check environment setup
check_environment() {
    log_message "Checking environment setup..."

    # Check Android SDK
    if [[ -z "$ANDROID_HOME" ]]; then
        log_message "Error: ANDROID_HOME environment variable not set" >&2
        return 1
    fi

    # Check Gradle wrapper
    if [[ ! -x "$ANDROID_DIR/gradlew" ]]; then
        log_message "Error: Gradle wrapper not found or not executable" >&2
        return 1
    fi

    # Check build tools
    local build_tools_dir="$ANDROID_HOME/build-tools"
    if [[ ! -d "$build_tools_dir" ]]; then
        log_message "Error: Android SDK Build Tools not found" >&2
        return 1
    fi

    # Use the latest build tools version
    local latest_build_tools
    latest_build_tools=$(find "$build_tools_dir" -mindepth 1 -maxdepth 1 -type d | sort -V | tail -n 1)
    if [[ -z "$latest_build_tools" ]]; then
        log_message "Error: No Android SDK Build Tools version found" >&2
        return 1
    fi

    # Check for zipalign and apksigner
    if [[ ! -x "$latest_build_tools/zipalign" ]]; then
        log_message "Error: zipalign not found or not executable" >&2
        return 1
    fi

    if [[ ! -x "$latest_build_tools/apksigner" ]]; then
        log_message "Error: apksigner not found or not executable" >&2
        return 1
    fi

    # Check keystore file
    if [[ ! -f "$KEYSTORE_PATH" ]]; then
        log_message "Error: Keystore file not found at $KEYSTORE_PATH" >&2
        return 1
    fi

    # Check required environment variables for signing
    if [[ -z "$KEYSTORE_PASSWORD" ]]; then
        log_message "Error: KEYSTORE_PASSWORD environment variable not set" >&2
        return 1
    fi

    if [[ -z "$KEY_ALIAS" ]]; then
        log_message "Error: KEY_ALIAS environment variable not set" >&2
        return 1
    fi

    if [[ -z "$KEY_PASSWORD" ]]; then
        log_message "Error: KEY_PASSWORD environment variable not set" >&2
        return 1
    fi

    # Check Datadog API keys if needed
    if [[ -z "$DATADOG_API_KEY" ]]; then
        log_message "Warning: DATADOG_API_KEY environment variable not set"
    fi

    if [[ -z "$DATADOG_APP_KEY" ]]; then
        log_message "Warning: DATADOG_APP_KEY environment variable not set"
    fi

    log_message "Environment setup check completed successfully"
    return 0
}

# Setup build directory
setup_build_directory() {
    log_message "Setting up build directory: $OUTPUT_DIR"

    # Create output directory if it doesn't exist
    mkdir -p "$OUTPUT_DIR"

    # Clean output directory
    rm -rf "$OUTPUT_DIR"/*

    log_message "Build directory setup completed"
}

# Extract version information from build.gradle if not provided
extract_version_info() {
    log_message "Extracting version information..."

    local build_gradle="$ANDROID_DIR/$APP_MODULE/build.gradle.kts"
    
    # Extract version name if not provided
    if [[ -z "$VERSION_NAME" ]]; then
        VERSION_NAME=$(grep -E "versionName\s*=\s*\"[^\"]+\"" "$build_gradle" | sed -E 's/.*versionName\s*=\s*"([^"]+)".*/\1/')
        if [[ -z "$VERSION_NAME" ]]; then
            log_message "Error: Could not extract version name from build.gradle.kts" >&2
            return 1
        fi
    fi

    # Extract version code if not provided
    if [[ -z "$VERSION_CODE" ]]; then
        VERSION_CODE=$(grep -E "versionCode\s*=\s*[0-9]+" "$build_gradle" | sed -E 's/.*versionCode\s*=\s*([0-9]+).*/\1/')
        if [[ -z "$VERSION_CODE" ]]; then
            log_message "Error: Could not extract version code from build.gradle.kts" >&2
            return 1
        fi
    fi

    log_message "Using version: $VERSION_NAME ($VERSION_CODE)"
    return 0
}

# Run tests if not skipped
run_tests() {
    if [[ "$SKIP_TESTS" == "true" ]]; then
        log_message "Skipping tests as requested"
        return 0
    fi

    log_message "Running tests..."

    # Change to Android directory
    pushd "$ANDROID_DIR" > /dev/null

    # Run unit tests
    ./gradlew "$APP_MODULE:testDebugUnitTest" --stacktrace
    
    local test_result=$?
    
    popd > /dev/null

    if [[ $test_result -ne 0 ]]; then
        log_message "Error: Unit tests failed" >&2
        return $test_result
    fi

    log_message "Tests completed successfully"
    return 0
}

# Build release APK
build_apk() {
    log_message "Building release APK..."

    # Change to Android directory
    pushd "$ANDROID_DIR" > /dev/null

    # Build the release APK
    ./gradlew "$APP_MODULE:assemble${BUILD_TYPE^}" \
        -PversionName="$VERSION_NAME" \
        -PversionCode="$VERSION_CODE" \
        -PdatadogApiKey="$DATADOG_API_KEY" \
        -PdatadogAppKey="$DATADOG_APP_KEY" \
        --stacktrace
    
    local build_result=$?
    
    if [[ $build_result -ne 0 ]]; then
        popd > /dev/null
        log_message "Error: APK build failed" >&2
        return $build_result
    fi

    # Find the output APK
    local apk_path="$APP_MODULE/build/outputs/apk/$BUILD_TYPE/${APP_MODULE}-${BUILD_TYPE}.apk"
    
    if [[ ! -f "$apk_path" ]]; then
        popd > /dev/null
        log_message "Error: APK not found at expected path" >&2
        return 1
    fi

    # Copy APK to output directory
    local output_apk="$OUTPUT_DIR/ScanMonitorApps-${VERSION_NAME}-${VERSION_CODE}.apk"
    cp "$apk_path" "$output_apk"
    
    popd > /dev/null

    log_message "APK build completed: $output_apk"
    return 0
}

# Build Android App Bundle (AAB)
build_aab() {
    log_message "Building Android App Bundle (AAB)..."

    # Change to Android directory
    pushd "$ANDROID_DIR" > /dev/null

    # Build the release bundle
    ./gradlew "$APP_MODULE:bundle${BUILD_TYPE^}" \
        -PversionName="$VERSION_NAME" \
        -PversionCode="$VERSION_CODE" \
        -PdatadogApiKey="$DATADOG_API_KEY" \
        -PdatadogAppKey="$DATADOG_APP_KEY" \
        --stacktrace
    
    local build_result=$?
    
    if [[ $build_result -ne 0 ]]; then
        popd > /dev/null
        log_message "Error: AAB build failed" >&2
        return $build_result
    fi

    # Find the output AAB
    local aab_path="$APP_MODULE/build/outputs/bundle/$BUILD_TYPE/${APP_MODULE}-${BUILD_TYPE}.aab"
    
    if [[ ! -f "$aab_path" ]]; then
        popd > /dev/null
        log_message "Error: AAB not found at expected path" >&2
        return 1
    fi

    # Copy AAB to output directory
    local output_aab="$OUTPUT_DIR/ScanMonitorApps-${VERSION_NAME}-${VERSION_CODE}.aab"
    cp "$aab_path" "$output_aab"
    
    popd > /dev/null

    log_message "AAB build completed: $output_aab"
    return 0
}

# Sign the APK with the release keystore
sign_apk() {
    local apk_path="$OUTPUT_DIR/ScanMonitorApps-${VERSION_NAME}-${VERSION_CODE}.apk"
    local aligned_apk="$OUTPUT_DIR/ScanMonitorApps-${VERSION_NAME}-${VERSION_CODE}-aligned.apk"
    local signed_apk="$OUTPUT_DIR/ScanMonitorApps-${VERSION_NAME}-${VERSION_CODE}-signed.apk"
    
    log_message "Signing APK..."

    # Find the latest build tools version
    local build_tools_dir="$ANDROID_HOME/build-tools"
    local latest_build_tools
    latest_build_tools=$(find "$build_tools_dir" -mindepth 1 -maxdepth 1 -type d | sort -V | tail -n 1)

    # Optimize the APK
    "$latest_build_tools/zipalign" -v -p 4 "$apk_path" "$aligned_apk"

    # Sign the APK
    "$latest_build_tools/apksigner" sign --ks "$KEYSTORE_PATH" \
        --ks-pass "pass:$KEYSTORE_PASSWORD" \
        --key-pass "pass:$KEY_PASSWORD" \
        --ks-key-alias "$KEY_ALIAS" \
        --out "$signed_apk" \
        "$aligned_apk"

    # Verify the signature
    "$latest_build_tools/apksigner" verify "$signed_apk"

    # Replace the original APK with the signed one
    mv "$signed_apk" "$apk_path"
    rm "$aligned_apk"

    log_message "APK signed successfully: $apk_path"
    return 0
}

# Generate build info file
generate_build_info() {
    log_message "Generating build info file..."

    local build_time
    build_time=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
    
    local git_commit
    git_commit=$(git -C "$PROJECT_ROOT" rev-parse HEAD 2>/dev/null || echo "unknown")
    
    local build_type="apk"
    if [[ "$BUILD_AAB" == "true" ]]; then
        build_type="aab"
    fi

    local build_info_file="$OUTPUT_DIR/build-info.json"
    
    # Create build info JSON
    cat > "$build_info_file" << EOF
{
    "appName": "ScanMonitorApps",
    "versionName": "$VERSION_NAME",
    "versionCode": $VERSION_CODE,
    "buildTime": "$build_time",
    "gitCommit": "$git_commit",
    "buildType": "$BUILD_TYPE",
    "artifactType": "$build_type"
}
EOF

    log_message "Build info generated: $build_info_file"
}

# Main function
main() {
    local status=0

    # Parse command-line arguments
    parse_arguments "$@" || { status=$?; print_usage; return $status; }

    # Display help if requested
    if [[ "$1" == "-h" ]]; then
        print_usage
        return 0
    fi

    log_message "Starting build process for ScanMonitorApps..."

    # Check environment
    check_environment || return $?

    # Setup build directory
    setup_build_directory

    # Extract or validate version information
    extract_version_info || return $?

    # Run tests
    run_tests || return $?

    # Build AAB or APK
    if [[ "$BUILD_AAB" == "true" ]]; then
        build_aab || return $?
    else
        build_apk || return $?
        sign_apk || return $?
    fi

    # Generate build info
    generate_build_info

    log_message "Build completed successfully!"
    return 0
}

# If script is being run directly, call main with all arguments
if [[ "${BASH_SOURCE[0]}" == "$0" ]]; then
    main "$@"
    exit $?
fi

# Export the main function for use in other scripts
export -f build_release