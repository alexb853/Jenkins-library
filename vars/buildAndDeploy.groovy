// vars/buildAndDeploy.groovy
def call() {
   stage('Trigger Deploy') {
            steps {
               build job: 'BotDeploy', wait: false, parameters: [
                string(name: 'IMAGE_URL', value: "alexb853/$POLYBOT_IMG_NAME")
                ]
            }
   }
