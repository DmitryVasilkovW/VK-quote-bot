clean-run:
	./gradlew clean build
	java -jar build/libs/VK-quote-bot-1.0.0.jar

run:
	./gradlew build
	java -jar build/libs/VK-quote-bot-1.0.0.jar

export-env:
	./env.sh
