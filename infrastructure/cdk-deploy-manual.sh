#!/bin/bash
# Manual CDK deploy for a single personal AWS account.
#
# Usage:
#   ./cdk-deploy-manual.sh <action> <stack> [region] [profile]
#
#   action : deploy (-d) | remove (-r) | diff (-f)
#   stack  : the CDK stack name to target
#   region : AWS region (default: eu-central-1)
#   profile: AWS CLI profile (default: personal-aws)

ACTION=$1
STACK=$2
AWS_REGION=${3:-eu-central-1}
PROFILE=${4:-personal-aws}

if [ $# -lt 2 ]; then
    echo 'Expecting at least 2 parameters:'
    echo '  deploy (-d) <stack> [region] [profile] : deploy a stack'
    echo '  remove (-r) <stack> [region] [profile] : remove a stack'
    echo '  diff   (-f) <stack> [region] [profile] : show stack diff'
    exit 1
fi

# The CloudFront certificate stack is hardcoded to us-east-1, so deploying it from any other
# region would only bootstrap the wrong region and leave us-east-1 unbootstrapped. Fail fast.
if [ "$STACK" == "SiteCertificateStack" ] && [ "$AWS_REGION" != "us-east-1" ]; then
    echo "SiteCertificateStack must be deployed to us-east-1. Pass us-east-1 as the region argument."
    exit 1
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

source "$DIR/aws-creds.sh" "$PROFILE" "$AWS_REGION"
if [ $? -ne 0 ]; then
    echo "Failed to set AWS credentials. Aborting."
    exit 1
fi

export CDK_DEFAULT_ACCOUNT="$AWS_ACCOUNT"
export CDK_DEFAULT_REGION="$AWS_REGION"
export AWS_REGION="$AWS_REGION"

echo "AWS Account: ${CDK_DEFAULT_ACCOUNT}"
echo "AWS Region:  ${AWS_REGION}"
echo "Stack:       ${STACK}"

APP_CONTEXT="-c REGION=$AWS_REGION -c STACK_NAME=$STACK"

if [ "$ACTION" == 'remove' ] || [ "$ACTION" == '-r' ]; then
    echo "Removing stack"
    "$DIR"/gradlew build
    cdk destroy $STACK $APP_CONTEXT
elif [ "$ACTION" == 'diff' ] || [ "$ACTION" == '-f' ]; then
    echo "Checking stack diff"
    "$DIR"/gradlew build
    cdk diff $STACK $APP_CONTEXT
elif [ "$ACTION" == 'deploy' ] || [ "$ACTION" == '-d' ]; then
    echo "Deploying stack"
    "$DIR"/gradlew build
    cdk bootstrap $APP_CONTEXT
    cdk deploy $STACK $APP_CONTEXT
else
    echo "No action executed"
fi
