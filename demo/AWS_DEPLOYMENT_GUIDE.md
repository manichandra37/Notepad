# 🚀 AWS Deployment Guide for Notepad Backend

This guide provides step-by-step instructions for deploying your notepad backend to AWS using different services.

## 📋 Prerequisites

### 1. AWS Account Setup
- [ ] AWS Account created
- [ ] AWS CLI installed and configured
- [ ] Docker installed
- [ ] Appropriate AWS permissions

### 2. Required AWS Services
- **RDS PostgreSQL** - Database
- **ECR** - Container registry
- **ECS/EC2/Elastic Beanstalk** - Compute
- **VPC** - Networking
- **Security Groups** - Firewall

## 🗄️ Step 1: Set Up Database (RDS PostgreSQL)

### Create RDS Instance
```bash
# Create RDS PostgreSQL instance
aws rds create-db-instance \
    --db-instance-identifier notepad-db \
    --db-instance-class db.t3.micro \
    --engine postgres \
    --master-username postgres \
    --master-user-password YourSecurePassword123! \
    --allocated-storage 20 \
    --vpc-security-group-ids sg-xxxxxxxxx \
    --db-subnet-group-name your-subnet-group \
    --backup-retention-period 7 \
    --region us-east-1
```

### Get Database Endpoint
```bash
aws rds describe-db-instances \
    --db-instance-identifier notepad-db \
    --query 'DBInstances[0].Endpoint.Address' \
    --output text
```

## 🐳 Step 2: Build and Push Docker Image

### Build Image Locally
```bash
cd demo
docker build -t notepad-backend .
```

### Create ECR Repository
```bash
aws ecr create-repository --repository-name notepad-backend --region us-east-1
```

### Push to ECR
```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com

# Tag image
docker tag notepad-backend:latest YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/notepad-backend:latest

# Push image
docker push YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/notepad-backend:latest
```

## 🚀 Step 3: Choose Deployment Option

### Option A: Amazon ECS (Recommended)

#### Create ECS Cluster
```bash
aws ecs create-cluster --cluster-name notepad-cluster --region us-east-1
```

#### Create Task Definition
```bash
# Use the generated task-definition.json from aws-deploy.sh
aws ecs register-task-definition --cli-input-json file://task-definition.json --region us-east-1
```

#### Create ECS Service
```bash
aws ecs create-service \
    --cluster notepad-cluster \
    --service-name notepad-service \
    --task-definition notepad-backend \
    --desired-count 1 \
    --launch-type FARGATE \
    --network-configuration "awsvpcConfiguration={subnets=[subnet-xxxxxxxxx],securityGroups=[sg-xxxxxxxxx],assignPublicIp=ENABLED}" \
    --region us-east-1
```

### Option B: Amazon EC2

#### Launch EC2 Instance
```bash
aws ec2 run-instances \
    --image-id ami-0c02fb55956c7d316 \
    --count 1 \
    --instance-type t3.small \
    --key-name your-key-pair \
    --security-group-ids sg-xxxxxxxxx \
    --subnet-id subnet-xxxxxxxxx \
    --user-data file://user-data.sh \
    --region us-east-1
```

### Option C: Elastic Beanstalk

#### Install EB CLI
```bash
pip install awsebcli
```

#### Initialize EB Application
```bash
eb init notepad-backend --platform java --region us-east-1
```

#### Create Environment
```bash
eb create notepad-prod --instance-type t3.small --envvars SPRING_PROFILES_ACTIVE=prod
```

## 🔧 Step 4: Environment Configuration

### Set Environment Variables
```bash
# For ECS
aws ecs update-service \
    --cluster notepad-cluster \
    --service notepad-service \
    --task-definition notepad-backend \
    --region us-east-1

# For EC2 (via user data)
# Environment variables are set in user-data.sh

# For Elastic Beanstalk
eb setenv SPRING_PROFILES_ACTIVE=prod DATABASE_URL=jdbc:postgresql://your-rds-endpoint:5432/notepaddb
```

## 🌐 Step 5: Set Up Load Balancer (Optional)

### Create Application Load Balancer
```bash
aws elbv2 create-load-balancer \
    --name notepad-alb \
    --subnets subnet-xxxxxxxxx subnet-yyyyyyyyy \
    --security-groups sg-xxxxxxxxx \
    --region us-east-1
```

### Create Target Group
```bash
aws elbv2 create-target-group \
    --name notepad-tg \
    --protocol HTTP \
    --port 8080 \
    --vpc-id vpc-xxxxxxxxx \
    --target-type ip \
    --region us-east-1
```

