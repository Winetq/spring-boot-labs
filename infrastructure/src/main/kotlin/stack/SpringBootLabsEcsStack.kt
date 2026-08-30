package aui.stack

import aui.construct.AlbConstruct
import aui.construct.AlbConstruct.Companion.createAlbProperties
import aui.construct.ApiGatewayEcsConstruct
import aui.construct.ApiGatewayEcsConstruct.Companion.createApiGatewayProperties
import aui.construct.EcsConstruct
import aui.construct.EcsConstruct.Companion.createEcsProperties
import aui.construct.MqBrokerConstruct
import aui.construct.MqBrokerConstruct.Companion.createMqBrokerProperties
import aui.construct.RdsInstanceConstruct
import aui.construct.RdsInstanceConstruct.Companion.createRdsInstanceProperties
import aui.construct.SecurityGroupConstruct
import aui.construct.SecurityGroupConstruct.Companion.createAlbSecurityGroupProperties
import aui.construct.SecurityGroupConstruct.Companion.createEcsTasksSecurityGroupProperties
import aui.construct.SecurityGroupConstruct.Companion.createMqSecurityGroupProperties
import aui.construct.SecurityGroupConstruct.Companion.createRdsSecurityGroupProperties
import aui.construct.SecurityGroupConstruct.Companion.createVpcLinkSecurityGroupProperties
import aui.construct.SnsTopicConstruct
import aui.construct.SnsTopicConstruct.Companion.createAlarmsSnsTopicProperties
import aui.construct.StaticSiteConstruct
import aui.construct.StaticSiteConstruct.Companion.createStaticSiteProperties
import aui.construct.VpcLinkConstruct
import aui.construct.VpcLinkConstruct.Companion.createVpcLinkProperties
import software.amazon.awscdk.CfnOutput
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.certificatemanager.ICertificate
import software.amazon.awscdk.services.ec2.Vpc
import software.amazon.awscdk.services.ec2.VpcLookupOptions
import software.constructs.Construct

class SpringBootLabsEcsStack(
    scope: Construct,
    stackId: String,
    props: StackProps,
    certificate: ICertificate,
) : Stack(scope, stackId, props) {

    init {
        // Use the account's default VPC (already has public subnets).
        val vpc = Vpc.fromLookup(
            this,
            "DefaultVpc",
            VpcLookupOptions.builder()
                .isDefault(true)
                .build()
        )

        // The VPC Link is the only thing allowed to reach the private ALB, so its security group
        // must exist before the ALB security group (which references it as its ingress source).
        val vpcLinkSecurityGroup = SecurityGroupConstruct(
            this,
            "VpcLinkSecurityGroup",
            createVpcLinkSecurityGroupProperties(vpc)
        ).securityGroup

        // The private ALB accepts traffic only from the API Gateway VPC Link; the ECS tasks in turn
        // accept traffic from the ALB alone.
        val albSecurityGroup = SecurityGroupConstruct(
            this,
            "AlbSecurityGroup",
            createAlbSecurityGroupProperties(vpc, vpcLinkSecurityGroup)
        ).securityGroup

        val ecsTasksSecurityGroup = SecurityGroupConstruct(
            this,
            "EcsTasksSecurityGroup",
            createEcsTasksSecurityGroupProperties(vpc, albSecurityGroup)
        ).securityGroup

        val rdsSecurityGroup = SecurityGroupConstruct(
            this,
            "RdsSecurityGroup",
            createRdsSecurityGroupProperties(vpc, ecsTasksSecurityGroup, "the ECS tasks")
        ).securityGroup

        val rds = RdsInstanceConstruct(
            this,
            "PostgreSqlRds",
            createRdsInstanceProperties(vpc, rdsSecurityGroup)
        )

        val mqSecurityGroup = SecurityGroupConstruct(
            this,
            "MqSecurityGroup",
            createMqSecurityGroupProperties(vpc, ecsTasksSecurityGroup, "the ECS tasks")
        ).securityGroup

        val mq = MqBrokerConstruct(
            this,
            "RabbitMqBroker",
            createMqBrokerProperties(vpc, mqSecurityGroup)
        )

        // Application Load Balancer fronting the ECS services (path-based routing lives here).
        val alb = AlbConstruct(
            this,
            "LoadBalancer",
            createAlbProperties(vpc, albSecurityGroup)
        )

        // Single SNS topic (email subscription) that every CloudWatch alarm publishes to.
        val alarmsTopic = SnsTopicConstruct(
            this,
            "AlarmsTopic",
            createAlarmsSnsTopicProperties()
        ).topic

        // ECS reads the DB and MQ secrets from Secrets Manager; CDK grants the task execution
        // role read access automatically because they are wired in via .secrets(...).
        EcsConstruct(
            this,
            "EcsServices",
            createEcsProperties(
                vpc = vpc,
                securityGroup = ecsTasksSecurityGroup,
                listener = alb.listener,
                dbSecret = rds.secret,
                dbHost = rds.endpointAddress,
                dbReadHost = rds.readerEndpointAddress,
                mqSecret = mq.secret,
                mqHost = mq.amqpHost,
                alarmTopic = alarmsTopic,
            )
        )

        val staticSite = StaticSiteConstruct(
            this,
            "ClientStaticSite",
            createStaticSiteProperties(certificate)
        )

        // Private connection between the public HTTP API and the internal ALB.
        val vpcLink = VpcLinkConstruct(
            this,
            "ApiVpcLink",
            createVpcLinkProperties(vpc, vpcLinkSecurityGroup)
        ).vpcLink

        // API Gateway forwards straight to the ALB listener over the VPC Link; the ALB does the
        // path-based routing to coach/swimmer, so a single integration covers every route.
        val api = ApiGatewayEcsConstruct(
            this,
            "ApiGateway",
            createApiGatewayProperties(
                listener = alb.listener,
                vpcLink = vpcLink,
                allowedOrigin = staticSite.siteUrl,
            )
        )

        // Upload the frontend now that the API URL is known; CDK injects it into
        // configuration.js at deploy time, so no manual URL editing is needed.
        staticSite.deployContent(api.httpApi.apiEndpoint)

        CfnOutput.Builder.create(this, "ApiEndpoint")
            .description("Base URL of the HTTP API")
            .value(api.httpApi.apiEndpoint)
            .build()

        CfnOutput.Builder.create(this, "ClientUrl")
            .description("Public HTTPS URL of the CloudFront-hosted frontend")
            .value(staticSite.siteUrl)
            .build()
    }
}
