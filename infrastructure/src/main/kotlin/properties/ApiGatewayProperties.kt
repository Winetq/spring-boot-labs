package aui.properties

data class ApiGatewayProperties(
    val apiName: String,
    val coachBaseUrl: String,
    val swimmerBaseUrl: String,
    val issuerUrl: String,
    val audience: String,
    val allowedOrigin: String,
)
