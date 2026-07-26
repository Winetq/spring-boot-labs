package aui.config

import software.amazon.awscdk.Environment

object LevelSettings {

    const val PERSONAL_AWS = "236182043402"

    fun createAccountSettings(region: String): AccountSettings =
        AccountSettings(
            environment = Environment.builder()
                .account(PERSONAL_AWS)
                .region(region)
                .build(),
        )
}
