package aui.properties

import software.amazon.awscdk.services.ec2.IPeer
import software.amazon.awscdk.services.ec2.IVpc
import software.amazon.awscdk.services.ec2.Port

data class SecurityGroupProperties(
    val name: String,
    val description: String,
    val vpc: IVpc,
    val inboundRules: List<SecurityGroupRule> = emptyList(),
) {
    data class SecurityGroupRule(
        val peer: IPeer,
        val port: Port,
        val description: String,
    )
}
