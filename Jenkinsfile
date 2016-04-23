node {
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