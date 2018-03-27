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

    stage('Build') {

        onDevelop {
            sh "./gradlew clean addReleaseSuffixToDevelop build -x test"
        }

        onPR {
            sh "./gradlew clean addReleaseSuffixToDevelop build -x test"
        }

        onMaster {
            sh "./gradlew clean build -x test"
        }
    }

    stage('Test (Unit)') {
        try {
            sh "./gradlew test"
        } finally {
            junit 'build/test-results/test/**/*.xml'
        }
    }

    stage('Mutation Testing (Pitest)') {
        onPR {
              sh "./gradlew pitest"
        }
    }

    stage('Code Coverage (Sonar)') {
        onPR {
            sh "./gradlew -Dsonar.analysis.mode=preview -Dsonar.host.url=$SONARQUBE_URL sonarqube"
        }

        onMaster {
            sh "./gradlew -Dsonar.host.url=$SONARQUBE_URL sonarqube"
                }

        onDevelop {
            sh "./gradlew -Dsonar.host.url=$SONARQUBE_URL sonarqube"
        }
    }

    stage('Dependency check') {
        try {
            sh "./gradlew -DdependencyCheck.failBuild=true dependencyCheckAnalyze"
        } catch (ignored) {
            archiveArtifacts 'build/reports/dependency-check-report.html'
            notifyBuildResult channel: channel, color: 'warning',
                    message: 'OWASP dependency check failed see the report for the errors'
        }
    }

    stage('Package (JAR)') {
        versioner.addJavaVersionInfo()
        sh "./gradlew installDist bootRepackage"
    }

    stage('Jacoco Code Coverage') {
      sh "./gradlew jacocoTestCoverageVerification"
    }

    onDevelop {
      stage('Package (RPM)') {
        evidenceManagementClientApiRPMVersion = packager.javaRPM(
            'evidence-management-client-api',
            '$(ls build/libs/div-evidence-management-client-api-$(./gradlew -q printVersion).jar)',
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
            '$(ls build/libs/div-evidence-management-client-api-$(./gradlew -q printVersion).jar)',
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