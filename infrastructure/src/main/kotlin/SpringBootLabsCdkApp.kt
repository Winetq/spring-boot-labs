package aui

import aui.config.LevelSettings.createAccountSettings
import aui.config.readRegion
import aui.config.readStackName
import aui.stack.SpringBootLabsStack
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
            "SpringBootLabsStack" -> {
                SpringBootLabsStack(
                    scope = app,
                    stackId = "SpringBootLabsStack",
                    props = StackProps.builder()
                        .env(settings.environment)
                        .build(),
                    region = region,
                )
            }
        }

        app.synth()
    }
}
