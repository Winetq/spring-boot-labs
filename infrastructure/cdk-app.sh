VERSION=$(./gradlew :properties -q | grep -w ^version | awk '{print $2}')

exec java -jar build/libs/infrastructure-"${VERSION}".jar
