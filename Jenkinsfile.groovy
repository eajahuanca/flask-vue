def qa_server_ip="192.168.0.11";
def prod_server_ip="192.168.0.10";
pipeline {
    agent{label 'ubuntu'} // debian node is QA environment
    stages {
        stage('Clone QA env') {
            steps {
                git '<put here your repository url>'
            }            
        }
        stage('Change frontend ip'){
            steps{
                dir('client'){
                    sh "cat src/components/Books.vue | grep 'http://'"
                    sh "sed -i 's/localhost/${qa_server_ip}/g' src/components/Books.vue"
                    sh "cat src/components/Books.vue | grep 'http://'"
                }
            }

        }
        stage('Docker images build'){
            steps{
                dir('client'){
                    sh "docker build -t vueapp:latest ."

                    //Changing IP for PROD env. 
                    sh "sed -i 's/localhost/${prod_server_ip}/g' src/components/Books.vue"
                    sh "cat src/components/Books.vue | grep 'http://'"
                    sh "docker build -t vueapp:latest-prod ."

                }
                dir('server'){
                    sh 'docker build -t flaskapp:latest .'
                }
                sh "docker images"
            }
        }
        stage('Deploy QA environment'){
            steps{
                git '<put here your repository url>'
                sh "docker compose down"
                sh "docker compose up -d"
                sh "docker compose ps"
            }
        }
        stage('Save docker images'){
            steps{
                sh "docker save flaskapp:latest | gzip > flaskapplatest.tar.gz"
                sh "docker save vueapp:latest-prod | gzip > vueapplatest-prod.tar.gz"
                sh "ls -alth"
                stash includes: 'vueapplatest-prod.tar.gz', name: 'vueapp_image-prod'
                stash includes: 'flaskapplatest.tar.gz', name: 'flaskapp_image'
            }

        }
        stage ('Deploy PROD environment'){
            agent{label 'prod'}
            steps{
                git '<put here your repository url>'
                unstash 'flaskapp_image'
                unstash 'vueapp_image-prod'
                sh "docker compose -f docker-compose-prod.yml down"
                sh "echo 'Y' | docker image prune -a "
                sh "docker load -i vueapplatest-prod.tar.gz"
                sh "docker load -i flaskapplatest.tar.gz"
                sh "docker compose -f docker-compose-prod.yml up -d "
            }
        }
    }
}
