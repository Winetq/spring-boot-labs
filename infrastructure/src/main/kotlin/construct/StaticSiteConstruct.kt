package aui.construct

import aui.constants.InfrastructureConstants.CLIENT_SITE_NAME
import aui.constants.InfrastructureConstants.CLIENT_SOURCE_PATH
import aui.constants.InfrastructureConstants.NAME_TAG_KEY
import aui.constants.InfrastructureConstants.SITE_DOMAIN_NAME
import aui.properties.StaticSiteProperties
import software.amazon.awscdk.RemovalPolicy.DESTROY
import software.amazon.awscdk.Tags
import software.amazon.awscdk.services.certificatemanager.ICertificate
import software.amazon.awscdk.services.cloudfront.BehaviorOptions
import software.amazon.awscdk.services.cloudfront.Distribution
import software.amazon.awscdk.services.cloudfront.ViewerProtocolPolicy.REDIRECT_TO_HTTPS
import software.amazon.awscdk.services.cloudfront.origins.S3BucketOrigin
import software.amazon.awscdk.services.route53.AaaaRecord
import software.amazon.awscdk.services.route53.ARecord
import software.amazon.awscdk.services.route53.HostedZone
import software.amazon.awscdk.services.route53.HostedZoneProviderProps
import software.amazon.awscdk.services.route53.RecordTarget
import software.amazon.awscdk.services.route53.targets.CloudFrontTarget
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

    // Public HTTPS entry point for the frontend, on the custom domain (not the raw CloudFront URL).
    val siteUrl: String = "https://${staticSiteProperties.domainName}"

    init {
        // Private bucket - only CloudFront (via OAC) can read it, never the public internet.
        bucket = Bucket.Builder.create(this, "SiteBucket")
            .blockPublicAccess(BLOCK_ALL)
            .removalPolicy(DESTROY)
            .autoDeleteObjects(true)
            .build()

        val distribution = Distribution.Builder.create(this, "SiteDistribution")
            .defaultRootObject("index.html")
            // Serve the frontend from the custom domain, secured by the us-east-1 ACM certificate.
            .domainNames(listOf(staticSiteProperties.domainName))
            .certificate(staticSiteProperties.certificate)
            .defaultBehavior(
                BehaviorOptions.builder()
                    // OAC (Origin Access Control) is the modern replacement for OAI.
                    .origin(S3BucketOrigin.withOriginAccessControl(bucket))
                    // Okta requires HTTPS for the redirect URI, so force HTTPS.
                    .viewerProtocolPolicy(REDIRECT_TO_HTTPS)
                    .build()
            )
            .build()

        // The delegated subdomain is a Route 53 hosted zone, so point its apex at CloudFront with
        // alias records (A + AAAA for IPv6). Aliases work at the zone apex and are free to query.
        val hostedZone = HostedZone.fromLookup(
            this,
            "SiteHostedZone",
            HostedZoneProviderProps.builder()
                .domainName(SITE_DOMAIN_NAME)
                .build()
        )
        val aliasTarget = RecordTarget.fromAlias(CloudFrontTarget(distribution))
        ARecord.Builder.create(this, "SiteAliasRecordIpv4")
            .zone(hostedZone)
            .target(aliasTarget)
            .build()
        AaaaRecord.Builder.create(this, "SiteAliasRecordIpv6")
            .zone(hostedZone)
            .target(aliasTarget)
            .build()

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

        fun createStaticSiteProperties(certificate: ICertificate): StaticSiteProperties =
            StaticSiteProperties(
                siteName = CLIENT_SITE_NAME,
                sourcePath = CLIENT_SOURCE_PATH,
                domainName = SITE_DOMAIN_NAME,
                certificate = certificate,
            )
    }
}
