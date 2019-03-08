// Environment name will be taken from the ROOT folder
def env(){
  return env.JOB_NAME.replace("/${env.JOB_BASE_NAME}", '')
}

def validateTemplate() {
  def templates = findFiles(glob: 'cfn/**')
  for (template in templates) {
    cfnValidate(file: template.getPath())
  }
}

def updateDeploymentBucket() {
  def envName = env()
  cfnUpdate(stack: envName+'-deployment-bucket', file: 'cfn/deployment/s3bucket.cfn.yaml', params: ['BucketName': envName], timeoutInMinutes: 10, tags: ['Environment='+envName], pollInterval: 10000)
}

def getDeploymentBucketName() {
  def envName = env()
  return cfnDescribe(stack: envName+'-deployment-bucket').BucketName
}

def getDeploymentPath() {
  return "artifacts/${env()}/${env.JOB_BASE_NAME}/${env.BUILD_NUMBER}/cfn"
}

def uploadTemplates() {
  s3Upload(file: 'cfn', bucket: getDeploymentBucketName(), path: getDeploymentPath())
}

def uploadLambdaCode() {
  s3Upload(file: 'dist/lambda.zip', bucket: getDeploymentBucketName(), path: getDeploymentPath())
}

return this