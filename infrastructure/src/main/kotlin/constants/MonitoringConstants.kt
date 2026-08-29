package aui.constants

object MonitoringConstants {

    // SNS topic that fans CloudWatch alarms out to email.
    const val ALARMS_SNS_TOPIC_NAME = "spring-boot-labs-alarms"
    const val ALARM_NOTIFICATION_EMAIL = "michalcwynar99@gmail.com"

    // The CPU metric is read at 1-minute resolution; alarm when it stays at/above the target for
    // 2 of the last 2 minutes, so a brief spike doesn't page us but sustained load does.
    const val ALARM_EVALUATION_PERIODS = 2
    const val ALARM_DATAPOINTS_TO_ALARM = 2

    // Page as soon as the ALB reports at least one target failing its /actuator/health check.
    const val UNHEALTHY_HOST_THRESHOLD = 1
}
