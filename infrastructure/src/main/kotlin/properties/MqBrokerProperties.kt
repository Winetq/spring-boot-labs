package aui.properties

import software.amazon.awscdk.services.ec2.ISecurityGroup
import software.amazon.awscdk.services.ec2.IVpc

data class MqBrokerProperties(
    val brokerName: String,
    val engineVersion: String,
    val hostInstanceType: String,
    val username: String,
    val vpc: IVpc,
    val securityGroup: ISecurityGroup,
)
