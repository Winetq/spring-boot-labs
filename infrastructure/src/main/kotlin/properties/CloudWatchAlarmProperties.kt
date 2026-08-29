package aui.properties

import software.amazon.awscdk.services.cloudwatch.ComparisonOperator
import software.amazon.awscdk.services.cloudwatch.IAlarmAction
import software.amazon.awscdk.services.cloudwatch.IMetric
import software.amazon.awscdk.services.cloudwatch.TreatMissingData

data class CloudWatchAlarmProperties(
    val name: String,
    val description: String,
    val evaluationPeriods: Int,
    val threshold: Int,
    val datapointsToAlarm: Int,
    val comparisonOperator: ComparisonOperator,
    val treatMissingData: TreatMissingData,
    val metric: IMetric,
    val alarmAction: IAlarmAction,
)
