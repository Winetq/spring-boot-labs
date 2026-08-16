package aui.construct

import aui.constants.InfrastructureConstants.API_NAME
import aui.constants.InfrastructureConstants.COACHES_PATH
import aui.constants.InfrastructureConstants.OKTA_AUDIENCE
import aui.constants.InfrastructureConstants.OKTA_ISSUER_URL
import aui.constants.InfrastructureConstants.SWIMMERS_PATH
import aui.properties.ApiGatewayEc2Properties
import software.amazon.awscdk.aws_apigatewayv2_authorizers.HttpJwtAuthorizer
import software.amazon.awscdk.aws_apigatewayv2_authorizers.HttpJwtAuthorizerProps
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpUrlIntegration
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions
import software.amazon.awscdk.services.apigatewayv2.CorsHttpMethod.DELETE
import software.amazon.awscdk.services.apigatewayv2.CorsHttpMethod.GET
import software.amazon.awscdk.services.apigatewayv2.CorsHttpMethod.OPTIONS
import software.amazon.awscdk.services.apigatewayv2.CorsHttpMethod.POST
import software.amazon.awscdk.services.apigatewayv2.CorsHttpMethod.PUT
import software.amazon.awscdk.services.apigatewayv2.CorsPreflightOptions
import software.amazon.awscdk.services.apigatewayv2.HttpApi
import software.amazon.awscdk.services.apigatewayv2.HttpMethod
import software.constructs.Construct

class ApiGatewayEc2Construct(
    scope: Construct,
    id: String,
    apiGatewayProperties: ApiGatewayEc2Properties,
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

        addProxyRoutes(COACHES_PATH, apiGatewayProperties.coachBaseUrl)
        addProxyRoutes(SWIMMERS_PATH, apiGatewayProperties.swimmerBaseUrl)
    }

    // Prefix-based proxying (e.g. /coaches and /coaches/{proxy+}) instead of listing every endpoint.
    // Only the real HTTP methods are routed - OPTIONS is deliberately excluded so the API's
    // automatic CORS preflight handles it publicly, instead of the JWT authorizer rejecting it.
    private fun addProxyRoutes(pathPrefix: String, baseUrl: String) {
        val methods = listOf(HttpMethod.GET, HttpMethod.POST, HttpMethod.DELETE, HttpMethod.PUT)
        httpApi.addRoutes(
            AddRoutesOptions.builder()
                .path("/$pathPrefix")
                .methods(methods)
                .integration(HttpUrlIntegration("$pathPrefix-root", "$baseUrl/$pathPrefix"))
                .build()
        )
        httpApi.addRoutes(
            AddRoutesOptions.builder()
                .path("/$pathPrefix/{proxy+}")
                .methods(methods)
                .integration(HttpUrlIntegration("$pathPrefix-proxy", "$baseUrl/$pathPrefix/{proxy}"))
                .build()
        )
    }

    companion object {
        fun createApiGatewayProperties(
            coachBaseUrl: String,
            swimmerBaseUrl: String,
            allowedOrigin: String,
        ): ApiGatewayEc2Properties =
            ApiGatewayEc2Properties(
                apiName = API_NAME,
                coachBaseUrl = coachBaseUrl,
                swimmerBaseUrl = swimmerBaseUrl,
                issuerUrl = OKTA_ISSUER_URL,
                audience = OKTA_AUDIENCE,
                allowedOrigin = allowedOrigin,
            )
    }
}
