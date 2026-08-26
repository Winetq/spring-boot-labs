package aui.properties

import software.amazon.awscdk.services.ec2.IMachineImage
import software.amazon.awscdk.services.ec2.ISecurityGroup
import software.amazon.awscdk.services.ec2.IVpc
import software.amazon.awscdk.services.ec2.InstanceType
import software.amazon.awscdk.services.ec2.UserData
import software.amazon.awscdk.services.iam.IRole

data class Ec2InstanceProperties(
    val instanceName: String,
    val instanceType: InstanceType,
    val machineImage: IMachineImage,
    val vpc: IVpc,
    val securityGroup: ISecurityGroup,
    val role: IRole,
    val userData: UserData,
    val associatePublicIpAddress: Boolean = true,
)
