package aui.construct

import aui.constants.InfrastructureConstants.NAME_TAG_KEY
import aui.constants.InfrastructureConstants.VPC_LINK_NAME
import aui.properties.VpcLinkProperties
import software.amazon.awscdk.Tags
import software.amazon.awscdk.services.apigatewayv2.VpcLink
import software.amazon.awscdk.services.ec2.ISecurityGroup
import software.amazon.awscdk.services.ec2.IVpc
import software.amazon.awscdk.services.ec2.SubnetSelection
import software.amazon.awscdk.services.ec2.SubnetType.PUBLIC
import software.constructs.Construct

class VpcLinkConstruct(
    scope: Construct,
    id: String,
    vpcLinkProperties: VpcLinkProperties,
) : Construct(scope, id) {

    // Private channel that lets the public HTTP API forward requests to the internal ALB.
    // Its ENIs (Elastic network interfaces) live in the (public) VPC subnets and reach the ALB via its private IPs.
    val vpcLink: VpcLink =
        VpcLink.Builder.create(this, "VpcLink")
            .vpcLinkName(vpcLinkProperties.name)
            .vpc(vpcLinkProperties.vpc)
            .subnets(
                SubnetSelection.builder()
                    .subnetType(PUBLIC)
                    .build()
            )
            .securityGroups(listOf(vpcLinkProperties.securityGroup))
            .build()

    init {
        Tags.of(this).add(NAME_TAG_KEY, vpcLinkProperties.name)
    }

    companion object {
        fun createVpcLinkProperties(
            vpc: IVpc,
            securityGroup: ISecurityGroup,
        ): VpcLinkProperties =
            VpcLinkProperties(
                name = VPC_LINK_NAME,
                vpc = vpc,
                securityGroup = securityGroup,
            )
    }
}
