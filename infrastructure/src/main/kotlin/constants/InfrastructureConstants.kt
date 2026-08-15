package aui.constants

object InfrastructureConstants {

    const val REGION = "REGION"
    const val STACK_NAME = "STACK_NAME"

    const val NAME_TAG_KEY = "Name"

    // EC2 instances
    const val COACH_INSTANCE_NAME = "coach"
    const val SWIMMER_INSTANCE_NAME = "swimmer"
    const val INSTANCE_TYPE = "t3.micro"

    // Application ports
    const val COACH_PORT = 8081
    const val SWIMMER_PORT = 8082
    const val SSH_PORT = 22

    // Security group
    // 0.0.0.0/0 on purpose - it cannot be narrowed because:
    //  - 8081/8082: API Gateway reaches the instances over their PUBLIC IP, and that traffic
    //    originates from AWS-managed API Gateway addresses. These ports are
    //    instead protected at the application layer - the services validate the Okta JWT.
    //  - 22 (SSH): browser-based EC2 Instance Connect also comes from AWS-managed IP ranges.
    const val ALLOWED_INGRESS_CIDR = "0.0.0.0/0"

    // Personal public IP range (/24) allowed to reach RDS directly, e.g. from DBeaver.
    // Using a /24 (256 addresses) instead of a single /32 avoids exposing the exact IP and
    // survives changes to the last octet when the ISP (Internet Service Provider) reassigns a dynamic address.
    const val PERSONAL_INGRESS_CIDR = "165.1.145.0/24"

    // Docker Hub images
    const val COACH_IMAGE = "mcwynar/spring-boot-labs:coach"
    const val SWIMMER_IMAGE = "mcwynar/spring-boot-labs:swimmer"

    // RDS (PostgreSQL)
    const val RDS_INSTANCE_IDENTIFIER = "spring-boot-labs-postgre-sql"
    const val RDS_MASTER_USERNAME = "dbadmin"
    const val RDS_ALLOCATED_STORAGE = 20
    const val POSTGRES_PORT = 5432
    const val COACH_DATABASE_NAME = "coach_db"
    const val SWIMMER_DATABASE_NAME = "swimmer_db"

    // Amazon MQ (RabbitMQ)
    const val MQ_BROKER_NAME = "spring-boot-labs-rabbitmq"
    const val MQ_USERNAME = "mqadmin"
    const val MQ_ENGINE_VERSION = "3.13"
    // RabbitMQ no longer offers t3.micro; mq.m7g.medium is the smallest available type (not free tier ~ $0.08/h).
    const val MQ_HOST_INSTANCE_TYPE = "mq.m7g.medium"
    // Amazon MQ for RabbitMQ only accepts TLS connections (amqps) on 5671.
    const val MQ_PORT = 5671

    // ECS (Fargate) - used only by the ECS-based stack, the EC2 stack ignores these.
    const val ECS_CLUSTER_NAME = "spring-boot-labs-ecs-cluster"
    const val COACH_SERVICE_NAME = "coach"
    const val SWIMMER_SERVICE_NAME = "swimmer"
    // Smallest Fargate combo that comfortably starts a Spring Boot JVM (0.5 vCPU / 1 GB).
    const val FARGATE_CPU = 512
    const val FARGATE_MEMORY_MIB = 1024
    const val DESIRED_COUNT = 2
    // The health endpoint is public (permitAll in SecurityConfig), so the ALB can probe it
    // without a JWT and expect a plain 200 when the app is up.
    const val HEALTH_CHECK_PATH = "/actuator/health"
    const val HEALTHY_HTTP_CODES = "200"
    // Public Postgres client image used by the one-shot init container that creates the service DB.
    const val POSTGRES_INIT_IMAGE = "postgres:18-alpine"

    // Application Load Balancer (fronts the ECS services)
    const val ALB_NAME = "spring-boot-labs-alb"
    const val ALB_LISTENER_PORT = 80

    // API Gateway (HTTP API)
    const val API_NAME = "spring-boot-labs-api"
    const val COACHES_PATH = "coaches"
    const val SWIMMERS_PATH = "swimmers"

    // Static site (S3 + CloudFront)
    const val CLIENT_SITE_NAME = "spring-boot-labs-client"
    // Path to the frontend, relative to the infrastructure/ directory.
    const val CLIENT_SOURCE_PATH = "../client"

    // Okta (JWT authorizer) - must match the services' resource-server config.
    const val OKTA_ISSUER_URL = "https://integrator-5997569.okta.com/oauth2/default"
    const val OKTA_AUDIENCE = "api://default"
}
