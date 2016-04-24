node {
   // Mark the code checkout 'stage'....
   stage 'Checkout'

   // Get some code from a GitHub repository
   git url: 'https://github.com/romixch/SlackWatchdog.git', branch: 'JenkinsBuildFile'

   // Mark the code build 'stage'....
   stage 'Build'
   // Run the gradle build
   sh './gradlew jar'

   stage 'Unit tests'
   sh './gradlew test'
   step $class: 'JUnitResultArchiver', testResults: 'build/test-results/TEST-*.xml'

   stage 'Create Fat Jar'
   sh './gradlew shadowJar'

   stage 'Integration tests'
   sh 'docker ps -a'
}