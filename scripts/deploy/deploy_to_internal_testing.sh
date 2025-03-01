#!/bin/bash
# deploy_to_internal_testing.sh
#
# This script automates the deployment of the ScanMonitorApps Android application
# to Google Play Internal Testing track, allowing for controlled distribution to Jump staff.
#
# It handles the upload of the built Android App Bundle (AAB) to Google Play,
# assigns it to the Internal Testing track, and notifies the team about the deployment.

# Global variables
APP_DIR="$(dirname "$(dirname "$(dirname "$0"))")/src/android"
OUTPUT_DIR="${APP_DIR}/app/build/outputs/bundle/release"
LOG_FILE="$(dirname "$0")/logs/deploy_$(date +"%Y%m%d_%H%M%S").log"
SERVICE_ACCOUNT_KEY="$(dirname "$(dirname "$(dirname "$0"))")/infrastructure/config/play_store_service_account.json"
PACKAGE_NAME="com.jump.scanmonitor"

# ANSI color codes for logging
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check if all prerequisites are met before deployment
check_prerequisites() {
    local status=0

    # Check if the AAB file exists
    if [ ! -d "$OUTPUT_DIR" ]; then
        log_message "ERROR" "Build output directory not found: $OUTPUT_DIR"
        status=1
    else
        # Check if there's at least one AAB file
        if [ $(find "$OUTPUT_DIR" -name "*.aab" | wc -l) -eq 0 ]; then
            log_message "ERROR" "No AAB file found in $OUTPUT_DIR"
            status=1
        fi
    fi

    # Check if service account key exists
    if [ ! -f "$SERVICE_ACCOUNT_KEY" ]; then
        log_message "ERROR" "Google Play service account key not found: $SERVICE_ACCOUNT_KEY"
        status=1
    fi

    # Check if bundletool is installed
    if ! command -v bundletool &> /dev/null; then
        log_message "ERROR" "bundletool not found. Please install bundletool."
        status=1
    fi

    # Check if JAVA_HOME is set
    if [ -z "$JAVA_HOME" ]; then
        log_message "ERROR" "JAVA_HOME environment variable is not set"
        status=1
    fi

    # Check if Google Play Android Developer API is installed
    if ! pip list | grep -q "google-api-python-client"; then
        log_message "ERROR" "google-api-python-client not installed. Run: pip install google-api-python-client"
        status=1
    fi

    return $status
}

# Function to set up logging
setup_logging() {
    # Create logs directory if it doesn't exist
    mkdir -p "$(dirname "$LOG_FILE")"
    
    # Initialize log file with header
    echo "==== ScanMonitorApps Deployment Log - $(date) ====" > "$LOG_FILE"
    echo "Running on: $(hostname)" >> "$LOG_FILE"
    echo "User: $(whoami)" >> "$LOG_FILE"
    echo "====================================================" >> "$LOG_FILE"
    echo "" >> "$LOG_FILE"
    
    log_message "INFO" "Logging initialized: $LOG_FILE"
}

# Function to log messages to console and log file
log_message() {
    local level="$1"
    local message="$2"
    local timestamp=$(date +"%Y-%m-%d %H:%M:%S")
    
    # Format for log file
    echo "[$timestamp] [$level] $message" >> "$LOG_FILE"
    
    # Format for console with colors
    case "$level" in
        "INFO")
            echo -e "${BLUE}[$timestamp] [$level]${NC} $message"
            ;;
        "SUCCESS")
            echo -e "${GREEN}[$timestamp] [$level]${NC} $message"
            ;;
        "WARNING")
            echo -e "${YELLOW}[$timestamp] [$level]${NC} $message"
            ;;
        "ERROR")
            echo -e "${RED}[$timestamp] [$level]${NC} $message"
            ;;
        *)
            echo -e "[$timestamp] [$level] $message"
            ;;
    esac
}

