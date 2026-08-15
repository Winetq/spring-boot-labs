package aui.properties

import software.amazon.awscdk.services.ec2.ISecurityGroup
import software.amazon.awscdk.services.ec2.IVpc
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationListener
import software.amazon.awscdk.services.secretsmanager.ISecret

data class EcsProperties(
    val clusterName: String,
    val vpc: IVpc,
    val securityGroup: ISecurityGroup,
    val listener: ApplicationListener,
    val dbSecret: ISecret,
    val dbHost: String,
    val dbReadHost: String,
    val mqSecret: ISecret,
    val mqHost: String,
    val services: List<EcsServiceSpec>,
) {

    data class EcsServiceSpec(
        val serviceName: String,
        val image: String,
        val containerPort: Int,
        val databaseName: String,
        val pathPatterns: List<String>,
        val listenerPriority: Int,
    )
}
