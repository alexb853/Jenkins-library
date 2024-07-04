// vars/buildAndDeploy.groovy
def call() {
    pipeline {
        agent any

        options {
            disableConcurrentBuilds()
            buildDiscarder(logRotator(daysToKeepStr: '30'))
            timestamps()
        }

        environment {
            POLYBOT_IMG_NAME = "dockerbot:${BUILD_NUMBER}"
            NGINX_IMG = "nginx:alpine"
            DOCKERHUB_CREDENTIALS = credentials('dockerhub') // Jenkins credentials ID
            APP_IMAGE_NAME = 'python-app-image'
            WEB_IMAGE_NAME = 'web-image'
            DOCKER_COMPOSE_FILE = 'docker-compose.yml'
            BUILD_DATE = new Date().format('yyyyMMdd-HHmmss')
            IMAGE_TAG = "v1.0.0-${BUILD_NUMBER}-${BUILD_DATE}"
            SNYK_TOKEN = credentials('snykAPI')
        }

        stages {

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
        }

        post {
            always {
                cleanWs()
            }
        }
    }
}
