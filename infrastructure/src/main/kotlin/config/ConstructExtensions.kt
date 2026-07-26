package aui.config

import aui.constants.InfrastructureConstants.REGION
import aui.constants.InfrastructureConstants.STACK_NAME
import software.constructs.Construct

fun Construct.readRegion(): String = this.readContextVariable(REGION)

fun Construct.readStackName(): String = this.readContextVariable(STACK_NAME)

fun Construct.readContextVariable(key: String): String = this.node.tryGetContext(key) as String
