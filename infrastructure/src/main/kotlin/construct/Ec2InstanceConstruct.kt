package aui.construct

import aui.constants.InfrastructureConstants.COACH_IMAGE
import aui.constants.InfrastructureConstants.COACH_INSTANCE_NAME
import aui.constants.InfrastructureConstants.COACH_PORT
import aui.constants.InfrastructureConstants.INSTANCE_TYPE
import aui.constants.InfrastructureConstants.NAME_TAG_KEY
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
            containerEnvironment: Map<String, String>,
        ): Ec2InstanceProperties =
            Ec2InstanceProperties(
                instanceName = COACH_INSTANCE_NAME,
                instanceType = InstanceType(INSTANCE_TYPE),
                machineImage = latestAmazonLinux2023(),
                vpc = vpc,
                securityGroup = securityGroup,
                userData = dockerUserData(
                    containerName = COACH_INSTANCE_NAME,
                    image = COACH_IMAGE,
                    port = COACH_PORT,
                    environment = containerEnvironment,
                ),
            )

        fun createSwimmerEc2InstanceProperties(
            vpc: IVpc,
            securityGroup: ISecurityGroup,
            containerEnvironment: Map<String, String>,
        ): Ec2InstanceProperties =
            Ec2InstanceProperties(
                instanceName = SWIMMER_INSTANCE_NAME,
                instanceType = InstanceType(INSTANCE_TYPE),
                machineImage = latestAmazonLinux2023(),
                vpc = vpc,
                securityGroup = securityGroup,
                userData = dockerUserData(
                    containerName = SWIMMER_INSTANCE_NAME,
                    image = SWIMMER_IMAGE,
                    port = SWIMMER_PORT,
                    environment = containerEnvironment,
                ),
            )

        private fun dockerUserData(
            containerName: String,
            image: String,
            port: Int,
            environment: Map<String, String>,
        ): UserData {
            val userData = UserData.forLinux()
            val envArgs = environment.entries.joinToString(" ") { (key, value) -> "-e $key=$value" }
            userData.addCommands(
                "yum update -y",
                "yum install -y docker",
                "systemctl enable --now docker",
                "docker run -d --restart always --name $containerName -p $port:$port $envArgs $image",
            )
            return userData
        }
    }
}
