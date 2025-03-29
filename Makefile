clean-run:
	./gradlew clean build
	java -jar build/libs/VK-quote-bot-0.0.1-SNAPSHOT.jar

run:
	./gradlew build
	java -jar build/libs/VK-quote-bot-0.0.1-SNAPSHOT.jar

export-env:
	./env.sh
