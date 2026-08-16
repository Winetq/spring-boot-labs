package aui.properties

import software.amazon.awscdk.services.ec2.ISecurityGroup
import software.amazon.awscdk.services.ec2.IVpc

data class VpcLinkProperties(
    val name: String,
    val vpc: IVpc,
    val securityGroup: ISecurityGroup,
)
