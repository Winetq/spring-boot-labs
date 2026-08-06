package aui.stack

import aui.construct.ApiGatewayConstruct
import aui.construct.ApiGatewayConstruct.Companion.createApiGatewayProperties
import aui.construct.Ec2InstanceConstruct
import aui.construct.Ec2InstanceConstruct.Companion.createCoachEc2InstanceProperties
import aui.construct.Ec2InstanceConstruct.Companion.createSwimmerEc2InstanceProperties
import aui.construct.MqBrokerConstruct
import aui.construct.MqBrokerConstruct.Companion.createMqBrokerProperties
import aui.construct.RdsInstanceConstruct
import aui.construct.RdsInstanceConstruct.Companion.createRdsInstanceProperties
import aui.construct.SecurityGroupConstruct
import aui.construct.SecurityGroupConstruct.Companion.createEc2InstancesSecurityGroupProperties
import aui.construct.SecurityGroupConstruct.Companion.createMqSecurityGroupProperties
import aui.construct.SecurityGroupConstruct.Companion.createRdsSecurityGroupProperties
import aui.construct.StaticSiteConstruct
import aui.construct.StaticSiteConstruct.Companion.createStaticSiteProperties
import aui.constants.InfrastructureConstants.COACH_PORT
import aui.constants.InfrastructureConstants.SWIMMER_PORT
import software.amazon.awscdk.CfnOutput
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.ec2.Vpc
import software.amazon.awscdk.services.ec2.VpcLookupOptions
import software.amazon.awscdk.services.iam.Role
import software.amazon.awscdk.services.iam.ServicePrincipal
import software.constructs.Construct

class SpringBootLabsStack(
    scope: Construct,
    stackId: String,
    props: StackProps,
    region: String,
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

        val ec2SecurityGroup = SecurityGroupConstruct(
            this,
            "Ec2InstancesSecurityGroup",
            createEc2InstancesSecurityGroupProperties(vpc)
        ).securityGroup

        val rdsSecurityGroup = SecurityGroupConstruct(
            this,
            "RdsSecurityGroup",
            createRdsSecurityGroupProperties(vpc, ec2SecurityGroup)
        ).securityGroup

        val rds = RdsInstanceConstruct(
            this,
            "PostgreSqlRds",
            createRdsInstanceProperties(vpc, rdsSecurityGroup)
        )

        val mqSecurityGroup = SecurityGroupConstruct(
            this,
            "MqSecurityGroup",
            createMqSecurityGroupProperties(vpc, ec2SecurityGroup)
        ).securityGroup

        val mq = MqBrokerConstruct(
            this,
            "RabbitMqBroker",
            createMqBrokerProperties(vpc, mqSecurityGroup)
        )

        // Lets both instances read the DB and MQ secrets from Secrets Manager.
        val instanceRole = Role.Builder.create(this, "Ec2InstanceRole")
            .assumedBy(ServicePrincipal("ec2.amazonaws.com"))
            .build()
        rds.secret.grantRead(instanceRole)
        mq.secret.grantRead(instanceRole)

        val coachInstance = Ec2InstanceConstruct(
            this,
            "CoachEc2Instance",
            createCoachEc2InstanceProperties(
                vpc = vpc,
                securityGroup = ec2SecurityGroup,
                role = instanceRole,
                dbSecret = rds.secret,
                dbHost = rds.endpointAddress,
                dbReadHost = rds.readerEndpointAddress,
                mqSecret = mq.secret,
                mqAmqpEndpoint = mq.amqpEndpoint,
                region = region,
            )
        ).instance

        val swimmerInstance = Ec2InstanceConstruct(
            this,
            "SwimmerEc2Instance",
            createSwimmerEc2InstanceProperties(
                vpc = vpc,
                securityGroup = ec2SecurityGroup,
                role = instanceRole,
                dbSecret = rds.secret,
                dbHost = rds.endpointAddress,
                mqSecret = mq.secret,
                mqAmqpEndpoint = mq.amqpEndpoint,
                region = region,
            )
        ).instance

        val staticSite = StaticSiteConstruct(
            this,
            "ClientStaticSite",
            createStaticSiteProperties()
        )

        val api = ApiGatewayConstruct(
            this,
            "ApiGateway",
            createApiGatewayProperties(
                coachBaseUrl = "http://${coachInstance.instancePublicIp}:$COACH_PORT",
                swimmerBaseUrl = "http://${swimmerInstance.instancePublicIp}:$SWIMMER_PORT",
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
    }
}
