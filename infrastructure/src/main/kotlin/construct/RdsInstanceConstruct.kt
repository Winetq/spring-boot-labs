package aui.construct

import aui.constants.InfrastructureConstants.NAME_TAG_KEY
import aui.constants.InfrastructureConstants.RDS_ALLOCATED_STORAGE
import aui.constants.InfrastructureConstants.RDS_INSTANCE_IDENTIFIER
import aui.constants.InfrastructureConstants.RDS_MASTER_USERNAME
import aui.properties.RdsInstanceProperties
import software.amazon.awscdk.Duration
import software.amazon.awscdk.RemovalPolicy.DESTROY
import software.amazon.awscdk.Tags
import software.amazon.awscdk.services.ec2.ISecurityGroup
import software.amazon.awscdk.services.ec2.IVpc
import software.amazon.awscdk.services.ec2.InstanceClass.BURSTABLE3
import software.amazon.awscdk.services.ec2.InstanceSize.MICRO
import software.amazon.awscdk.services.ec2.InstanceType
import software.amazon.awscdk.services.ec2.SubnetSelection
import software.amazon.awscdk.services.ec2.SubnetType.PUBLIC
import software.amazon.awscdk.services.rds.Credentials
import software.amazon.awscdk.services.rds.DatabaseInstance
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine
import software.amazon.awscdk.services.rds.DatabaseInstanceReadReplica
import software.amazon.awscdk.services.rds.PostgresEngineVersion.VER_18_3
import software.amazon.awscdk.services.rds.PostgresInstanceEngineProps
import software.amazon.awscdk.services.secretsmanager.ISecret
import software.constructs.Construct

class RdsInstanceConstruct(
    scope: Construct,
    id: String,
    rdsInstanceProperties: RdsInstanceProperties,
) : Construct(scope, id) {

    val instance: DatabaseInstance =
        DatabaseInstance.Builder.create(this, "Database")
            .instanceIdentifier(rdsInstanceProperties.instanceIdentifier)
            .engine(
                DatabaseInstanceEngine.postgres(
                    PostgresInstanceEngineProps.builder()
                        .version(VER_18_3)
                        .build()
                )
            )
            .instanceType(InstanceType.of(BURSTABLE3, MICRO)) // db.t3.micro
            .vpc(rdsInstanceProperties.vpc)
            .vpcSubnets(
                SubnetSelection.builder()
                    .subnetType(PUBLIC)
                    .build()
            )
            // Publicly accessible so it can be reached from a local client (e.g. DBeaver).
            // Access is still restricted by the RDS security group to the EC2 SG and the personal IP.
            .publiclyAccessible(true)
            .securityGroups(listOf(rdsInstanceProperties.securityGroup))
            .allocatedStorage(rdsInstanceProperties.allocatedStorage)
            .credentials(Credentials.fromGeneratedSecret(rdsInstanceProperties.masterUsername))
            // At least one day of automated backups is required to create a read replica from this instance.
            .backupRetention(Duration.days(1))
            .deleteAutomatedBackups(true)
            .removalPolicy(DESTROY)
            .build()

    // Read-only replica of the instance above. Gives the app a separate reader endpoint
    // (a poor man's Aurora reader/writer split) so read-only transactions can be offloaded.
    val readReplica: DatabaseInstanceReadReplica =
        DatabaseInstanceReadReplica.Builder.create(this, "ReadReplica")
            .instanceIdentifier("${rdsInstanceProperties.instanceIdentifier}-replica")
            .sourceDatabaseInstance(instance)
            .instanceType(InstanceType.of(BURSTABLE3, MICRO)) // db.t3.micro
            .vpc(rdsInstanceProperties.vpc)
            .vpcSubnets(
                SubnetSelection.builder()
                    .subnetType(PUBLIC)
                    .build()
            )
            .publiclyAccessible(true)
            .securityGroups(listOf(rdsInstanceProperties.securityGroup))
            .removalPolicy(DESTROY)
            .build()

    // Auto-generated master credentials (username + password) stored in Secrets Manager.
    val secret: ISecret = requireNotNull(instance.secret) {
        "RDS instance was created with a generated secret, so it must not be null"
    }

    val endpointAddress: String = instance.dbInstanceEndpointAddress

    val readerEndpointAddress: String = readReplica.dbInstanceEndpointAddress

    init {
        Tags.of(this).add(NAME_TAG_KEY, rdsInstanceProperties.instanceIdentifier)
    }

    companion object {
        fun createRdsInstanceProperties(
            vpc: IVpc,
            securityGroup: ISecurityGroup,
        ): RdsInstanceProperties =
            RdsInstanceProperties(
                instanceIdentifier = RDS_INSTANCE_IDENTIFIER,
                vpc = vpc,
                securityGroup = securityGroup,
                masterUsername = RDS_MASTER_USERNAME,
                allocatedStorage = RDS_ALLOCATED_STORAGE,
            )
    }
}
