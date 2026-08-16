package aui.construct

import aui.constants.InfrastructureConstants.ALB_LISTENER_PORT
import aui.constants.InfrastructureConstants.ALB_NAME
import aui.constants.InfrastructureConstants.NAME_TAG_KEY
import aui.properties.AlbProperties
import software.amazon.awscdk.Tags
import software.amazon.awscdk.services.ec2.ISecurityGroup
import software.amazon.awscdk.services.ec2.IVpc
import software.amazon.awscdk.services.ec2.SubnetSelection
import software.amazon.awscdk.services.ec2.SubnetType.PUBLIC
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationListener
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationLoadBalancer
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationProtocol.HTTP
import software.amazon.awscdk.services.elasticloadbalancingv2.BaseApplicationListenerProps
import software.amazon.awscdk.services.elasticloadbalancingv2.FixedResponseOptions
import software.amazon.awscdk.services.elasticloadbalancingv2.ListenerAction
import software.constructs.Construct

class AlbConstruct(
    scope: Construct,
    id: String,
    albProperties: AlbProperties,
) : Construct(scope, id) {

    val loadBalancer: ApplicationLoadBalancer =
        ApplicationLoadBalancer.Builder.create(this, "Alb")
            .loadBalancerName(albProperties.name)
            .vpc(albProperties.vpc)
            // Private ALB: it has no public IPs and only the API Gateway VPC Link can reach it
            // (enforced by the ALB security group). API Gateway is the single public entry point.
            .internetFacing(false)
            .crossZoneEnabled(true)
            .securityGroup(albProperties.securityGroup)
            .vpcSubnets(
                SubnetSelection.builder()
                    .subnetType(PUBLIC)
                    .build()
            )
            .build()

    // Single HTTP listener; per-service path rules are added later by the ECS construct.
    // Unmatched paths get a plain 404 so the ALB never forwards stray traffic anywhere.
    val listener: ApplicationListener =
        loadBalancer.addListener(
            "HttpListener",
            BaseApplicationListenerProps.builder()
                .port(ALB_LISTENER_PORT)
                .protocol(HTTP)
                .defaultAction(
                    ListenerAction.fixedResponse(
                        404,
                        FixedResponseOptions.builder()
                            .contentType("text/plain")
                            .messageBody("Not Found")
                            .build()
                    )
                )
                .build()
        )

    val dnsName: String = loadBalancer.loadBalancerDnsName

    init {
        Tags.of(this).add(NAME_TAG_KEY, albProperties.name)
    }

    companion object {
        fun createAlbProperties(
            vpc: IVpc,
            securityGroup: ISecurityGroup,
        ): AlbProperties =
            AlbProperties(
                name = ALB_NAME,
                vpc = vpc,
                securityGroup = securityGroup,
            )
    }
}