# Function to deploy the app to internal testing
deploy_to_internal_testing() {
    local status=0
    
    log_message "INFO" "Starting deployment to Google Play Internal Testing track..."
    
    # Find the latest AAB file
    local latest_aab=$(find "$OUTPUT_DIR" -name "*.aab" -type f -printf '%T@ %p\n' | sort -nr | head -1 | cut -d' ' -f2-)
    
    if [ -z "$latest_aab" ]; then
        log_message "ERROR" "Could not find any AAB files to deploy"
        return 1
    fi
    
    log_message "INFO" "Found AAB file: $latest_aab"
    
    # Extract version from the AAB filename or use bundletool to get version
    local version_name=$(basename "$latest_aab" | grep -oP '(?<=app-release-)[0-9]+\.[0-9]+\.[0-9]+(?=\.aab)')
    
    if [ -z "$version_name" ]; then
        log_message "WARNING" "Could not extract version from filename, using 'latest' as version identifier"
        version_name="latest"
    fi
    
    log_message "INFO" "Deploying version: $version_name"
    
    # Create temporary directory for deployment
    local temp_dir="/tmp/scanmonitor_deploy_$(date +%s)"
    mkdir -p "$temp_dir"
    
    # Prepare for upload using Google Play API
    log_message "INFO" "Authenticating with Google Play..."
    
    # Using the Google Play Android Publisher API script
    local deploy_script="$temp_dir/deploy.py"
    
    cat > "$deploy_script" << EOF
#!/usr/bin/env python3
import os
import sys
from googleapiclient.discovery import build
from google.oauth2 import service_account
from googleapiclient.http import MediaFileUpload

def deploy_to_internal_testing():
    credentials = service_account.Credentials.from_service_account_file(
        '${SERVICE_ACCOUNT_KEY}',
        scopes=['https://www.googleapis.com/auth/androidpublisher']
    )
    
    service = build('androidpublisher', 'v3', credentials=credentials)
    
    # Upload the AAB file
    aab_file = '${latest_aab}'
    package_name = '${PACKAGE_NAME}'
    
    try:
        # Create an edit
        edit_request = service.edits().insert(body={}, packageName=package_name)
        result = edit_request.execute()
        edit_id = result['id']
        
        # Upload AAB
        media = MediaFileUpload(aab_file, mimetype='application/octet-stream')
        upload_request = service.edits().bundles().upload(
            editId=edit_id,
            packageName=package_name,
            media_body=media
        )
        upload_response = upload_request.execute()
        
        version_code = upload_response['versionCode']
        print(f"Uploaded AAB with version code: {version_code}")
        
        # Update track
        track_request = service.edits().tracks().update(
            editId=edit_id,
            packageName=package_name,
            track='internal',
            body={
                'releases': [{
                    'versionCodes': [version_code],
                    'status': 'completed',
                    'releaseNotes': [
                        {
                            'language': 'en-US',
                            'text': 'Test version for Jump staff. Please provide feedback.'
                        }
                    ]
                }]
            }
        )
        track_response = track_request.execute()
        
        # Commit changes
        commit_request = service.edits().commit(
            editId=edit_id,
            packageName=package_name
        )
        commit_response = commit_request.execute()
        
        print(f"App deployed to internal testing track. Edit committed: {commit_response['id']}")
        return 0
    except Exception as e:
        print(f"Error deploying to Google Play: {str(e)}")
        return 1

if __name__ == '__main__':
    sys.exit(deploy_to_internal_testing())
EOF
    
    chmod +x "$deploy_script"
    
    # Execute the deployment script
    log_message "INFO" "Uploading to Google Play..."
    python3 "$deploy_script" >> "$LOG_FILE" 2>&1
    
    if [ $? -eq 0 ]; then
        log_message "SUCCESS" "Successfully deployed to Google Play Internal Testing track!"
        status=0
    else
        log_message "ERROR" "Failed to deploy to Google Play. Check the log file for details."
        status=1
    fi
    
    # Clean up
    rm -rf "$temp_dir"
    
    return $status
}

# Function to notify the team about deployment
notify_team() {
    local deployment_status=$1
    
    log_message "INFO" "Preparing team notification..."
    
    if [ $deployment_status -eq 0 ]; then
        local subject="[SUCCESS] ScanMonitorApps deployed to Internal Testing"
        local message="ScanMonitorApps has been successfully deployed to Google Play Internal Testing track.\n\nTesters can access the app through the Google Play Store app using their registered test accounts.\n\nCheck your email for an invitation if you haven't received one yet.\n\nDeployment timestamp: $(date)"
    else
        local subject="[FAILED] ScanMonitorApps deployment failed"
        local message="ScanMonitorApps deployment to Google Play Internal Testing track has failed.\n\nPlease check the deployment logs for details.\n\nLog file: $LOG_FILE\n\nTimestamp: $(date)"
    fi
    
    # Send email notification
    log_message "INFO" "Sending notification email with subject: $subject"
    echo -e "$message" | mail -s "$subject" scanmonitor-team@jump.com
    
    # Send Slack notification if configured
    if [ -n "$SLACK_WEBHOOK_URL" ]; then
        log_message "INFO" "Sending Slack notification"
        curl -X POST -H 'Content-type: application/json' \
             --data "{\"text\":\"$subject\n$message\"}" \
             "$SLACK_WEBHOOK_URL"
    fi
    
    log_message "INFO" "Team notification completed"
}

# Function to perform cleanup after deployment
cleanup() {
    log_message "INFO" "Performing post-deployment cleanup..."
    
    # Compress logs older than 7 days
    find "$(dirname "$LOG_FILE")" -name "deploy_*.log" -type f -mtime +7 -exec gzip {} \;
    
    # Remove logs older than 30 days
    find "$(dirname "$LOG_FILE")" -name "deploy_*.log.gz" -type f -mtime +30 -delete
    
    # Update deployment status file
    echo "Last deployment: $(date)" > "$(dirname "$0")/last_deployment.txt"
    
    log_message "INFO" "Cleanup completed"
}

# Main execution
main() {
    setup_logging
    
    log_message "INFO" "Starting ScanMonitorApps deployment to Internal Testing"
    
    # Check prerequisites
    log_message "INFO" "Checking prerequisites..."
    if ! check_prerequisites; then
        log_message "ERROR" "Prerequisites check failed. Aborting deployment."
        notify_team 1
        exit 1
    fi
    
    # Deploy to internal testing
    deploy_to_internal_testing
    local deploy_status=$?
    
    # Notify team
    notify_team $deploy_status
    
    # Cleanup
    cleanup
    
    if [ $deploy_status -eq 0 ]; then
        log_message "SUCCESS" "Deployment process completed successfully"
        exit 0
    else
        log_message "ERROR" "Deployment process failed"
        exit 1
    fi
}

# Run the main function
main