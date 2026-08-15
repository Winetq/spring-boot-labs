package aui.stack

import aui.construct.AlbConstruct
import aui.construct.AlbConstruct.Companion.createAlbProperties
import aui.construct.ApiGatewayConstruct
import aui.construct.ApiGatewayConstruct.Companion.createApiGatewayProperties
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
import aui.construct.StaticSiteConstruct
import aui.construct.StaticSiteConstruct.Companion.createStaticSiteProperties
import software.amazon.awscdk.CfnOutput
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.ec2.Vpc
import software.amazon.awscdk.services.ec2.VpcLookupOptions
import software.constructs.Construct

class SpringBootLabsEcsStack(
    scope: Construct,
    stackId: String,
    props: StackProps,
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

        // The ALB is the only public entry point; the ECS tasks accept traffic from it alone.
        val albSecurityGroup = SecurityGroupConstruct(
            this,
            "AlbSecurityGroup",
            createAlbSecurityGroupProperties(vpc)
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
            )
        )

        val staticSite = StaticSiteConstruct(
            this,
            "ClientStaticSite",
            createStaticSiteProperties()
        )

        // Both routes point at the same ALB; the load balancer forwards /coaches* and /swimmers*
        // to the matching ECS service, so the API Gateway only needs the ALB's public DNS name.
        val albBaseUrl = "http://${alb.dnsName}"
        val api = ApiGatewayConstruct(
            this,
            "ApiGateway",
            createApiGatewayProperties(
                coachBaseUrl = albBaseUrl,
                swimmerBaseUrl = albBaseUrl,
                allowedOrigin = "https://${staticSite.distributionDomainName}",
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
            .value("https://${staticSite.distributionDomainName}")
            .build()

        CfnOutput.Builder.create(this, "LoadBalancerDns")
            .description("Public DNS name of the application load balancer")
            .value(alb.dnsName)
            .build()
    }
}
