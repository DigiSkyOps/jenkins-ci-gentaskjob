// vim: set ft=groovy:

def jobdir = "JenkinsABS"

def argProps = ('''
    |artifact.version=$PIPELINE_VERSION
    |ftp.host=127.0.0.1
    |ftp.port=21
    |ftp.user=abs
    |ftp.pwd=abs123''').stripMargin().trim().toString()

deliveryPipelineView("${jobdir}/01-box") {
  pipelineInstances(1)
  allowPipelineStart()
  allowRebuild()
  enableManualTriggers()
  showTotalBuildTime()
  showChangeLog()
  enablePaging()
  useTheme('contrast')
  pipelines {
    component("abs-box", "box-base")
  }
} // end deliverypipelineview

job("${jobdir}/box-base") {
    logRotator { numToKeep(30) }
    label('dockerimage')
    scm { git { branch('master')
            extensions {
                cleanBeforeCheckout()
            }
            remote {
                url("git@github.com/DigiSkyOps/box-baseimage.git")
                credentials('cibuild-sshkey')
            }
        }}
    triggers { gitlabPush { includeBranches('master') } }
    wrappers {
        deliveryPipelineVersion('${BUILD_NUMBER}.${GIT_REVISION,length=6}', true)
        timestamps()
    }

    steps {
        absConfigBuilder {
            configSource { configFromWorkspace { ciYmlPath('.ci.yml') }}
            runPipeline('dev')
            argProperties(argProps)
        }      
    }
    publishers {
        absReportPublisher()
    }
}
