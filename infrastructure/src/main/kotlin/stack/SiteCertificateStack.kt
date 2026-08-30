package aui.stack

import aui.constants.InfrastructureConstants.SITE_DOMAIN_NAME
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.certificatemanager.Certificate
import software.amazon.awscdk.services.certificatemanager.CertificateValidation
import software.amazon.awscdk.services.certificatemanager.ICertificate
import software.amazon.awscdk.services.route53.HostedZone
import software.amazon.awscdk.services.route53.HostedZoneProviderProps
import software.constructs.Construct

/**
 * CloudFront only accepts ACM certificates issued in us-east-1, so this stack is pinned to that
 * region while the rest of the app lives in eu-central-1. Whichever main stack is deployed imports
 * the certificate through a cross-region reference (both sides set crossRegionReferences = true).
 */
class SiteCertificateStack(
    scope: Construct,
    stackId: String,
    props: StackProps,
) : Stack(scope, stackId, props) {

    val certificate: ICertificate

    init {
        val hostedZone = HostedZone.fromLookup(
            this,
            "SiteHostedZone",
            HostedZoneProviderProps.builder()
                .domainName(SITE_DOMAIN_NAME)
                .build()
        )

        certificate = Certificate.Builder.create(this, "SiteCertificate")
            .domainName(SITE_DOMAIN_NAME)
            // DNS validation writes the proof CNAME straight into the Route 53 zone, so issuance is
            // fully automated - no manual record to paste, no deploy hanging on manual validation.
            .validation(CertificateValidation.fromDns(hostedZone))
            .build()
    }
}
