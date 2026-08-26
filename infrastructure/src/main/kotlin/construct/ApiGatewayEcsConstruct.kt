package aui.construct

import aui.constants.InfrastructureConstants.API_NAME
import aui.constants.InfrastructureConstants.COACHES_PATH
import aui.constants.InfrastructureConstants.OKTA_AUDIENCE
import aui.constants.InfrastructureConstants.OKTA_ISSUER_URL
import aui.constants.InfrastructureConstants.SWIMMERS_PATH
import aui.properties.ApiGatewayEcsProperties
import software.amazon.awscdk.aws_apigatewayv2_authorizers.HttpJwtAuthorizer
import software.amazon.awscdk.aws_apigatewayv2_authorizers.HttpJwtAuthorizerProps
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpAlbIntegration
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpAlbIntegrationProps
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions
import software.amazon.awscdk.services.apigatewayv2.CorsHttpMethod.DELETE
import software.amazon.awscdk.services.apigatewayv2.CorsHttpMethod.GET
import software.amazon.awscdk.services.apigatewayv2.CorsHttpMethod.OPTIONS
import software.amazon.awscdk.services.apigatewayv2.CorsHttpMethod.POST
import software.amazon.awscdk.services.apigatewayv2.CorsHttpMethod.PUT
import software.amazon.awscdk.services.apigatewayv2.CorsPreflightOptions
import software.amazon.awscdk.services.apigatewayv2.HttpApi
import software.amazon.awscdk.services.apigatewayv2.HttpMethod
import software.amazon.awscdk.services.apigatewayv2.HttpRouteIntegration
import software.amazon.awscdk.services.apigatewayv2.IVpcLink
import software.amazon.awscdk.services.elasticloadbalancingv2.IApplicationListener
import software.constructs.Construct

class ApiGatewayEcsConstruct(
    scope: Construct,
    id: String,
    apiGatewayProperties: ApiGatewayEcsProperties,
) : Construct(scope, id) {

    val httpApi: HttpApi

    init {
        // Validates the Okta access token at the edge, mirroring the old Spring Cloud Gateway.
        val authorizer = HttpJwtAuthorizer(
            "OktaJwtAuthorizer",
            apiGatewayProperties.issuerUrl,
            HttpJwtAuthorizerProps.builder()
                .jwtAudience(listOf(apiGatewayProperties.audience))
                .identitySource(listOf("\$request.header.Authorization"))
                .build()
        )

        httpApi = HttpApi.Builder.create(this, "HttpApi")
            .apiName(apiGatewayProperties.apiName)
            .corsPreflight(
                CorsPreflightOptions.builder()
                    .allowOrigins(listOf(apiGatewayProperties.allowedOrigin))
                    .allowMethods(listOf(GET, POST, DELETE, PUT, OPTIONS))
                    .allowHeaders(listOf("*"))
                    .build()
            )
            // Applied to every route we define. The authorizer does NOT automatically skip the
            // OPTIONS preflight - it stays public only because our routes exclude OPTIONS (see
            // addProxyRoutes), so the API's built-in CORS preflight answers it instead.
            .defaultAuthorizer(authorizer)
            .build()

        // One integration is enough for all routes: the ALB itself routes by path to coach or
        // swimmer. (The EC2 variant needs a separate integration per service because the target
        // URL is fixed per integration.)
        val albIntegration = HttpAlbIntegration(
            "AlbIntegration",
            apiGatewayProperties.listener,
            HttpAlbIntegrationProps.builder()
                .vpcLink(apiGatewayProperties.vpcLink)
                .build()
        )

        addProxyRoutes(COACHES_PATH, albIntegration)
        addProxyRoutes(SWIMMERS_PATH, albIntegration)
    }

    // Prefix-based proxying (e.g. /coaches and /coaches/{proxy+}) instead of listing every endpoint.
    // Only the real HTTP methods are routed - OPTIONS is deliberately excluded so the API's
    // automatic CORS preflight handles it publicly, instead of the JWT authorizer rejecting it.
    private fun addProxyRoutes(pathPrefix: String, integration: HttpRouteIntegration) {
        val methods = listOf(HttpMethod.GET, HttpMethod.POST, HttpMethod.DELETE, HttpMethod.PUT)
        httpApi.addRoutes(
            AddRoutesOptions.builder()
                .path("/$pathPrefix")
                .methods(methods)
                .integration(integration)
                .build()
        )
        httpApi.addRoutes(
            AddRoutesOptions.builder()
                .path("/$pathPrefix/{proxy+}")
                .methods(methods)
                .integration(integration)
                .build()
        )
    }

    companion object {
        fun createApiGatewayProperties(
            listener: IApplicationListener,
            vpcLink: IVpcLink,
            allowedOrigin: String,
        ): ApiGatewayEcsProperties =
            ApiGatewayEcsProperties(
                apiName = API_NAME,
                listener = listener,
                vpcLink = vpcLink,
                issuerUrl = OKTA_ISSUER_URL,
                audience = OKTA_AUDIENCE,
                allowedOrigin = allowedOrigin,
            )
    }
}
