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

    stage('Sonar') {
        onPR {
            sh "./gradlew -Dsonar.analysis.mode=preview -Dsonar.host.url=$SONARQUBE_URL sonarqube"
        }

        onDevelop {
            sh "./gradlew -Dsonar.host.url=$SONARQUBE_URL sonarqube"
        }
    }

    stage('OWASP dependency check') {
        try {
            sh "./gradlew -DdependencyCheck.failBuild=true dependencyCheck"
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