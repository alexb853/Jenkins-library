// vars/buildAndDeploy.groovy
def call() {
  node {
    stage('Pull Nginx Image') {
        steps {
            script {
                pipelineUtils.pullNginxImage()
            }
        }
    }

    stage('Build Polybot Image') {
        steps {
            script {
                pipelineUtils.buildPolybotImage(env.POLYBOT_IMG_NAME)
            }
        }
    }

    stage('Build Docker Image') {
        steps {
            script {
                pipelineUtils.buildDockerImage(env.DOCKER_COMPOSE_FILE)
            }
        }
    }

    stage('Snyk Container Test') {
        steps {
            script {
                pipelineUtils.snykContainerTest(env.APP_IMAGE_NAME, env.SNYK_TOKEN)
            }
        }
    }

    stage('Tag and Push Images') {
        steps {
            script {
                withCredentials([
                    usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASS')
                ]) {
                    pipelineUtils.tagAndPushImages(env.APP_IMAGE_NAME, env.WEB_IMAGE_NAME, env.IMAGE_TAG, env.DOCKER_USERNAME, env.DOCKER_PASS)
                }
            }
        }
    }

    stage('Trigger Deploy') {
        steps {
            script {
                pipelineUtils.triggerDeploy("alexb853/${env.POLYBOT_IMG_NAME}")
            }
        }
    }

    stage('Sleep') {
        steps {
            sleep 20
        }
    }

    post {
        always {
            cleanWs()
        }
    }
  }
}
