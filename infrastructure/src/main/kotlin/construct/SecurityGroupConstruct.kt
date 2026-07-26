package aui.construct

import aui.constants.InfrastructureConstants.ALLOWED_INGRESS_CIDR
import aui.constants.InfrastructureConstants.COACH_PORT
import aui.constants.InfrastructureConstants.NAME_TAG_KEY
import aui.constants.InfrastructureConstants.SSH_PORT
import aui.constants.InfrastructureConstants.SWIMMER_PORT
import aui.properties.SecurityGroupProperties
import aui.properties.SecurityGroupProperties.SecurityGroupRule
import software.amazon.awscdk.Tags
import software.amazon.awscdk.services.ec2.IVpc
import software.amazon.awscdk.services.ec2.Peer
import software.amazon.awscdk.services.ec2.Port
import software.amazon.awscdk.services.ec2.SecurityGroup
import software.constructs.Construct

class SecurityGroupConstruct(
    scope: Construct,
    id: String,
    securityGroupProperties: SecurityGroupProperties,
) : Construct(scope, id) {

    val securityGroup: SecurityGroup =
        SecurityGroup.Builder.create(this, "SecurityGroup")
            .securityGroupName(securityGroupProperties.name)
            .vpc(securityGroupProperties.vpc)
            .description(securityGroupProperties.description)
            .allowAllOutbound(true)
            .disableInlineRules(true)
            .build()

    init {
        Tags.of(this).add(NAME_TAG_KEY, securityGroupProperties.name)
        securityGroupProperties.inboundRules.forEach { rule ->
            securityGroup.addIngressRule(rule.peer, rule.port, rule.description)
        }
    }

    companion object {
        fun createEc2InstancesSecurityGroupProperties(vpc: IVpc): SecurityGroupProperties =
            SecurityGroupProperties(
                name = "spring-boot-labs-ec2-instances-sg",
                description = "Security group for coach and swimmer EC2 instances",
                vpc = vpc,
                inboundRules = listOf(
                    SecurityGroupRule(
                        peer = Peer.ipv4(ALLOWED_INGRESS_CIDR),
                        port = Port.tcp(COACH_PORT),
                        description = "Allow inbound traffic to coach service",
                    ),
                    SecurityGroupRule(
                        peer = Peer.ipv4(ALLOWED_INGRESS_CIDR),
                        port = Port.tcp(SWIMMER_PORT),
                        description = "Allow inbound traffic to swimmer service",
                    ),
                    SecurityGroupRule(
                        peer = Peer.ipv4(ALLOWED_INGRESS_CIDR),
                        port = Port.tcp(SSH_PORT),
                        description = "Allow SSH access", // it enables EC2 Instance Connect
                    ),
                ),
            )
    }
}
