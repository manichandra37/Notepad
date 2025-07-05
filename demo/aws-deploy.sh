#!/bin/bash

# AWS Deployment Script for Notepad Backend
# This script provides multiple deployment options for AWS

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="notepad-backend"
REGION="us-east-1"
ECR_REPOSITORY="notepad-backend"
CLUSTER_NAME="notepad-cluster"
SERVICE_NAME="notepad-service"

echo -e "${BLUE}🚀 AWS Deployment Script for Notepad Backend${NC}"
echo "=================================================="

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo -e "${RED}❌ AWS CLI is not installed. Please install it first.${NC}"
    exit 1
fi

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker is not installed. Please install it first.${NC}"
    exit 1
fi

# Function to build and push Docker image
build_and_push_image() {
    echo -e "${YELLOW}📦 Building Docker image...${NC}"
    docker build -t $APP_NAME .
    
    echo -e "${YELLOW}🔐 Logging into ECR...${NC}"
    aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com
    
    echo -e "${YELLOW}🏷️ Tagging image...${NC}"
    docker tag $APP_NAME:latest $AWS_ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/$ECR_REPOSITORY:latest
    
    echo -e "${YELLOW}📤 Pushing image to ECR...${NC}"
    docker push $AWS_ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/$ECR_REPOSITORY:latest
    
    echo -e "${GREEN}✅ Docker image pushed successfully!${NC}"
}

# Function to deploy to ECS
deploy_to_ecs() {
    echo -e "${BLUE}🐳 Deploying to Amazon ECS...${NC}"
    
    # Create ECR repository if it doesn't exist
    aws ecr describe-repositories --repository-names $ECR_REPOSITORY --region $REGION 2>/dev/null || \
    aws ecr create-repository --repository-name $ECR_REPOSITORY --region $REGION
    
    # Build and push image
    build_and_push_image
    
    # Create ECS cluster
    aws ecs create-cluster --cluster-name $CLUSTER_NAME --region $REGION 2>/dev/null || echo "Cluster already exists"
    
    # Create task definition
    cat > task-definition.json << EOF
{
    "family": "$APP_NAME",
    "networkMode": "awsvpc",
    "requiresCompatibilities": ["FARGATE"],
    "cpu": "256",
    "memory": "512",
    "executionRoleArn": "arn:aws:iam::$AWS_ACCOUNT_ID:role/ecsTaskExecutionRole",
    "containerDefinitions": [
        {
            "name": "$APP_NAME",
            "image": "$AWS_ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/$ECR_REPOSITORY:latest",
            "portMappings": [
                {
                    "containerPort": 8080,
                    "protocol": "tcp"
                }
            ],
            "environment": [
                {
                    "name": "SPRING_PROFILES_ACTIVE",
                    "value": "prod"
                },
                {
                    "name": "DATABASE_URL",
                    "value": "$DATABASE_URL"
                },
                {
                    "name": "DATABASE_USERNAME",
                    "value": "$DATABASE_USERNAME"
                },
                {
                    "name": "DATABASE_PASSWORD",
                    "value": "$DATABASE_PASSWORD"
                }
            ],
            "logConfiguration": {
                "logDriver": "awslogs",
                "options": {
                    "awslogs-group": "/ecs/$APP_NAME",
                    "awslogs-region": "$REGION",
                    "awslogs-stream-prefix": "ecs"
                }
            }
        }
    ]
}
EOF
    
    # Register task definition
    aws ecs register-task-definition --cli-input-json file://task-definition.json --region $REGION
    
    # Create service
    aws ecs create-service \
        --cluster $CLUSTER_NAME \
        --service-name $SERVICE_NAME \
        --task-definition $APP_NAME \
        --desired-count 1 \
        --launch-type FARGATE \
        --network-configuration "awsvpcConfiguration={subnets=[$SUBNET_ID],securityGroups=[$SECURITY_GROUP_ID],assignPublicIp=ENABLED}" \
        --region $REGION 2>/dev/null || echo "Service already exists"
    
    echo -e "${GREEN}✅ ECS deployment completed!${NC}"
}

