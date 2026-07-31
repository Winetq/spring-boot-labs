package aui.properties

import software.amazon.awscdk.services.ec2.ISecurityGroup
import software.amazon.awscdk.services.ec2.IVpc

data class RdsInstanceProperties(
    val instanceIdentifier: String,
    val vpc: IVpc,
    val securityGroup: ISecurityGroup,
    val masterUsername: String,
    val allocatedStorage: Int,
)
