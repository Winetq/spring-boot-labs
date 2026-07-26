package aui.constants

object InfrastructureConstants {

    const val REGION = "REGION"
    const val STACK_NAME = "STACK_NAME"

    const val NAME_TAG_KEY = "Name"

    // EC2 instances
    const val COACH_INSTANCE_NAME = "coach"
    const val SWIMMER_INSTANCE_NAME = "swimmer"
    const val INSTANCE_TYPE = "t3.micro"

    // Application ports
    const val COACH_PORT = 8081
    const val SWIMMER_PORT = 8082
    const val SSH_PORT = 22

    // Security group
    // NOTE: 0.0.0.0/0 is open to the whole internet - fine for quick testing,
    // but narrow this down to your IP (e.g. "x.x.x.x/32") for anything real.
    const val ALLOWED_INGRESS_CIDR = "0.0.0.0/0"

    // Docker Hub images
    const val COACH_IMAGE = "mcwynar/spring-boot-labs:coach"
    const val SWIMMER_IMAGE = "mcwynar/spring-boot-labs:swimmer"
}
