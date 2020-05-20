#!groovy

node {

  // See https://www.jenkins.io/doc/pipeline/steps/pipeline-utility-steps/
  // https://www.jenkins.io/doc/pipeline/steps/
  // https://github.com/jenkinsci/nexus-artifact-uploader-plugin

  stage ('checkout') {
    checkout scm
  }

  stage ('check') {
    dir ( 'graphqlmt' ) {
      echo 'Hello, JDK'
      sh 'java -version'
      sh './gradlew --version'

      // We would like to access the credentials declared
      withCredentials([usernamePassword(credentialsId: 'kinexus', usernameVariable: 'NUSER', passwordVariable: 'NPASS')]) {
        sh 'echo NEXUS_USERNAME=${NUSER} NEXUS_PASSWORD=${NPASS}'
      }
    }
  }

  stage ('build') {
    dir ( 'graphqlmt' ) {
      sh './gradlew --no-daemon --console=plain build'
      sh 'ls ./build/libs'
    }
  }

  stage ('publish') {
    dir ( 'graphqlmt' ) {
      def props = readProperties file: 'gradle.properties'
      def target_repository = null;
      println("Props: ${props}");
      def release_files = findFiles(glob: '**/graphqlmt-*.*.*.jar')
      println("Release Files: ${release_files}");
      if ( release_files.size() == 1 ) {
        // println("Release file : ${release_files[0].name}");
        if ( release_files[0].name.contains('SNAPSHOT') ) {
          target_repository='semweb-snapshot';
        }
        else {
          target_repository='semweb-release';
        }
      }

      if ( target_repository != null ) {
        println("Publish ${release_files[0].path} with version ${props.appVersion} to ${target_repository}");
        nexusArtifactUploader(
          nexusVersion: 'nexus3',
          protocol: 'http',
          nexusUrl: 'nexus.semweb.co',
          groupId: 'com.semweb',
          version: props.appVersion,
          repository: target_repository,
          credentialsId: 'semweb-nexus',
          artifacts: [
              [artifactId: 'graphqlmt',
               classifier: '',
               file: release_files[0].path,
               type: 'jar']
          ]
        )
      }
    }
  }
}

