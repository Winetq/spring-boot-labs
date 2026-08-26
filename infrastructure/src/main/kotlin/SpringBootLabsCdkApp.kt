package aui

import aui.config.LevelSettings.createAccountSettings
import aui.config.readRegion
import aui.config.readStackName
import aui.stack.SpringBootLabsEc2Stack
import aui.stack.SpringBootLabsEcsStack
import software.amazon.awscdk.App
import software.amazon.awscdk.StackProps

object SpringBootLabsCdkApp {

    @JvmStatic
    fun main(args: Array<String>) {
        val app = App()
        val region = app.readRegion()
        val stackName = app.readStackName()
        val settings = createAccountSettings(region)

        when (stackName) {
            "SpringBootLabsEc2Stack" -> {
                SpringBootLabsEc2Stack(
                    scope = app,
                    stackId = "SpringBootLabsEc2Stack",
                    props = StackProps.builder()
                        .env(settings.environment)
                        .build(),
                    region = region,
                )
            }
            "SpringBootLabsEcsStack" -> {
                SpringBootLabsEcsStack(
                    scope = app,
                    stackId = "SpringBootLabsEcsStack",
                    props = StackProps.builder()
                        .env(settings.environment)
                        .build(),
                )
            }
        }

        app.synth()
    }
}
