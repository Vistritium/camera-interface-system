pipeline {
    agent: any

    environment {
        DOCKER_LOGIN_USERNAME = 'nowicki.azurecr.io'
        DOCKER_LOGIN_PASSWORD = credentials('DOCKER_REGISTRY_PASSWORD')
    }

    stage {
        stage('Build, publish docker image'){
            steps {
                sh 'docker login -u ${DOCKER_LOGIN_USERNAME} -p ${DOCKER_LOGIN_PASSWORD}'

            }
        }
    }
}