package aui.construct

import aui.constants.MonitoringConstants.ALARM_DATAPOINTS_TO_ALARM
import aui.constants.MonitoringConstants.ALARM_EVALUATION_PERIODS
import aui.constants.MonitoringConstants.UNHEALTHY_HOST_THRESHOLD
import aui.constants.InfrastructureConstants.SCALING_TARGET_CPU_PERCENT
import aui.properties.CloudWatchAlarmProperties
import software.amazon.awscdk.services.cloudwatch.Alarm
import software.amazon.awscdk.services.cloudwatch.ComparisonOperator.GREATER_THAN_OR_EQUAL_TO_THRESHOLD
import software.amazon.awscdk.services.cloudwatch.IMetric
import software.amazon.awscdk.services.cloudwatch.TreatMissingData.NOT_BREACHING
import software.amazon.awscdk.services.cloudwatch.actions.SnsAction
import software.amazon.awscdk.services.sns.ITopic
import software.constructs.Construct

class CloudWatchAlarmConstruct(
    scope: Construct,
    id: String,
    cloudWatchAlarmProperties: CloudWatchAlarmProperties,
) : Construct(scope, id) {

    init {
        val alarm: Alarm = Alarm.Builder.create(this, "Alarm")
            .alarmName(cloudWatchAlarmProperties.name)
            .alarmDescription(cloudWatchAlarmProperties.description)
            .evaluationPeriods(cloudWatchAlarmProperties.evaluationPeriods)
            .threshold(cloudWatchAlarmProperties.threshold)
            .datapointsToAlarm(cloudWatchAlarmProperties.datapointsToAlarm)
            .comparisonOperator(cloudWatchAlarmProperties.comparisonOperator)
            .treatMissingData(cloudWatchAlarmProperties.treatMissingData)
            .metric(cloudWatchAlarmProperties.metric)
            .actionsEnabled(true)
            .build()
        alarm.addAlarmAction(cloudWatchAlarmProperties.alarmAction)
    }

    companion object {
        fun createHighCpuAlarmProperties(
            serviceName: String,
            cpuMetric: IMetric,
            snsTopic: ITopic,
        ): CloudWatchAlarmProperties =
            CloudWatchAlarmProperties(
                name = "spring-boot-labs-$serviceName-high-cpu",
                description = "Average CPU of the $serviceName ECS service stayed at/above " +
                    "$SCALING_TARGET_CPU_PERCENT% (the auto-scaling target) for " +
                    "$ALARM_DATAPOINTS_TO_ALARM consecutive minutes",
                evaluationPeriods = ALARM_EVALUATION_PERIODS,
                threshold = SCALING_TARGET_CPU_PERCENT,
                datapointsToAlarm = ALARM_DATAPOINTS_TO_ALARM,
                comparisonOperator = GREATER_THAN_OR_EQUAL_TO_THRESHOLD,
                treatMissingData = NOT_BREACHING,
                metric = cpuMetric,
                alarmAction = SnsAction(snsTopic),
            )

        fun createUnhealthyHostsAlarmProperties(
            serviceName: String,
            unhealthyHostCountMetric: IMetric,
            snsTopic: ITopic,
        ): CloudWatchAlarmProperties =
            CloudWatchAlarmProperties(
                name = "spring-boot-labs-$serviceName-unhealthy-hosts",
                description = "The ALB reported at least $UNHEALTHY_HOST_THRESHOLD unhealthy " +
                    "$serviceName task (failing /actuator/health) for " +
                    "$ALARM_DATAPOINTS_TO_ALARM consecutive minutes",
                evaluationPeriods = ALARM_EVALUATION_PERIODS,
                threshold = UNHEALTHY_HOST_THRESHOLD,
                datapointsToAlarm = ALARM_DATAPOINTS_TO_ALARM,
                comparisonOperator = GREATER_THAN_OR_EQUAL_TO_THRESHOLD,
                treatMissingData = NOT_BREACHING,
                metric = unhealthyHostCountMetric,
                alarmAction = SnsAction(snsTopic),
            )
    }
}
