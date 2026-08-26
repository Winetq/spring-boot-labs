package aui.properties

import software.amazon.awscdk.services.ec2.ISecurityGroup
import software.amazon.awscdk.services.ec2.IVpc

data class AlbProperties(
    val name: String,
    val vpc: IVpc,
    val securityGroup: ISecurityGroup,
)
