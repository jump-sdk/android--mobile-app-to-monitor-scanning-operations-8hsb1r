#!/bin/bash
#
# ScanMonitorApps Development Environment Setup Script
# Version: 1.0.0
#
# This script automates the setup of a complete development environment
# for the ScanMonitorApps Android application. It handles the installation
# and configuration of all required tools, dependencies, and API keys.
#

# Set script variables
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
ANDROID_DIR="${PROJECT_ROOT}/src/android"
LOG_FILE="${PROJECT_ROOT}/setup_logs.txt"
MIN_JAVA_VERSION="17"
REQUIRED_ANDROID_SDK_PACKAGES=(
  "platform-tools"
  "platforms;android-24"
  "platforms;android-33"
  "build-tools;33.0.2"
)

# Function to display a welcome banner
print_banner() {
  echo "
   _____                 __  __             _ _              ___              
  / ____|               |  \/  |           (_) |            / _ \             
 | (___   ___ __ _ _ __ | \  / | ___  _ __  _| |_ ___  _ __| | | |_ __  _ __  
  \___ \ / __/ _\` | '_ \| |\/| |/ _ \| '_ \| | __/ _ \| '__| | | | '_ \| '_ \ 
  ____) | (_| (_| | | | | |  | | (_) | | | | | || (_) | |  | |_| | |_) | |_) |
 |_____/ \___\__,_|_| |_|_|  |_|\___/|_| |_|_|\__\___/|_|   \___/| .__/| .__/ 
                                                                  | |   | |    
                                                                  |_|   |_|    
 Development Environment Setup Script (v1.0.0)
 -----------------------------------------------------------------
 This script will set up your development environment for the 
 ScanMonitorApps Android application. It will:
 
  - Check and setup Java JDK 17+
  - Setup Android SDK and required components
  - Provide instructions for Android Studio installation
  - Configure environment variables
  - Set up Datadog API keys
  - Guide through Firebase setup
  - Configure Git hooks
 
 Press Enter to continue or Ctrl+C to exit.
"
  read -r
}

# Function to log message to both console and log file
log() {
  local message="$1"
  local level="${2:-INFO}"
  local timestamp
  timestamp=$(date "+%Y-%m-%d %H:%M:%S")
  local formatted_message="[${timestamp}] [${level}] ${message}"
  
  # Color output based on log level
  case "${level}" in
    INFO)
      echo -e "\033[0;32m${formatted_message}\033[0m"
      ;;
    WARNING)
      echo -e "\033[0;33m${formatted_message}\033[0m"
      ;;
    ERROR)
      echo -e "\033[0;31m${formatted_message}\033[0m"
      ;;
    *)
      echo "${formatted_message}"
      ;;
  esac
  
  # Append to log file
  echo "${formatted_message}" >> "${LOG_FILE}"
}

# Function to check prerequisites
check_prerequisites() {
  log "Checking prerequisites..."
  
  # Check bash
  if [ -z "${BASH_VERSION}" ]; then
    log "This script requires bash to run." "ERROR"
    return 1
  fi
  
  # Check Java
  if ! command -v java &> /dev/null; then
    log "Java is not installed." "WARNING"
  else
    local java_version
    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    log "Java version ${java_version} detected."
    # Check Java version
    if [[ "${java_version}" != *"1.${MIN_JAVA_VERSION}"* ]] && [[ "${java_version}" != "${MIN_JAVA_VERSION}"* ]]; then
      log "Java ${MIN_JAVA_VERSION}+ is required. Found version ${java_version}." "WARNING"
    else
      log "Java version is compatible."
    fi
  fi
  
  # Check git
  if ! command -v git &> /dev/null; then
    log "Git is not installed." "WARNING"
    return 1
  fi
  
  # Check curl
  if ! command -v curl &> /dev/null; then
    log "curl is not installed." "WARNING"
    return 1
  fi
  
  # Check unzip
  if ! command -v unzip &> /dev/null; then
    log "unzip is not installed." "WARNING"
    return 1
  fi
  
  log "Prerequisites check completed."
  return 0
}

# Function to set up Java
setup_java() {
  log "Setting up Java Development Kit..."
  
  # Check if Java is already installed and meets minimum version
  if command -v java &> /dev/null; then
    local java_version
    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    if [[ "${java_version}" == *"1.${MIN_JAVA_VERSION}"* ]] || [[ "${java_version}" == "${MIN_JAVA_VERSION}"* ]]; then
      log "Java ${MIN_JAVA_VERSION}+ is already installed (${java_version})."
      return 0
    else
      log "Java ${MIN_JAVA_VERSION}+ is required. Found version ${java_version}." "WARNING"
    fi
  else
    log "Java is not installed." "WARNING"
  fi
  
  # Detect operating system
  local os
  if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    os="linux"
  elif [[ "$OSTYPE" == "darwin"* ]]; then
    os="macos"
  elif [[ "$OSTYPE" == "cygwin" ]] || [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
    os="windows"
  else
    log "Unsupported operating system: $OSTYPE" "ERROR"
    return 1
  fi
  
  # Provide OS-specific instructions
  case "${os}" in
    linux)
      log "For Linux, please install JDK 17 using your package manager."
      log "For Ubuntu/Debian: sudo apt-get update && sudo apt-get install openjdk-17-jdk"
      log "For Fedora/CentOS: sudo dnf install java-17-openjdk-devel"
      log "After installation, please run this script again."
      ;;
    macos)
      log "For macOS, please install JDK 17 using Homebrew:"
      log "1. Install Homebrew if not installed: /bin/bash -c \"$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\""
      log "2. Install JDK 17: brew install --cask temurin17"
      log "After installation, please run this script again."
      ;;
    windows)
      log "For Windows, please download and install JDK 17 from:"
      log "https://adoptium.net/temurin/releases/?version=17"
      log "After installation, please run this script again."
      ;;
  esac
  
  # Ask user to confirm installation
  echo ""
  log "Have you installed JDK 17 as instructed? (y/n)"
  read -r confirm
  if [[ "${confirm}" != "y" && "${confirm}" != "Y" ]]; then
    log "Java setup aborted. Please install JDK 17 and run the script again." "ERROR"
    return 1
  fi
  
  # Verify Java installation
  if command -v java &> /dev/null; then
    local java_version
    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    if [[ "${java_version}" == *"1.${MIN_JAVA_VERSION}"* ]] || [[ "${java_version}" == "${MIN_JAVA_VERSION}"* ]]; then
      log "Java ${MIN_JAVA_VERSION}+ is now installed (${java_version})."
      
      # Configure JAVA_HOME
      if [[ -z "${JAVA_HOME}" ]]; then
        local java_path
        java_path=$(which java)
        java_path=$(dirname "$(dirname "$(readlink -f "${java_path}")")")
        log "Setting JAVA_HOME to ${java_path}"
        export JAVA_HOME="${java_path}"
      else
        log "JAVA_HOME is already set to ${JAVA_HOME}"
      fi
      
      return 0
    else
      log "Java ${MIN_JAVA_VERSION}+ is required. Found version ${java_version}." "ERROR"
      return 1
    fi
  else
    log "Java installation failed." "ERROR"
    return 1
  fi
}

# Function to set up Android SDK
setup_android_sdk() {
  log "Setting up Android SDK..."
  
  # Check if ANDROID_HOME or ANDROID_SDK_ROOT is already set
  if [[ -n "${ANDROID_HOME}" ]]; then
    log "ANDROID_HOME is already set to ${ANDROID_HOME}"
    export ANDROID_SDK_ROOT="${ANDROID_HOME}"
  elif [[ -n "${ANDROID_SDK_ROOT}" ]]; then
    log "ANDROID_SDK_ROOT is already set to ${ANDROID_SDK_ROOT}"
    export ANDROID_HOME="${ANDROID_SDK_ROOT}"
  else
    # Install Android SDK Command Line Tools
    local download_url
    local sdk_dir="${HOME}/Android/Sdk"
    local tmp_dir="/tmp/android_sdk"
    
    # Create temporary directory
    mkdir -p "${tmp_dir}"
    
    # Detect operating system
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
      download_url="https://dl.google.com/android/repository/commandlinetools-linux-8512546_latest.zip"
    elif [[ "$OSTYPE" == "darwin"* ]]; then
      download_url="https://dl.google.com/android/repository/commandlinetools-mac-8512546_latest.zip"
    elif [[ "$OSTYPE" == "cygwin" ]] || [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
      download_url="https://dl.google.com/android/repository/commandlinetools-win-8512546_latest.zip"
    else
      log "Unsupported operating system: $OSTYPE" "ERROR"
      return 1
    fi
    
    log "Downloading Android SDK from ${download_url}..."
    if ! curl -fsSL "${download_url}" -o "${tmp_dir}/sdk.zip"; then
      log "Failed to download Android SDK." "ERROR"
      return 1
    fi
    
    log "Extracting Android SDK..."
    if ! unzip -q "${tmp_dir}/sdk.zip" -d "${tmp_dir}"; then
      log "Failed to extract Android SDK." "ERROR"
      return 1
    fi
    
    # Create appropriate directory structure
    mkdir -p "${sdk_dir}/cmdline-tools"
    mv "${tmp_dir}/cmdline-tools" "${sdk_dir}/cmdline-tools/latest"
    
    # Set environment variables
    export ANDROID_HOME="${sdk_dir}"
    export ANDROID_SDK_ROOT="${sdk_dir}"
    export PATH="${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools"
    
    log "Android SDK installed to ${sdk_dir}"
  fi
  
  # Accept SDK licenses
  log "Accepting Android SDK licenses..."
  yes | "${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager" --licenses > /dev/null
  
  # Install required SDK components
  log "Installing required Android SDK components..."
  for package in "${REQUIRED_ANDROID_SDK_PACKAGES[@]}"; do
    log "Installing ${package}..."
    "${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager" "${package}" > /dev/null
  done
  
  # Verify installation
  log "Verifying Android SDK components installation..."
  local installed_packages
  installed_packages=$("${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager" --list_installed)
  for package in "${REQUIRED_ANDROID_SDK_PACKAGES[@]}"; do
    if ! echo "${installed_packages}" | grep -q "${package}"; then
      log "Package ${package} is not installed." "ERROR"
      return 1
    fi
  done
  
  log "Android SDK setup completed successfully."
  return 0
}

# Function to provide instructions for installing Android Studio
setup_android_studio() {
  log "Setting up Android Studio..."
  
  # Detect operating system
  local os
  if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    os="linux"
  elif [[ "$OSTYPE" == "darwin"* ]]; then
    os="macos"
  elif [[ "$OSTYPE" == "cygwin" ]] || [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]]; then
    os="windows"
  else
    log "Unsupported operating system: $OSTYPE" "ERROR"
    return 1
  fi
  
  log "Please download and install Android Studio Hedgehog (2023.1.1+) from:"
  log "https://developer.android.com/studio"
  
  # Provide OS-specific instructions
  case "${os}" in
    linux)
      log "For Linux:"
      log "1. Extract the downloaded archive"
      log "2. Navigate to the android-studio/bin directory"
      log "3. Run ./studio.sh"
      log "4. Follow the setup wizard"
      ;;
    macos)
      log "For macOS:"
      log "1. Open the downloaded DMG file"
      log "2. Drag Android Studio to the Applications folder"
      log "3. Open Android Studio from Applications"
      log "4. Follow the setup wizard"
      ;;
    windows)
      log "For Windows:"
      log "1. Run the downloaded installer"
      log "2. Follow the installation wizard"
      log "3. Launch Android Studio after installation"
      log "4. Follow the setup wizard"
      ;;
  esac
  
  log "During first-time setup:"
  log "1. Choose 'Standard' installation type"
  log "2. Select the UI theme you prefer"
  log "3. Wait for component downloads to complete"
  
  # Ask user to confirm installation
  echo ""
  log "Have you installed Android Studio as instructed? (y/n)"
  read -r confirm
  if [[ "${confirm}" != "y" && "${confirm}" != "Y" ]]; then
    log "Android Studio setup skipped. You can install it later." "WARNING"
    return 0
  fi
  
  log "Android Studio setup completed."
  return 0
}

# Function to set up environment variables
setup_environment_variables() {
  log "Setting up environment variables..."
  
  # Determine shell configuration file
  local shell_config_file
  local shell_type="${SHELL##*/}"
  
  case "${shell_type}" in
    bash)
      if [[ -f "${HOME}/.bash_profile" ]]; then
        shell_config_file="${HOME}/.bash_profile"
      else
        shell_config_file="${HOME}/.bashrc"
      fi
      ;;
    zsh)
      shell_config_file="${HOME}/.zshrc"
      ;;
    *)
      log "Unsupported shell: ${shell_type}" "WARNING"
      log "Please manually add environment variables to your shell configuration file."
      return 1
      ;;
  esac
  
  log "Using shell configuration file: ${shell_config_file}"
  
  # Backup the config file
  cp "${shell_config_file}" "${shell_config_file}.bak"
  log "Created backup of ${shell_config_file} to ${shell_config_file}.bak"
  
  # Check and add JAVA_HOME if needed
  if ! grep -q "JAVA_HOME" "${shell_config_file}"; then
    echo "" >> "${shell_config_file}"
    echo "# Java configuration for ScanMonitorApps" >> "${shell_config_file}"
    echo "export JAVA_HOME=\"${JAVA_HOME}\"" >> "${shell_config_file}"
    log "Added JAVA_HOME to ${shell_config_file}"
  fi
  
  # Check and add ANDROID_HOME if needed
  if ! grep -q "ANDROID_HOME" "${shell_config_file}"; then
    echo "" >> "${shell_config_file}"
    echo "# Android SDK configuration for ScanMonitorApps" >> "${shell_config_file}"
    echo "export ANDROID_HOME=\"${ANDROID_HOME}\"" >> "${shell_config_file}"
    echo "export ANDROID_SDK_ROOT=\"${ANDROID_HOME}\"" >> "${shell_config_file}"
    echo "export PATH=\"\${PATH}:\${ANDROID_HOME}/cmdline-tools/latest/bin:\${ANDROID_HOME}/platform-tools\"" >> "${shell_config_file}"
    log "Added Android SDK environment variables to ${shell_config_file}"
  fi
  
  log "Environment variables have been set up."
  log "Please run 'source ${shell_config_file}' to apply the changes to your current session."
  
  return 0
}

# Function to configure Datadog API keys
configure_datadog_keys() {
  log "Setting up Datadog API keys..."
  
  # Check if local.properties exists
  local local_properties="${ANDROID_DIR}/local.properties"
  if [[ ! -f "${local_properties}" ]]; then
    touch "${local_properties}"
    log "Created ${local_properties}"
  fi
  
  # Ask user if they have Datadog API keys
  log "Do you have Datadog API keys? (y/n)"
  read -r has_keys
  
  local api_key
  local app_key
  
  if [[ "${has_keys}" == "y" || "${has_keys}" == "Y" ]]; then
    # Prompt for API key
    log "Please enter your Datadog API key:"
    read -r api_key
    
    # Prompt for App key
    log "Please enter your Datadog Application key:"
    read -r app_key
  else
    # Generate placeholder keys for development
    log "Using placeholder keys for development. These are not valid for API access."
    api_key="dev_api_key_placeholder"
    app_key="dev_app_key_placeholder"
  fi
  
  # Add keys to local.properties
  if grep -q "DATADOG_API_KEY" "${local_properties}"; then
    sed -i.bak "s/DATADOG_API_KEY=.*/DATADOG_API_KEY=${api_key}/" "${local_properties}"
  else
    echo "DATADOG_API_KEY=${api_key}" >> "${local_properties}"
  fi
  
  if grep -q "DATADOG_APP_KEY" "${local_properties}"; then
    sed -i.bak "s/DATADOG_APP_KEY=.*/DATADOG_APP_KEY=${app_key}/" "${local_properties}"
  else
    echo "DATADOG_APP_KEY=${app_key}" >> "${local_properties}"
  fi
  
  log "Datadog API keys configured in ${local_properties}"
  
  # Remind users about secure keys in production
  log "Note: For production use, please ensure you use valid Datadog API keys."
  log "The keys are stored in local.properties which is excluded from version control."
  
  return 0
}

# Function to guide user through Firebase setup
setup_firebase() {
  log "Setting up Firebase for monitoring, analytics, and crash reporting..."
  
  # Ask if user wants to set up Firebase now
  log "Do you want to set up Firebase for this application now? (y/n)"
  read -r setup_now
  
  if [[ "${setup_now}" != "y" && "${setup_now}" != "Y" ]]; then
    log "Firebase setup skipped. You can set it up later."
    return 0
  fi
  
  log "Firebase is used for the following purposes in this application:"
  log "- Crashlytics for crash reporting"
  log "- Performance Monitoring for app performance tracking"
  log "- Analytics for usage statistics"
  
  log "To set up Firebase, please follow these steps:"
  log "1. Go to https://console.firebase.google.com/"
  log "2. Click 'Add project' and create a new Firebase project"
  log "3. Name your project (e.g., ScanMonitorApps-Dev)"
  log "4. Configure Google Analytics if prompted"
  log "5. Wait for project creation to complete"
  log "6. Click 'Android' to add an Android app to your project"
  log "7. Enter package name: com.jump.scanmonitor"
  log "8. Enter app nickname: ScanMonitorApps"
  log "9. Register the app"
  log "10. Download the google-services.json file"
  log "11. Place the google-services.json file in the src/android/app directory"
  
  # Wait for user to complete Firebase setup
  log "Have you completed the Firebase setup and placed google-services.json in the src/android/app directory? (y/n)"
  read -r firebase_complete
  
  if [[ "${firebase_complete}" != "y" && "${firebase_complete}" != "Y" ]]; then
    log "Firebase setup not completed. You can complete it later." "WARNING"
    return 1
  fi
  
  # Verify google-services.json
  if [[ ! -f "${ANDROID_DIR}/app/google-services.json" ]]; then
    log "google-services.json not found in ${ANDROID_DIR}/app/" "ERROR"
    log "Please download the file from Firebase console and place it in the correct location."
    return 1
  fi
  
  log "Firebase setup completed successfully."
  return 0
}

# Function to validate project setup
validate_project_setup() {
  log "Validating project setup..."
  
  # Check if Android project directory exists
  if [[ ! -d "${ANDROID_DIR}" ]]; then
    log "Android project directory not found at ${ANDROID_DIR}" "ERROR"
    return 1
  fi
  
  # Navigate to Android project directory
  cd "${ANDROID_DIR}" || {
    log "Failed to navigate to ${ANDROID_DIR}" "ERROR"
    return 1
  }
  
  # Try running a simple Gradle task to validate Gradle setup
  log "Validating Gradle setup..."
  if ! ./gradlew --version > /dev/null 2>&1; then
    log "Gradle setup validation failed. Please check your installation." "ERROR"
    return 1
  fi
  
  # Check for critical configuration files
  log "Checking for critical configuration files..."
  
  # Check local.properties
  if [[ ! -f "${ANDROID_DIR}/local.properties" ]]; then
    log "local.properties not found in ${ANDROID_DIR}/" "ERROR"
    return 1
  fi
  
  # Check if local.properties contains required keys
  if ! grep -q "DATADOG_API_KEY" "${ANDROID_DIR}/local.properties" || ! grep -q "DATADOG_APP_KEY" "${ANDROID_DIR}/local.properties"; then
    log "Datadog API keys not found in local.properties" "ERROR"
    return 1
  fi
  
  # Check for Firebase configuration if selected
  if [[ -f "${ANDROID_DIR}/app/google-services.json" ]]; then
    log "Firebase configuration found."
  else
    log "Firebase configuration not found. This is optional but recommended." "WARNING"
  fi
  
  log "Project setup validation completed successfully."
  return 0
}

# Function to set up Git hooks
setup_git_hooks() {
  log "Setting up Git hooks..."
  
  # Navigate to project root
  cd "${PROJECT_ROOT}" || {
    log "Failed to navigate to ${PROJECT_ROOT}" "ERROR"
    return 1
  }
  
  # Create hooks directory if it doesn't exist
  mkdir -p "${PROJECT_ROOT}/.git/hooks"
  
  # Create pre-commit hook
  local pre_commit_hook="${PROJECT_ROOT}/.git/hooks/pre-commit"
  cat > "${pre_commit_hook}" << 'EOF'
#!/bin/bash

echo "Running pre-commit checks..."

# Run detekt
echo "Running detekt..."
cd src/android
./gradlew detekt --daemon

# Check the exit code
if [ $? -ne 0 ]; then
    echo "Detekt found issues. Please fix them before committing."
    exit 1
fi

echo "Pre-commit checks passed."
exit 0
EOF
  
  # Create pre-push hook
  local pre_push_hook="${PROJECT_ROOT}/.git/hooks/pre-push"
  cat > "${pre_push_hook}" << 'EOF'
#!/bin/bash

echo "Running pre-push checks..."

# Run tests
echo "Running tests..."
cd src/android
./gradlew test --daemon

# Check the exit code
if [ $? -ne 0 ]; then
    echo "Tests failed. Please fix the failing tests before pushing."
    exit 1
fi

echo "Pre-push checks passed."
exit 0
EOF
  
  # Make hooks executable
  chmod +x "${pre_commit_hook}" "${pre_push_hook}"
  
  log "Git hooks set up successfully."
  return 0
}

# Function to show next steps
show_next_steps() {
  local border="=============================================================="
  
  echo ""
  echo "${border}"
  echo "                   SETUP COMPLETED SUCCESSFULLY!                   "
  echo "${border}"
  echo ""
  log "Your development environment for ScanMonitorApps is now set up."
  log "Here are the next steps:"
  echo ""
  log "1. Open the project in Android Studio:"
  log "   - Launch Android Studio"
  log "   - Select 'Open an existing Android Studio project'"
  log "   - Navigate to: ${ANDROID_DIR}"
  log "   - Click 'Open'"
  echo ""
  log "2. Build the project:"
  log "   - Wait for Gradle synchronization to complete"
  log "   - Click 'Build > Make Project' or press Ctrl+F9 (Cmd+F9 on macOS)"
  echo ""
  log "3. Run the application:"
  log "   - Connect an Android device or start an emulator"
  log "   - Click 'Run > Run app' or press Shift+F10 (Ctrl+R on macOS)"
  echo ""
  log "4. Check the documentation for more information:"
  log "   - README.md in the project root directory"
  log "   - docs/ directory for detailed documentation"
  echo ""
  log "If you encounter any issues, please check the setup log file:"
  log "${LOG_FILE}"
  echo ""
  echo "${border}"
}

# Function to clean up temporary files
cleanup() {
  log "Cleaning up temporary files..."
  
  # Remove temporary Android SDK download if exists
  if [[ -d "/tmp/android_sdk" ]]; then
    rm -rf "/tmp/android_sdk"
  fi
  
  # Remove backup files
  find "${ANDROID_DIR}" -name "*.bak" -type f -delete
  
  log "Cleanup completed."
}

# Main function to orchestrate the setup process
main() {
  # Print welcome banner
  print_banner
  
  # Create log file
  echo "" > "${LOG_FILE}"
  log "Setup started at $(date)"
  
  # Check prerequisites
  if ! check_prerequisites; then
    log "Prerequisites check failed. Please install required tools and try again." "ERROR"
    return 1
  fi
  
  # Setup Java if needed
  if ! setup_java; then
    log "Java setup failed. Please check the log for details." "ERROR"
    return 1
  fi
  
  # Setup Android SDK if needed
  if ! setup_android_sdk; then
    log "Android SDK setup failed. Please check the log for details." "ERROR"
    return 1
  fi
  
  # Provide Android Studio installation instructions
  if ! setup_android_studio; then
    log "Android Studio setup guidance failed. Please check the log for details." "WARNING"
    # Don't return error as this is optional
  fi
  
  # Setup environment variables
  if ! setup_environment_variables; then
    log "Environment variables setup failed. Please check the log for details." "WARNING"
    # Don't return error as user might set them manually
  fi
  
  # Configure Datadog API keys
  if ! configure_datadog_keys; then
    log "Datadog API keys configuration failed. Please check the log for details." "ERROR"
    return 1
  fi
  
  # Offer Firebase setup assistance
  if ! setup_firebase; then
    log "Firebase setup not completed. This is optional but recommended for monitoring." "WARNING"
    # Don't return error as Firebase is optional
  fi
  
  # Validate project setup
  if ! validate_project_setup; then
    log "Project setup validation failed. Please check the log for details." "ERROR"
    return 1
  fi
  
  # Setup Git hooks
  if ! setup_git_hooks; then
    log "Git hooks setup failed. Please check the log for details." "WARNING"
    # Don't return error as this is optional
  fi
  
  # Show next steps
  show_next_steps
  
  # Cleanup
  cleanup
  
  log "Setup completed successfully."
  return 0
}

# Execute main function
main
exit $?