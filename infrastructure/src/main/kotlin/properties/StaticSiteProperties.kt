package aui.properties

import software.amazon.awscdk.services.certificatemanager.ICertificate

data class StaticSiteProperties(
    val siteName: String,
    val sourcePath: String,
    val domainName: String,
    val certificate: ICertificate,
)
