pipeline {
    agent any

    environment {
        DOCKER_LOGIN_USERNAME = 'nowicki'
        DOCKER_LOGIN_PASSWORD = credentials('DOCKER_REGISTRY_PASSWORD')
        DOCKER_REPO = 'nowicki.azurecr.io'
    }

    stages {
        stage('Build, publish docker image'){
            steps {
                sh 'docker login -u ${DOCKER_LOGIN_USERNAME} -p ${DOCKER_LOGIN_PASSWORD} ${DOCKER_REPO}'
                sh 'for i in $(seq 1 5); do sbt docker:publish && s=0 && break || s=$? && sleep 1; done; (exit $s)'
            }
        }
        stage('Restart service'){
            steps {
                sh 'docker pull nowicki.azurecr.io/nowicki/camera-interface-system:latest'
                sh 'docker service update --with-registry-auth --image nowicki.azurecr.io/nowicki/camera-interface-system:latest camera-interface-system'
            }
        }
    }

}