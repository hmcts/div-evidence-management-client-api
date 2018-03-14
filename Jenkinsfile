#!groovy
properties(
    [[$class: 'GithubProjectProperty', projectUrlStr: 'https://github.com/hmcts/div-evidence-management-client-api/'],
     pipelineTriggers([[$class: 'GitHubPushTrigger']])]
)

@Library(['Reform', 'Divorce'])
import uk.gov.hmcts.Packager
import uk.gov.hmcts.Versioner

def packager = new Packager(this, 'divorce')
def versioner = new Versioner(this)
def notificationChannel = '#div-dev'

buildNode {
  try {
    def version
    String evidenceManagementClientApiRPMVersion

    stage('Checkout') {
      deleteDir()
      checkout scm
      env.CURRENT_SHA = gitSha()
    }

    onDevelop {
      stage('Develop Branch SNAPSHOT') {
        sh '''
          sed  -i '1,/parent/ s/<\\/version>/-SNAPSHOT<\\/version>/' pom.xml
        '''
      }
    }

    stage('Build') {
      sh "mvn clean compile"
    }

    stage('Test (Unit)') {
      sh "mvn test"
    }

    onMaster {
      stage('Code Coverage (Sonar)') {
        sh "mvn sonar:sonar -Dsonar.host.url=$SONARQUBE_URL"
      }
    }

    onDevelop {
      stage('Code Coverage (Sonar)') {
        sh "mvn sonar:sonar -Dsonar.host.url=$SONARQUBE_URL"
      }
    }

    stage("Dependency check") {
      try {
        sh "mvn dependency-check:check"
      }
      finally {
        publishHTML(target: [
            alwaysLinkToLastBuild: true,
            keepAll              : true,
            reportDir            : "target/",
            reportFiles          : 'dependency-check-report.html',
            reportName           : 'Dependency Check Security Test Report'
        ])
      }
    }

    stage('Package (JAR)') {
      versioner.addJavaVersionInfo()
      sh "mvn clean package -DskipTests=true"
    }

    stage('Jacoco Code Coverage') {
      sh "mvn verify"
    }

    onDevelop {
      stage('Package (RPM)') {
        evidenceManagementClientApiRPMVersion = packager.javaRPM(
            'evidence-management-client-api',
            '$(ls target/evidence-management-client-api-*.jar)',
            'springboot',
            'src/main/resources/application.properties'
        )

        version = "{evidence_management_client_api_buildnumber: ${evidenceManagementClientApiRPMVersion} }"

        sh "echo \${version}"

        packager.publishJavaRPM('evidence-management-client-api')

      }
    }

    onMaster {
      stage('Package (Docker)') {
        dockerImage imageName: 'divorce/evidence-management-client-api'
      }
    }

    onMaster {
      stage('Package (RPM)') {
        evidenceManagementClientApiRPMVersion = packager.javaRPM(
            'evidence-management-client-api',
            '$(ls target/evidence-management-client-api-*.jar)',
            'springboot',
            'src/main/resources/application.properties'
        )

        version = "{evidence_management_client_api_buildnumber: ${evidenceManagementClientApiRPMVersion} }"

        sh "echo \${version}"

        packager.publishJavaRPM('evidence-management-client-api')

        deploy app: 'evidence-management-client-api', version: evidenceManagementClientApiRPMVersion, sha: env.CURRENT_SHA
      }
    }

  } catch (err) {
    onMaster {
      slackSend(
          channel: notificationChannel,
          color: 'danger',
          message: "${env.JOB_NAME}:  <${env.BUILD_URL}console|Build ${env.BUILD_DISPLAY_NAME}> has FAILED")
    }
    throw err
  }
}