# Function to deploy to EC2
deploy_to_ec2() {
    echo -e "${BLUE}🖥️ Deploying to Amazon EC2...${NC}"
    
    # Build and push image
    build_and_push_image
    
    # Create user data script
    cat > user-data.sh << 'EOF'
#!/bin/bash
yum update -y
yum install -y docker
systemctl start docker
systemctl enable docker
usermod -a -G docker ec2-user

# Install Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# Create app directory
mkdir -p /opt/notepad-app
cd /opt/notepad-app

# Create docker-compose.yml
cat > docker-compose.yml << 'DOCKER_COMPOSE_EOF'
version: '3.8'
services:
  app:
    image: $AWS_ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/$ECR_REPOSITORY:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_URL=$DATABASE_URL
      - DATABASE_USERNAME=$DATABASE_USERNAME
      - DATABASE_PASSWORD=$DATABASE_PASSWORD
    restart: unless-stopped
DOCKER_COMPOSE_EOF

# Start the application
docker-compose up -d
EOF
    
    echo -e "${GREEN}✅ EC2 deployment script created!${NC}"
    echo -e "${YELLOW}📝 Use the user-data.sh script when launching your EC2 instance${NC}"
}

# Function to deploy to Elastic Beanstalk
deploy_to_elastic_beanstalk() {
    echo -e "${BLUE}🌱 Deploying to AWS Elastic Beanstalk...${NC}"
    
    # Create .ebextensions directory
    mkdir -p .ebextensions
    
    # Create environment configuration
    cat > .ebextensions/environment.config << EOF
option_settings:
  aws:elasticbeanstalk:application:environment:
    SPRING_PROFILES_ACTIVE: prod
    DATABASE_URL: $DATABASE_URL
    DATABASE_USERNAME: $DATABASE_USERNAME
    DATABASE_PASSWORD: $DATABASE_PASSWORD
  aws:elasticbeanstalk:container:java:
    Xms: 256m
    Xmx: 512m
EOF
    
    # Create Procfile
    echo "web: java -jar target/demo-0.0.1-SNAPSHOT.jar" > Procfile
    
    # Initialize EB application
    eb init $APP_NAME --platform java --region $REGION 2>/dev/null || echo "EB application already initialized"
    
    # Create environment
    eb create notepad-prod --instance-type t3.small --envvars SPRING_PROFILES_ACTIVE=prod 2>/dev/null || echo "Environment already exists"
    
    echo -e "${GREEN}✅ Elastic Beanstalk deployment completed!${NC}"
}

# Main menu
echo -e "${YELLOW}Choose your deployment option:${NC}"
echo "1) Deploy to Amazon ECS (Fargate)"
echo "2) Deploy to Amazon EC2"
echo "3) Deploy to Elastic Beanstalk"
echo "4) Build and push Docker image only"
echo "5) Exit"

read -p "Enter your choice (1-5): " choice

# Get AWS account ID
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

# Get environment variables
echo -e "${YELLOW}Please provide the following information:${NC}"
read -p "Database URL (e.g., jdbc:postgresql://your-rds-endpoint:5432/notepaddb): " DATABASE_URL
read -p "Database Username: " DATABASE_USERNAME
read -p "Database Password: " DATABASE_PASSWORD

case $choice in
    1)
        echo -e "${YELLOW}For ECS deployment, you'll need:${NC}"
        echo "- VPC Subnet ID"
        echo "- Security Group ID"
        read -p "Subnet ID: " SUBNET_ID
        read -p "Security Group ID: " SECURITY_GROUP_ID
        deploy_to_ecs
        ;;
    2)
        deploy_to_ec2
        ;;
    3)
        deploy_to_elastic_beanstalk
        ;;
    4)
        build_and_push_image
        ;;
    5)
        echo -e "${GREEN}Goodbye! 👋${NC}"
        exit 0
        ;;
    *)
        echo -e "${RED}Invalid choice. Please run the script again.${NC}"
        exit 1
        ;;
esac

echo -e "${GREEN}🎉 Deployment completed successfully!${NC}"
echo -e "${BLUE}Your notepad backend is now running on AWS! 🚀${NC}" 