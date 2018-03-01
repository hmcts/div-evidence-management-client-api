create-sonar-local:
	docker pull sonarqube:latest
	docker run -d --restart=always -p9000:9000 sonarqube:latest

dependency-check:
	mvn dependency-check:check

clean-install:
	mvn clean install

lint-all:
	mvn -DcompilerArgument=-Xlint:all compile

run-emclient:
	mvn clean
	mvn spring-boot:run

compile-emclient:
	mvn compile

generate-sonar-report-local:
	mvn clean install sonar:sonar -Dsonar.host.url=http://localhost:9000

run-test:
	mvn test

make-start:
	docker-compose up
