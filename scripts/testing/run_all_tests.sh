#!/bin/bash
# ScanMonitorApps Test Runner
# Script to run all tests for the ScanMonitorApps project

set -e

# Script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="${SCRIPT_DIR}/../../"

# Change to project root directory
cd "${PROJECT_ROOT}"

# Define usage function for displaying help
usage() {
    echo "ScanMonitorApps Test Runner"
    echo "Configuration for running all tests for the ScanMonitorApps project"
    echo ""
    echo "Usage: ./scripts/testing/run_all_tests.sh [options]"
    echo ""
    echo "Options:"
    echo "  --unit-only       Run only unit tests"
    echo "  --android-only    Run only Android instrumented tests"
    echo "  --lint-only       Run only lint checks"
    echo "  --with-coverage   Generate test coverage reports"
    echo "  --help            Show this help message"
    echo ""
    echo "Test reports will be available in the following locations:"
    echo "  Unit tests:       app/build/reports/tests/"
    echo "  Android tests:    app/build/reports/androidTests/"
    echo "  Lint checks:      app/build/reports/lint-results.html"
    echo ""
    exit 0
}

# Check if Android device is connected (for instrumented tests)
check_android_device() {
    echo "Checking for connected Android device..."
    if ! adb devices | grep -q "device$"; then
        echo "Error: No Android device connected. Connect a device or emulator for Android tests."
        exit 1
    fi
    echo "Android device found."
}

# Initialize variables
RUN_UNIT_TESTS=true
RUN_ANDROID_TESTS=true
RUN_LINT_CHECKS=true
GENERATE_COVERAGE=false

# Parse command-line arguments
while [[ $# -gt 0 ]]; do
    case "$1" in
        --unit-only)
            RUN_UNIT_TESTS=true
            RUN_ANDROID_TESTS=false
            RUN_LINT_CHECKS=false
            shift
            ;;
        --android-only)
            RUN_UNIT_TESTS=false
            RUN_ANDROID_TESTS=true
            RUN_LINT_CHECKS=false
            shift
            ;;
        --lint-only)
            RUN_UNIT_TESTS=false
            RUN_ANDROID_TESTS=false
            RUN_LINT_CHECKS=true
            shift
            ;;
        --with-coverage)
            GENERATE_COVERAGE=true
            shift
            ;;
        --help)
            usage
            ;;
        *)
            echo "Unknown option: $1"
            echo "Run with --help for usage information."
            exit 1
            ;;
    esac
done

# Header
echo "==================================================="
echo "         ScanMonitorApps Test Runner                "
echo "==================================================="
echo ""

# Run unit tests if enabled
if [ "$RUN_UNIT_TESTS" = true ]; then
    echo "==================================================="
    echo "Running Unit Tests..."
    echo "==================================================="
    if [ "$GENERATE_COVERAGE" = true ]; then
        echo "Generating test coverage report..."
        ./gradlew test jacocoTestReport
    else
        ./gradlew test
    fi
    echo "Unit tests completed."
    echo "Report available at: app/build/reports/tests/"
    echo ""
fi

# Run Android instrumented tests if enabled
if [ "$RUN_ANDROID_TESTS" = true ]; then
    echo "==================================================="
    echo "Running Android Instrumented Tests..."
    echo "==================================================="
    check_android_device
    ./gradlew connectedAndroidTest
    echo "Android tests completed."
    echo "Report available at: app/build/reports/androidTests/"
    echo ""
fi

# Run lint checks if enabled
if [ "$RUN_LINT_CHECKS" = true ]; then
    echo "==================================================="
    echo "Running Lint Checks..."
    echo "==================================================="
    ./gradlew lint
    echo "Lint checks completed."
    echo "Report available at: app/build/reports/lint-results.html"
    echo ""
fi

# Summary
echo "==================================================="
echo "                 Test Run Summary                   "
echo "==================================================="
echo "Tests executed:"
[ "$RUN_UNIT_TESTS" = true ] && echo "- Unit Tests: Complete"
[ "$RUN_ANDROID_TESTS" = true ] && echo "- Android Tests: Complete"
[ "$RUN_LINT_CHECKS" = true ] && echo "- Lint Checks: Complete"
echo ""
echo "All tests completed successfully!"
echo "==================================================="