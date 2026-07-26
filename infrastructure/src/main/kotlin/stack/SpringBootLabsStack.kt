package aui.stack

import aui.constants.InfrastructureConstants.COACH_PORT
import aui.constants.InfrastructureConstants.SWIMMER_PORT
import aui.construct.Ec2InstanceConstruct
import aui.construct.Ec2InstanceConstruct.Companion.createCoachEc2InstanceProperties
import aui.construct.Ec2InstanceConstruct.Companion.createSwimmerEc2InstanceProperties
import aui.construct.SecurityGroupConstruct
import aui.construct.SecurityGroupConstruct.Companion.createEc2InstancesSecurityGroupProperties
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.ec2.Vpc
import software.amazon.awscdk.services.ec2.VpcLookupOptions
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

        val securityGroup = SecurityGroupConstruct(
            this,
            "Ec2InstancesSecurityGroup",
            createEc2InstancesSecurityGroupProperties(vpc)
        ).securityGroup

        val coachEnvironment = mapOf(
            "SERVER_PORT" to COACH_PORT.toString(),
            "POSTGRES_USER" to "admin",
            "POSTGRES_PASSWORD" to "admin",
            "POSTGRES_HOST" to "postgres-coach-db",
            "POSTGRES_DATABASE" to "coach_db",
            "POSTGRES_PORT" to "5432",
            "RABBIT_USER" to "guest",
            "RABBIT_PASSWORD" to "guest",
            "RABBIT_HOST" to "rabbitmq",
            "RABBIT_PORT" to "5672",
        )

        val swimmerEnvironment = mapOf(
            "SERVER_PORT" to SWIMMER_PORT.toString(),
            "POSTGRES_USER" to "admin",
            "POSTGRES_PASSWORD" to "admin",
            "POSTGRES_HOST" to "postgres-swimmer-db",
            "POSTGRES_DATABASE" to "swimmer_db",
            "POSTGRES_PORT" to "5432",
            "RABBIT_USER" to "guest",
            "RABBIT_PASSWORD" to "guest",
            "RABBIT_HOST" to "rabbitmq",
            "RABBIT_PORT" to "5672",
        )

        Ec2InstanceConstruct(
            this,
            "CoachEc2Instance",
            createCoachEc2InstanceProperties(vpc, securityGroup, coachEnvironment)
        )

        Ec2InstanceConstruct(
            this,
            "SwimmerEc2Instance",
            createSwimmerEc2InstanceProperties(vpc, securityGroup, swimmerEnvironment)
        )
    }
}
