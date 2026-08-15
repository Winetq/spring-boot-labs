package aui.construct

import aui.constants.InfrastructureConstants.COACHES_PATH
import aui.constants.InfrastructureConstants.COACH_IMAGE
import aui.constants.InfrastructureConstants.COACH_PORT
import aui.constants.InfrastructureConstants.COACH_DATABASE_NAME
import aui.constants.InfrastructureConstants.COACH_SERVICE_NAME
import aui.constants.InfrastructureConstants.DESIRED_COUNT
import aui.constants.InfrastructureConstants.ECS_CLUSTER_NAME
import aui.constants.InfrastructureConstants.FARGATE_CPU
import aui.constants.InfrastructureConstants.FARGATE_MEMORY_MIB
import aui.constants.InfrastructureConstants.HEALTHY_HTTP_CODES
import aui.constants.InfrastructureConstants.HEALTH_CHECK_PATH
import aui.constants.InfrastructureConstants.MQ_PORT
import aui.constants.InfrastructureConstants.NAME_TAG_KEY
import aui.constants.InfrastructureConstants.POSTGRES_INIT_IMAGE
import aui.constants.InfrastructureConstants.POSTGRES_PORT
import aui.constants.InfrastructureConstants.SWIMMERS_PATH
import aui.constants.InfrastructureConstants.SWIMMER_IMAGE
import aui.constants.InfrastructureConstants.SWIMMER_PORT
import aui.constants.InfrastructureConstants.SWIMMER_DATABASE_NAME
import aui.constants.InfrastructureConstants.SWIMMER_SERVICE_NAME
import aui.properties.EcsProperties
import aui.properties.EcsProperties.EcsServiceSpec
import software.amazon.awscdk.Tags
import software.amazon.awscdk.services.ec2.ISecurityGroup
import software.amazon.awscdk.services.ec2.IVpc
import software.amazon.awscdk.services.ec2.SubnetSelection
import software.amazon.awscdk.services.ec2.SubnetType.PUBLIC
import software.amazon.awscdk.services.ecs.Cluster
import software.amazon.awscdk.services.ecs.AwsLogDriverProps
import software.amazon.awscdk.services.ecs.ContainerDefinitionOptions
import software.amazon.awscdk.services.ecs.ContainerDependency
import software.amazon.awscdk.services.ecs.ContainerDependencyCondition.SUCCESS
import software.amazon.awscdk.services.ecs.ContainerImage
import software.amazon.awscdk.services.ecs.FargateService
import software.amazon.awscdk.services.ecs.FargateTaskDefinition
import software.amazon.awscdk.services.ecs.LoadBalancerTargetOptions
import software.amazon.awscdk.services.ecs.LogDriver
import software.amazon.awscdk.services.ecs.PortMapping
import software.amazon.awscdk.services.ecs.Protocol.TCP
import software.amazon.awscdk.services.ecs.Secret as EcsSecret
import software.amazon.awscdk.services.elasticloadbalancingv2.AddApplicationTargetsProps
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationListener
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationProtocol.HTTP
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck
import software.amazon.awscdk.services.elasticloadbalancingv2.ListenerCondition
import software.amazon.awscdk.services.secretsmanager.ISecret
import software.constructs.Construct

