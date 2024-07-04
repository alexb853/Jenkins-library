// vars/pipelineUtils.groovy
def pullNginxImage() {
    docker.image("nginx:alpine").pull()
}

def buildPolybotImage(imageName) {
    sh """
       docker build -t ${imageName} .
       docker tag ${imageName} alexb853/${imageName}
    """
}

def buildDockerImage(composeFile) {
    sh "docker-compose -f ${composeFile} build"
}

def snykContainerTest(appImageName, snykToken) {
    sh """
       snyk auth ${snykToken}
       snyk container test ${appImageName}:latest --policy-path=.snyk
    """
}

def tagAndPushImages(appImageName, webImageName, imageTag, dockerUsername, dockerPass) {
    sh """
       docker login -u ${dockerUsername} -p ${dockerPass}
       docker tag ${appImageName}:latest ${dockerUsername}/${appImageName}:${imageTag}
       docker push ${dockerUsername}/${appImageName}:${imageTag}
       docker tag ${webImageName}:latest ${dockerUsername}/${webImageName}:${imageTag}
       docker push ${dockerUsername}/${webImageName}:${imageTag}
    """
}

def triggerDeploy(imageUrl) {
    build job: 'BotDeploy', wait: false, parameters: [
        string(name: 'IMAGE_URL', value: imageUrl)
    ]
}
