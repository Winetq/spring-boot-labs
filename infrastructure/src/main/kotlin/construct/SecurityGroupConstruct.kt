package aui.construct

import aui.constants.InfrastructureConstants.ALB_LISTENER_PORT
import aui.constants.InfrastructureConstants.ALLOWED_INGRESS_CIDR
import aui.constants.InfrastructureConstants.COACH_PORT
import aui.constants.InfrastructureConstants.PERSONAL_INGRESS_CIDR
import aui.constants.InfrastructureConstants.MQ_PORT
import aui.constants.InfrastructureConstants.NAME_TAG_KEY
import aui.constants.InfrastructureConstants.POSTGRES_PORT
import aui.constants.InfrastructureConstants.SSH_PORT
import aui.constants.InfrastructureConstants.SWIMMER_PORT
import aui.properties.SecurityGroupProperties
import aui.properties.SecurityGroupProperties.SecurityGroupRule
import software.amazon.awscdk.Tags
import software.amazon.awscdk.services.ec2.ISecurityGroup
import software.amazon.awscdk.services.ec2.IVpc
import software.amazon.awscdk.services.ec2.Peer
import software.amazon.awscdk.services.ec2.Port
import software.amazon.awscdk.services.ec2.SecurityGroup
import software.constructs.Construct

class SecurityGroupConstruct(
    scope: Construct,
    id: String,
    securityGroupProperties: SecurityGroupProperties,
) : Construct(scope, id) {

    val securityGroup: SecurityGroup =
        SecurityGroup.Builder.create(this, "SecurityGroup")
            .securityGroupName(securityGroupProperties.name)
            .vpc(securityGroupProperties.vpc)
            .description(securityGroupProperties.description)
            .allowAllOutbound(true)
            .disableInlineRules(true)
            .build()

    init {
        Tags.of(this).add(NAME_TAG_KEY, securityGroupProperties.name)
        securityGroupProperties.inboundRules.forEach { rule ->
            securityGroup.addIngressRule(rule.peer, rule.port, rule.description)
        }
    }

    companion object {

        // --- EC2-based stack security groups ---

        fun createEc2InstancesSecurityGroupProperties(vpc: IVpc): SecurityGroupProperties =
            SecurityGroupProperties(
                name = "spring-boot-labs-ec2-instances-sg",
                description = "Security group for coach and swimmer EC2 instances",
                vpc = vpc,
                inboundRules = listOf(
                    SecurityGroupRule(
                        peer = Peer.ipv4(ALLOWED_INGRESS_CIDR),
                        port = Port.tcp(COACH_PORT),
                        description = "Allow inbound traffic to coach service",
                    ),
                    SecurityGroupRule(
                        peer = Peer.ipv4(ALLOWED_INGRESS_CIDR),
                        port = Port.tcp(SWIMMER_PORT),
                        description = "Allow inbound traffic to swimmer service",
                    ),
                    SecurityGroupRule(
                        peer = Peer.ipv4(ALLOWED_INGRESS_CIDR),
                        port = Port.tcp(SSH_PORT),
                        description = "Allow SSH access", // it enables EC2 Instance Connect
                    ),
                ),
            )

        fun createRdsSecurityGroupProperties(
            vpc: IVpc,
            sourceSecurityGroup: ISecurityGroup,
            sourceDescription: String = "the EC2 instances",
        ): SecurityGroupProperties =
            SecurityGroupProperties(
                name = "spring-boot-labs-rds-sg",
                description = "Security group for the RDS PostgreSQL instance",
                vpc = vpc,
                inboundRules = listOf(
                    // ISecurityGroup is an IPeer, so the source SG becomes a source-SG ingress rule.
                    SecurityGroupRule(
                        peer = sourceSecurityGroup,
                        port = Port.tcp(POSTGRES_PORT),
                        description = "Allow PostgreSQL access from $sourceDescription",
                    ),
                    // Allow connecting directly from the personal IP only (e.g. from DBeaver).
                    SecurityGroupRule(
                        peer = Peer.ipv4(PERSONAL_INGRESS_CIDR),
                        port = Port.tcp(POSTGRES_PORT),
                        description = "Allow PostgreSQL access from the personal IP",
                    ),
                ),
            )

        fun createMqSecurityGroupProperties(
            vpc: IVpc,
            sourceSecurityGroup: ISecurityGroup,
            sourceDescription: String = "the EC2 instances",
        ): SecurityGroupProperties =
            SecurityGroupProperties(
                name = "spring-boot-labs-mq-sg",
                description = "Security group for the Amazon MQ RabbitMQ broker",
                vpc = vpc,
                inboundRules = listOf(
                    SecurityGroupRule(
                        peer = sourceSecurityGroup,
                        port = Port.tcp(MQ_PORT),
                        description = "Allow AMQPS access from $sourceDescription",
                    ),
                ),
            )

        // --- ECS-based stack security groups ---

        fun createAlbSecurityGroupProperties(vpc: IVpc): SecurityGroupProperties =
            SecurityGroupProperties(
                name = "spring-boot-labs-alb-sg",
                description = "Security group for the application load balancer",
                vpc = vpc,
                inboundRules = listOf(
                    // API Gateway reaches the public ALB over the internet from AWS-managed IPs,
                    // so port 80 stays open; requests are still guarded by the Okta JWT authorizer.
                    // The tighter alternative is a private ALB (internetFacing = false) fronted by an
                    // API Gateway VPC Link (with its own security group); ingress here would then be
                    // restricted to that VPC Link's security group instead of 0.0.0.0/0.
                    SecurityGroupRule(
                        peer = Peer.ipv4(ALLOWED_INGRESS_CIDR),
                        port = Port.tcp(ALB_LISTENER_PORT),
                        description = "Allow inbound HTTP traffic to the ALB",
                    ),
                ),
            )

        fun createEcsTasksSecurityGroupProperties(
            vpc: IVpc,
            albSecurityGroup: ISecurityGroup,
        ): SecurityGroupProperties =
            SecurityGroupProperties(
                name = "spring-boot-labs-ecs-tasks-sg",
                description = "Security group for the ECS Fargate tasks",
                vpc = vpc,
                inboundRules = listOf(
                    SecurityGroupRule(
                        peer = albSecurityGroup,
                        port = Port.tcp(COACH_PORT),
                        description = "Allow traffic from the ALB to the coach service",
                    ),
                    SecurityGroupRule(
                        peer = albSecurityGroup,
                        port = Port.tcp(SWIMMER_PORT),
                        description = "Allow traffic from the ALB to the swimmer service",
                    ),
                ),
            )
    }
}