## 🔒 Step 6: Security Configuration

### Security Groups
```bash
# Create security group for application
aws ec2 create-security-group \
    --group-name notepad-app-sg \
    --description "Security group for notepad application" \
    --vpc-id vpc-xxxxxxxxx

# Allow HTTP traffic
aws ec2 authorize-security-group-ingress \
    --group-id sg-xxxxxxxxx \
    --protocol tcp \
    --port 8080 \
    --cidr 0.0.0.0/0
```

### IAM Roles
```bash
# Create ECS task execution role
aws iam create-role \
    --role-name ecsTaskExecutionRole \
    --assume-role-policy-document file://trust-policy.json

# Attach required policies
aws iam attach-role-policy \
    --role-name ecsTaskExecutionRole \
    --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy
```

## 📊 Step 6: Monitoring and Logging

### CloudWatch Logs
```bash
# Create log group
aws logs create-log-group --log-group-name /ecs/notepad-backend --region us-east-1
```

### CloudWatch Alarms
```bash
# Create CPU alarm
aws cloudwatch put-metric-alarm \
    --alarm-name notepad-cpu-alarm \
    --alarm-description "CPU utilization alarm" \
    --metric-name CPUUtilization \
    --namespace AWS/ECS \
    --statistic Average \
    --period 300 \
    --threshold 80 \
    --comparison-operator GreaterThanThreshold \
    --evaluation-periods 2
```

## 🧪 Step 7: Testing

### Test API Endpoints
```bash
# Health check
curl https://your-alb-endpoint/api/notepads/health

# Get all notepads
curl https://your-alb-endpoint/api/notepads

# Create a notepad
curl -X POST https://your-alb-endpoint/api/notepads \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Note","content":"This is a test note"}'
```

## 🔄 Step 8: CI/CD Pipeline (Optional)

### GitHub Actions Workflow
```yaml
name: Deploy to AWS
on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    
    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v1
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: us-east-1
    
    - name: Build and push Docker image
      run: |
        docker build -t notepad-backend .
        aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin ${{ secrets.AWS_ACCOUNT_ID }}.dkr.ecr.us-east-1.amazonaws.com
        docker tag notepad-backend:latest ${{ secrets.AWS_ACCOUNT_ID }}.dkr.ecr.us-east-1.amazonaws.com/notepad-backend:latest
        docker push ${{ secrets.AWS_ACCOUNT_ID }}.dkr.ecr.us-east-1.amazonaws.com/notepad-backend:latest
    
    - name: Update ECS service
      run: |
        aws ecs update-service --cluster notepad-cluster --service notepad-service --force-new-deployment --region us-east-1
```

## 💰 Cost Optimization

### Estimated Monthly Costs (us-east-1)
- **RDS PostgreSQL (db.t3.micro)**: ~$15/month
- **ECS Fargate (0.25 vCPU, 0.5GB)**: ~$10/month
- **Application Load Balancer**: ~$20/month
- **Data Transfer**: ~$5/month
- **Total**: ~$50/month

### Cost Optimization Tips
1. Use Spot Instances for non-critical workloads
2. Implement auto-scaling based on demand
3. Use S3 for static assets
4. Enable RDS backup retention optimization

## 🛠️ Troubleshooting

### Common Issues

#### 1. Container Won't Start
```bash
# Check ECS service events
aws ecs describe-services --cluster notepad-cluster --services notepad-service

# Check container logs
aws logs describe-log-streams --log-group-name /ecs/notepad-backend
```

#### 2. Database Connection Issues
```bash
# Test database connectivity
aws rds describe-db-instances --db-instance-identifier notepad-db

# Check security group rules
aws ec2 describe-security-groups --group-ids sg-xxxxxxxxx
```

#### 3. Application Not Responding
```bash
# Check load balancer health
aws elbv2 describe-target-health --target-group-arn your-target-group-arn

# Check security group rules
aws ec2 describe-security-groups --group-ids sg-xxxxxxxxx
```

## 📚 Additional Resources

- [AWS ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [AWS RDS Documentation](https://docs.aws.amazon.com/rds/)
- [Spring Boot on AWS](https://spring.io/guides/gs/spring-boot-docker/)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)

## 🎯 Quick Deployment

For the fastest deployment, use the provided script:

```bash
cd demo
./aws-deploy.sh
```

This script will guide you through the entire deployment process! 