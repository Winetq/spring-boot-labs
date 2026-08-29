package aui.construct

import aui.constants.MonitoringConstants.ALARM_NOTIFICATION_EMAIL
import aui.constants.MonitoringConstants.ALARMS_SNS_TOPIC_NAME
import aui.constants.InfrastructureConstants.NAME_TAG_KEY
import aui.properties.SnsTopicProperties
import software.amazon.awscdk.Tags
import software.amazon.awscdk.services.sns.ITopic
import software.amazon.awscdk.services.sns.Topic
import software.amazon.awscdk.services.sns.subscriptions.EmailSubscription
import software.constructs.Construct

class SnsTopicConstruct(
    scope: Construct,
    id: String,
    snsTopicProperties: SnsTopicProperties,
) : Construct(scope, id) {

    val topic: ITopic =
        Topic.Builder.create(this, "Topic")
            .topicName(snsTopicProperties.topicName)
            .build()

    init {
        Tags.of(this).add(NAME_TAG_KEY, snsTopicProperties.topicName)
        // Confirming the subscription requires clicking the link AWS emails once, after the first deploy.
        topic.addSubscription(EmailSubscription(snsTopicProperties.emailAddress))
    }

    companion object {
        fun createAlarmsSnsTopicProperties(): SnsTopicProperties =
            SnsTopicProperties(
                topicName = ALARMS_SNS_TOPIC_NAME,
                emailAddress = ALARM_NOTIFICATION_EMAIL,
            )
    }
}
