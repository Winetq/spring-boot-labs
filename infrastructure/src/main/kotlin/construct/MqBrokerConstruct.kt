package aui.construct

import aui.constants.InfrastructureConstants.MQ_BROKER_NAME
import aui.constants.InfrastructureConstants.MQ_ENGINE_VERSION
import aui.constants.InfrastructureConstants.MQ_HOST_INSTANCE_TYPE
import aui.constants.InfrastructureConstants.MQ_USERNAME
import aui.constants.InfrastructureConstants.NAME_TAG_KEY
import aui.properties.MqBrokerProperties
import software.amazon.awscdk.Fn
import software.amazon.awscdk.Tags
import software.amazon.awscdk.services.amazonmq.CfnBroker
import software.amazon.awscdk.services.ec2.ISecurityGroup
import software.amazon.awscdk.services.ec2.IVpc
import software.amazon.awscdk.services.secretsmanager.Secret
import software.amazon.awscdk.services.secretsmanager.SecretStringGenerator
import software.constructs.Construct

class MqBrokerConstruct(
    scope: Construct,
    id: String,
    mqBrokerProperties: MqBrokerProperties,
) : Construct(scope, id) {

    // Unlike RDS (an L2 construct with the built-in Credentials.fromGeneratedSecret helper),
    // Amazon MQ only ships as the L1 CfnBroker, which has no credential helpers. So we generate
    // the secret ourselves here and feed the username/password into the broker's users list below.
    val secret: Secret =
        Secret.Builder.create(this, "Credentials")
            .generateSecretString(
                SecretStringGenerator.builder()
                    .secretStringTemplate("{\"username\":\"${mqBrokerProperties.username}\"}")
                    .generateStringKey("password")
                    .passwordLength(32)
                    // Amazon MQ passwords must not contain commas, colons, equals signs or spaces.
                    .excludeCharacters(",:= @/\\\"'")
                    .build()
            )
            .build()

    val broker: CfnBroker =
        CfnBroker.Builder.create(this, "Broker")
            .brokerName(mqBrokerProperties.brokerName)
            .engineType("RABBITMQ")
            .engineVersion(mqBrokerProperties.engineVersion)
            .hostInstanceType(mqBrokerProperties.hostInstanceType)
            .deploymentMode("SINGLE_INSTANCE")
            .autoMinorVersionUpgrade(true)
            .publiclyAccessible(false)
            .securityGroups(listOf(mqBrokerProperties.securityGroup.securityGroupId))
            .subnetIds(listOf(mqBrokerProperties.vpc.publicSubnets.first().subnetId))
            .users(
                listOf(
                    CfnBroker.UserProperty.builder()
                        .username(secret.secretValueFromJson("username").unsafeUnwrap())
                        .password(secret.secretValueFromJson("password").unsafeUnwrap())
                        .build()
                )
            )
            .build()

    // e.g. "amqps://b-xxxx.mq.eu-central-1.amazonaws.com:5671"
    val amqpEndpoint: String = Fn.select(0, broker.attrAmqpEndpoints)

    // Bare broker host (no "amqps://" prefix, no ":5671" suffix), for use as RABBIT_HOST.
    // The EC2 stack derives this at runtime with sed in user-data; the ECS stack needs it at
    // synth time, so we peel the token apart with CloudFormation intrinsics: split on "://" and
    // take the second half, then split on ":" and take the host.
    val amqpHost: String =
        Fn.select(0, Fn.split(":", Fn.select(1, Fn.split("://", amqpEndpoint))))

    init {
        Tags.of(this).add(NAME_TAG_KEY, mqBrokerProperties.brokerName)
    }

    companion object {
        fun createMqBrokerProperties(
            vpc: IVpc,
            securityGroup: ISecurityGroup,
        ): MqBrokerProperties =
            MqBrokerProperties(
                brokerName = MQ_BROKER_NAME,
                engineVersion = MQ_ENGINE_VERSION,
                hostInstanceType = MQ_HOST_INSTANCE_TYPE,
                username = MQ_USERNAME,
                vpc = vpc,
                securityGroup = securityGroup,
            )
    }
}
