package aui.construct

import aui.constants.InfrastructureConstants.COACH_DATABASE_NAME
import aui.constants.InfrastructureConstants.COACH_IMAGE
import aui.constants.InfrastructureConstants.COACH_INSTANCE_NAME
import aui.constants.InfrastructureConstants.COACH_PORT
import aui.constants.InfrastructureConstants.INSTANCE_TYPE
import aui.constants.InfrastructureConstants.MQ_PORT
import aui.constants.InfrastructureConstants.NAME_TAG_KEY
import aui.constants.InfrastructureConstants.POSTGRES_PORT
import aui.constants.InfrastructureConstants.SWIMMER_DATABASE_NAME
import aui.constants.InfrastructureConstants.SWIMMER_IMAGE
import aui.constants.InfrastructureConstants.SWIMMER_INSTANCE_NAME
import aui.constants.InfrastructureConstants.SWIMMER_PORT
import aui.properties.Ec2InstanceProperties
import software.amazon.awscdk.Tags
import software.amazon.awscdk.services.ec2.ISecurityGroup
import software.amazon.awscdk.services.ec2.IVpc
import software.amazon.awscdk.services.ec2.Instance
import software.amazon.awscdk.services.ec2.InstanceType
import software.amazon.awscdk.services.ec2.MachineImage.latestAmazonLinux2023
import software.amazon.awscdk.services.ec2.SubnetSelection
import software.amazon.awscdk.services.ec2.SubnetType.PUBLIC
import software.amazon.awscdk.services.ec2.UserData
import software.amazon.awscdk.services.iam.IRole
import software.amazon.awscdk.services.secretsmanager.ISecret
import software.constructs.Construct

class Ec2InstanceConstruct(
    scope: Construct,
    id: String,
    ec2InstanceProperties: Ec2InstanceProperties,
) : Construct(scope, id) {

    val instance: Instance =
        Instance.Builder.create(this, "Ec2Instance")
            .instanceName(ec2InstanceProperties.instanceName)
            .instanceType(ec2InstanceProperties.instanceType)
            .machineImage(ec2InstanceProperties.machineImage)
            .vpc(ec2InstanceProperties.vpc)
            .vpcSubnets(
                SubnetSelection.builder()
                    .subnetType(PUBLIC)
                    .build()
            )
            .securityGroup(ec2InstanceProperties.securityGroup)
            .role(ec2InstanceProperties.role)
            .userData(ec2InstanceProperties.userData)
            .associatePublicIpAddress(ec2InstanceProperties.associatePublicIpAddress)
            .build()

    init {
        Tags.of(this).add(NAME_TAG_KEY, ec2InstanceProperties.instanceName)
    }

    companion object {
        fun createCoachEc2InstanceProperties(
            vpc: IVpc,
            securityGroup: ISecurityGroup,
            role: IRole,
            dbSecret: ISecret,
            dbHost: String,
            mqSecret: ISecret,
            mqAmqpEndpoint: String,
            region: String,
        ): Ec2InstanceProperties =
            Ec2InstanceProperties(
                instanceName = COACH_INSTANCE_NAME,
                instanceType = InstanceType(INSTANCE_TYPE),
                machineImage = latestAmazonLinux2023(),
                vpc = vpc,
                securityGroup = securityGroup,
                role = role,
                userData = dockerUserData(
                    containerName = COACH_INSTANCE_NAME,
                    image = COACH_IMAGE,
                    port = COACH_PORT,
                    databaseName = COACH_DATABASE_NAME,
                    dbSecretArn = dbSecret.secretArn,
                    dbHost = dbHost,
                    mqSecretArn = mqSecret.secretArn,
                    mqAmqpEndpoint = mqAmqpEndpoint,
                    region = region,
                ),
            )

        fun createSwimmerEc2InstanceProperties(
            vpc: IVpc,
            securityGroup: ISecurityGroup,
            role: IRole,
            dbSecret: ISecret,
            dbHost: String,
            mqSecret: ISecret,
            mqAmqpEndpoint: String,
            region: String,
        ): Ec2InstanceProperties =
            Ec2InstanceProperties(
                instanceName = SWIMMER_INSTANCE_NAME,
                instanceType = InstanceType(INSTANCE_TYPE),
                machineImage = latestAmazonLinux2023(),
                vpc = vpc,
                securityGroup = securityGroup,
                role = role,
                userData = dockerUserData(
                    containerName = SWIMMER_INSTANCE_NAME,
                    image = SWIMMER_IMAGE,
                    port = SWIMMER_PORT,
                    databaseName = SWIMMER_DATABASE_NAME,
                    dbSecretArn = dbSecret.secretArn,
                    dbHost = dbHost,
                    mqSecretArn = mqSecret.secretArn,
                    mqAmqpEndpoint = mqAmqpEndpoint,
                    region = region,
                ),
            )

        private fun dockerUserData(
            containerName: String,
            image: String,
            port: Int,
            databaseName: String,
            dbSecretArn: String,
            dbHost: String,
            mqSecretArn: String,
            mqAmqpEndpoint: String,
            region: String,
        ): UserData {
            val userData = UserData.forLinux()
            userData.addCommands(
                "yum update -y",
                "yum install -y docker jq postgresql15",
                "systemctl enable --now docker",
                // Fetch DB master credentials from Secrets Manager.
                "DB_SECRET=\$(aws secretsmanager get-secret-value --secret-id $dbSecretArn --query SecretString --output text --region $region)",
                "DB_USER=\$(echo \$DB_SECRET | jq -r .username)",
                "DB_PASS=\$(echo \$DB_SECRET | jq -r .password)",
                "export PGPASSWORD=\$DB_PASS", // used by the non-interactive psql calls below
                // Wait until RDS accepts connections, then create this service's database if missing.
                "until psql -h $dbHost -U \$DB_USER -d postgres -c '\\q' 2>/dev/null; do echo 'waiting for rds...'; sleep 5; done",
                "psql -h $dbHost -U \$DB_USER -d postgres -tc \"SELECT 1 FROM pg_database WHERE datname='$databaseName'\" | grep -q 1 || psql -h $dbHost -U \$DB_USER -d postgres -c \"CREATE DATABASE $databaseName\"",
                // Fetch RabbitMQ credentials and derive the broker host from the amqps endpoint.
                "MQ_SECRET=\$(aws secretsmanager get-secret-value --secret-id $mqSecretArn --query SecretString --output text --region $region)",
                "MQ_USER=\$(echo \$MQ_SECRET | jq -r .username)",
                "MQ_PASS=\$(echo \$MQ_SECRET | jq -r .password)",
                "MQ_HOST=\$(echo '$mqAmqpEndpoint' | sed -e 's|^amqps://||' -e 's|:$MQ_PORT\$||')",
                "docker run -d --restart on-failure:3 --name $containerName -p $port:$port" +
                    " -e SERVER_PORT=$port" +
                    " -e POSTGRES_HOST=$dbHost -e POSTGRES_PORT=$POSTGRES_PORT -e POSTGRES_DATABASE=$databaseName" +
                    " -e POSTGRES_USER=\$DB_USER -e POSTGRES_PASSWORD=\$DB_PASS" +
                    " -e RABBIT_HOST=\$MQ_HOST -e RABBIT_PORT=$MQ_PORT" +
                    " -e RABBIT_USER=\$MQ_USER -e RABBIT_PASSWORD=\$MQ_PASS -e RABBIT_SSL_ENABLED=true" +
                    " $image",
            )
            return userData
        }
    }
}
