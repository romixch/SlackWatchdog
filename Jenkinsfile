node {
   // Mark the code checkout 'stage'....
   stage 'Checkout'

   // Get some code from a GitHub repository
   git url: 'https://github.com/romixch/SlackWatchdog.git', branch: 'JenkinsBuildFile'

   // Mark the code build 'stage'....
   stage 'Build'
   // Run the gradle build
   sh './gradlew jar'

   stage 'Create Fat Jar'
   sh './gradlew shadowJar'

   stage 'Testing'
   sh './gradlew test'
   step $class: 'JUnitResultArchiver', testResults: 'build/test-results/TEST-*.xml'
}