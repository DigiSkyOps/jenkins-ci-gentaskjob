// vim: set ft=groovy:
def repos = ["dlfiles", "opspkg", "maven", 
    "customtask", "javacompile", "semver", "ftpartifact", "eclipsebuild", "genlogparser", 
    "cloudartifact", "hellosh", "docker_build", "docker_push"
]

def jobdir = "JenkinsABS"

def argProps = ('''
    |artifact.version=$PIPELINE_VERSION
    |ftp.host=127.0.0.1
    |ftp.port=21
    |ftp.user=abs
    |ftp.pwd=abs123''').stripMargin().trim().toString()

for (repo in repos) {
    job("${jobdir}/${repo}") {
        logRotator { numToKeep(30) }
        label('abs')
        scm {
            git {
                branch('master')
                extensions {
                    cleanBeforeCheckout()
                }
                remote {
                    url("git@github.com:DigiSkyOps/jenkins-task-${repo}.git")
                    credentials('cibuild-sshkey')
                }
            }
        }
        triggers {
            pollSCM {
              scmpoll_spec('H/3 * * * *') 
              ignorePostCommitHooks(true)
            }
            gitlabPush { includeBranches('master') }
        }
        wrappers {
            deliveryPipelineVersion('${VERSION,path="VERSION"}+${GIT_REVISION,length=6}.${BUILD_NUMBER}', true)
            timestamps()
        }

        steps {

            absConfigBuilder {
                configSource { configFromWorkspace { ciYmlPath('.ci.yml') } }
                runPipeline('dev')
                argProperties(argProps)
            }      

        }
        publishers {
            absReportPublisher()
        }
    }
}

