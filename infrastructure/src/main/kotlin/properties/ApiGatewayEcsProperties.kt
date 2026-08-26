package aui.properties

import software.amazon.awscdk.services.apigatewayv2.IVpcLink
import software.amazon.awscdk.services.elasticloadbalancingv2.IApplicationListener

data class ApiGatewayEcsProperties(
    val apiName: String,
    val listener: IApplicationListener,
    val vpcLink: IVpcLink,
    val issuerUrl: String,
    val audience: String,
    val allowedOrigin: String,
)
