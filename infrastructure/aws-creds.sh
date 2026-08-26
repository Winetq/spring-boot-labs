#!/bin/bash

# Personal single-account setup using a static IAM profile (Access Key + Secret).
# Configure the profile once with: aws configure --profile <profile_name>

profile_name=${1:-personal-aws}
region=${2:-eu-central-1}

if [[ $profile_name == "--help" ]]; then
    echo "Usage: source aws-creds.sh [profile_name] [region]"
    echo "Verifies the given AWS IAM profile and exports env variables for AWS auth."
    echo "Arguments:"
    echo "  profile_name  The AWS profile to use (default: personal-aws)."
    echo "  region        The AWS region to use (default: eu-central-1)."
    return 0
fi

check_logged_in=$(aws sts get-caller-identity --profile "$profile_name" --output json 2>/dev/null)
if [[ $? -ne 0 ]]; then
    echo -e "\033[91m*=>\033[93m Profile '$profile_name' is not configured or the keys are invalid. \033[0m"
    echo -e "\033[91m*=>\033[93m Run: aws configure --profile $profile_name \033[0m"
    return 1
fi

echo -e "\033[94m*=>\033[93m Using AWS profile '$profile_name' in region '$region' \033[0m"

export AWS_PROFILE="$profile_name"
export AWS_DEFAULT_REGION="$region"
export AWS_REGION="$region"

# Parse JSON output to extract UserId, Account, and Arn
USER_ID=$(echo "$check_logged_in" | jq -r '.UserId')
ACCOUNT=$(echo "$check_logged_in" | jq -r '.Account')
ARN=$(echo "$check_logged_in" | jq -r '.Arn')

# Set additional environment variables
export AWS_USER_ID=$USER_ID
export AWS_ACCOUNT=$ACCOUNT
export AWS_METADATA_USER_ARN=$ARN

echo -e "\033[94m*=>\033[93m Environment variables set: \033[0m"
echo -e "\033[94m*=>\033[93m    AWS_PROFILE=$AWS_PROFILE \033[0m"
echo -e "\033[94m*=>\033[93m    AWS_REGION=$AWS_REGION \033[0m"
echo -e "\033[94m*=>\033[93m    AWS_ACCOUNT=$AWS_ACCOUNT \033[0m"
echo -e "\033[94m*=>\033[93m    AWS_USER_ID=$AWS_USER_ID \033[0m"
echo -e "\033[94m*=>\033[93m    AWS_METADATA_USER_ARN=$AWS_METADATA_USER_ARN \033[0m"
