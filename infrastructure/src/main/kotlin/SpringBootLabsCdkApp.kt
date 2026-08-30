package aui

import aui.config.LevelSettings.createAccountSettings
import aui.config.readRegion
import aui.config.readStackName
import aui.stack.SiteCertificateStack
import aui.stack.SpringBootLabsEc2Stack
import aui.stack.SpringBootLabsEcsStack
import software.amazon.awscdk.App
import software.amazon.awscdk.StackProps

object SpringBootLabsCdkApp {

    private const val US_EAST_1 = "us-east-1"

    @JvmStatic
    fun main(args: Array<String>) {
        val app = App()
        val region = app.readRegion()
        val stackName = app.readStackName()
        val settings = createAccountSettings(region)

        // The CloudFront certificate must live in us-east-1, so it gets its own stack there. Both
        // this stack and the main stack enable crossRegionReferences so the ARN can cross regions.
        //
        // It is created outside the `when` on purpose: the main stacks reference
        // certificateStack.certificate, so both stacks must exist in the same App during synth for
        // crossRegionReferences to wire the export/import. Deploy the certificate stack first
        // (targeting us-east-1), then the main stack.
        val certificateStack = SiteCertificateStack(
            scope = app,
            stackId = "SiteCertificateStack",
            props = StackProps.builder()
                .env(createAccountSettings(US_EAST_1).environment)
                .crossRegionReferences(true)
                .build(),
        )

        when (stackName) {
            "SpringBootLabsEc2Stack" -> {
                SpringBootLabsEc2Stack(
                    scope = app,
                    stackId = "SpringBootLabsEc2Stack",
                    props = StackProps.builder()
                        .env(settings.environment)
                        .crossRegionReferences(true)
                        .build(),
                    region = region,
                    certificate = certificateStack.certificate,
                )
            }
            "SpringBootLabsEcsStack" -> {
                SpringBootLabsEcsStack(
                    scope = app,
                    stackId = "SpringBootLabsEcsStack",
                    props = StackProps.builder()
                        .env(settings.environment)
                        .crossRegionReferences(true)
                        .build(),
                    certificate = certificateStack.certificate,
                )
            }
        }

        app.synth()
    }
}