class EcsConstruct(
    scope: Construct,
    id: String,
    ecsProperties: EcsProperties,
) : Construct(scope, id) {

    val cluster: Cluster =
        Cluster.Builder.create(this, "Cluster")
            .clusterName(ecsProperties.clusterName)
            .vpc(ecsProperties.vpc)
            .build()

    init {
        Tags.of(this).add(NAME_TAG_KEY, ecsProperties.clusterName)
        ecsProperties.services.forEach { service -> createService(ecsProperties, service) }
    }

    private fun createService(ecsProperties: EcsProperties, spec: EcsServiceSpec) {
        // PascalCase name used only for CDK construct IDs, to match the rest of the codebase
        // (e.g. "Database", "Cluster"). Container/service names stay lowercase (coach/swimmer).
        val name = spec.serviceName.replaceFirstChar { it.uppercase() }

        val taskDefinition = FargateTaskDefinition.Builder.create(this, "${name}TaskDef")
            .cpu(FARGATE_CPU)
            .memoryLimitMiB(FARGATE_MEMORY_MIB)
            .build()

        // One-shot init container: waits for RDS and creates the service database if missing,
        // mirroring the psql bootstrap the EC2 stack runs in user-data.
        val initContainer = taskDefinition.addContainer(
            "${name}DbInit",
            ContainerDefinitionOptions.builder()
                .containerName("${spec.serviceName}-db-init")
                .image(ContainerImage.fromRegistry(POSTGRES_INIT_IMAGE))
                .essential(false)
                // Overrides the image's default entrypoint to run an arbitrary shell
                // script (initDatabaseScript) passed as the -c argument instead of starting Postgres.
                .entryPoint(listOf("sh", "-c"))
                .command(listOf(initDatabaseScript(ecsProperties.dbHost, spec.databaseName)))
                .secrets(
                    mapOf(
                        "DB_USER" to EcsSecret.fromSecretsManager(ecsProperties.dbSecret, "username"),
                        "PGPASSWORD" to EcsSecret.fromSecretsManager(ecsProperties.dbSecret, "password"),
                    )
                )
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder().streamPrefix("${spec.serviceName}-db-init").build()))
                .build()
        )

        val appContainer = taskDefinition.addContainer(
            name,
            ContainerDefinitionOptions.builder()
                .containerName(spec.serviceName)
                .image(ContainerImage.fromRegistry(spec.image))
                .portMappings(
                    listOf(
                        PortMapping.builder()
                            .containerPort(spec.containerPort)
                            .protocol(TCP)
                            .build()
                    )
                )
                .environment(
                    mapOf(
                        "SERVER_PORT" to spec.containerPort.toString(),
                        "POSTGRES_HOST" to ecsProperties.dbHost,
                        "POSTGRES_READ_HOST" to ecsProperties.dbReadHost,
                        "POSTGRES_PORT" to POSTGRES_PORT.toString(),
                        "POSTGRES_DATABASE" to spec.databaseName,
                        "RABBIT_HOST" to ecsProperties.mqHost,
                        "RABBIT_PORT" to MQ_PORT.toString(),
                        "RABBIT_SSL_ENABLED" to "true",
                    )
                )
                .secrets(
                    mapOf(
                        "POSTGRES_USER" to EcsSecret.fromSecretsManager(ecsProperties.dbSecret, "username"),
                        "POSTGRES_PASSWORD" to EcsSecret.fromSecretsManager(ecsProperties.dbSecret, "password"),
                        "RABBIT_USER" to EcsSecret.fromSecretsManager(ecsProperties.mqSecret, "username"),
                        "RABBIT_PASSWORD" to EcsSecret.fromSecretsManager(ecsProperties.mqSecret, "password"),
                    )
                )
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder().streamPrefix(spec.serviceName).build()))
                .build()
        )

        // The app must not start until the database exists.
        appContainer.addContainerDependencies(
            ContainerDependency.builder()
                .container(initContainer)
                .condition(SUCCESS)
                .build()
        )

        val service = FargateService.Builder.create(this, "${name}Service")
            .serviceName(spec.serviceName)
            .cluster(cluster)
            .taskDefinition(taskDefinition)
            .desiredCount(DESIRED_COUNT)
            // Public subnets + a public IP let tasks reach Docker Hub, Secrets Manager and RDS/MQ
            // without a (paid) NAT gateway, matching the no-NAT setup of the EC2 stack.
            .assignPublicIp(true)
            .vpcSubnets(
                SubnetSelection.builder()
                    .subnetType(PUBLIC)
                    .build()
            )
            .securityGroups(listOf(ecsProperties.securityGroup))
            .build()

        ecsProperties.listener.addTargets(
            "${name}Targets",
            AddApplicationTargetsProps.builder()
                // Unique rule priority (ALB requires it); rules are evaluated low-to-high and the
                // first match wins. Order is irrelevant here since /coaches* and /swimmers* are
                // disjoint - the gaps (10, 20) just leave room to slot rules in between later.
                .priority(spec.listenerPriority)
                .conditions(listOf(ListenerCondition.pathPatterns(spec.pathPatterns)))
                .port(spec.containerPort)
                .protocol(HTTP)
                .targets(
                    listOf(
                        service.loadBalancerTarget(
                            LoadBalancerTargetOptions.builder()
                                .containerName(spec.serviceName)
                                .containerPort(spec.containerPort)
                                .build()
                        )
                    )
                )
                .healthCheck(
                    HealthCheck.builder()
                        .path(HEALTH_CHECK_PATH)
                        .healthyHttpCodes(HEALTHY_HTTP_CODES)
                        .build()
                )
                .build()
        )
    }

    private fun initDatabaseScript(dbHost: String, databaseName: String): String =
        """
        until pg_isready -h $dbHost -U "${'$'}DB_USER" -d postgres; do echo 'waiting for rds...'; sleep 5; done
        psql -h $dbHost -U "${'$'}DB_USER" -d postgres -tc "SELECT 1 FROM pg_database WHERE datname='$databaseName'" | grep -q 1 || psql -h $dbHost -U "${'$'}DB_USER" -d postgres -c "CREATE DATABASE $databaseName"
        """.trimIndent()

    companion object {
        fun createEcsProperties(
            vpc: IVpc,
            securityGroup: ISecurityGroup,
            listener: ApplicationListener,
            dbSecret: ISecret,
            dbHost: String,
            dbReadHost: String,
            mqSecret: ISecret,
            mqHost: String,
        ): EcsProperties =
            EcsProperties(
                clusterName = ECS_CLUSTER_NAME,
                vpc = vpc,
                securityGroup = securityGroup,
                listener = listener,
                dbSecret = dbSecret,
                dbHost = dbHost,
                dbReadHost = dbReadHost,
                mqSecret = mqSecret,
                mqHost = mqHost,
                services = listOf(
                    EcsServiceSpec(
                        serviceName = COACH_SERVICE_NAME,
                        image = COACH_IMAGE,
                        containerPort = COACH_PORT,
                        databaseName = COACH_DATABASE_NAME,
                        pathPatterns = listOf("/$COACHES_PATH", "/$COACHES_PATH/*"),
                        listenerPriority = 10,
                    ),
                    EcsServiceSpec(
                        serviceName = SWIMMER_SERVICE_NAME,
                        image = SWIMMER_IMAGE,
                        containerPort = SWIMMER_PORT,
                        databaseName = SWIMMER_DATABASE_NAME,
                        pathPatterns = listOf("/$SWIMMERS_PATH", "/$SWIMMERS_PATH/*"),
                        listenerPriority = 20,
                    ),
                ),
            )
    }
}
