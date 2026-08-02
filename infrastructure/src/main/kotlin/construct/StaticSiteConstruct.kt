package aui.construct

import aui.constants.InfrastructureConstants.CLIENT_SITE_NAME
import aui.constants.InfrastructureConstants.CLIENT_SOURCE_PATH
import aui.constants.InfrastructureConstants.NAME_TAG_KEY
import aui.properties.StaticSiteProperties
import software.amazon.awscdk.RemovalPolicy.DESTROY
import software.amazon.awscdk.Tags
import software.amazon.awscdk.services.cloudfront.BehaviorOptions
import software.amazon.awscdk.services.cloudfront.Distribution
import software.amazon.awscdk.services.cloudfront.ViewerProtocolPolicy.REDIRECT_TO_HTTPS
import software.amazon.awscdk.services.cloudfront.origins.S3BucketOrigin
import software.amazon.awscdk.services.s3.BlockPublicAccess.BLOCK_ALL
import software.amazon.awscdk.services.s3.Bucket
import software.amazon.awscdk.services.s3.deployment.BucketDeployment
import software.amazon.awscdk.services.s3.deployment.Source
import software.constructs.Construct

class StaticSiteConstruct(
    scope: Construct,
    id: String,
    private val staticSiteProperties: StaticSiteProperties,
) : Construct(scope, id) {

    private val bucket: Bucket

    // CloudFront domain used as the public HTTPS entry point for the frontend.
    val distributionDomainName: String

    init {
        // Private bucket - only CloudFront (via OAC) can read it, never the public internet.
        bucket = Bucket.Builder.create(this, "SiteBucket")
            .blockPublicAccess(BLOCK_ALL)
            .removalPolicy(DESTROY)
            .autoDeleteObjects(true)
            .build()

        val distribution = Distribution.Builder.create(this, "SiteDistribution")
            .defaultRootObject("index.html")
            .defaultBehavior(
                BehaviorOptions.builder()
                    // OAC (Origin Access Control) is the modern replacement for OAI.
                    .origin(S3BucketOrigin.withOriginAccessControl(bucket))
                    // Okta requires HTTPS for the redirect URI, so force HTTPS.
                    .viewerProtocolPolicy(REDIRECT_TO_HTTPS)
                    .build()
            )
            .build()

        distributionDomainName = distribution.distributionDomainName

        Tags.of(this).add(NAME_TAG_KEY, staticSiteProperties.siteName)
    }

    // Uploads client/ to the bucket. The backend URL is only known once the API Gateway
    // exists, so this runs after it - Source.data overrides configuration.js with the real
    // URL, substituting the CloudFormation token at deploy time (single deploy, no manual sed).
    fun deployContent(backendUrl: String) {
        BucketDeployment.Builder.create(this, "DeploySite")
            .sources(
                listOf(
                    Source.asset(staticSiteProperties.sourcePath),
                    Source.data(CONFIGURATION_OBJECT_KEY, configurationJs(backendUrl)),
                )
            )
            .destinationBucket(bucket)
            .build()
    }

    companion object {
        private const val CONFIGURATION_OBJECT_KEY = "js/configuration.js"

        private fun configurationJs(backendUrl: String): String =
            """
            |/**
            | *
            | * @returns {string} url for backend server (API gateway)
            | */
            |export function getBackendUrl() {
            |    return "$backendUrl"
            |}
            """.trimMargin()

        fun createStaticSiteProperties(): StaticSiteProperties =
            StaticSiteProperties(
                siteName = CLIENT_SITE_NAME,
                sourcePath = CLIENT_SOURCE_PATH,
            )
    }
}
