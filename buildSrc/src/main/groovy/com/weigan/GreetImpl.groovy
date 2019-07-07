package com.weigan

import org.gradle.api.Plugin
import org.gradle.api.Project

class GreetImpl implements Plugin<Project>{

    @Override
    void apply(Project project) {

        //创建一个扩展属性 myExtension，使用 MyExtension 进行管理外部属性配置
        project.extensions.create('testExtension', MyExtensions, project)

        project.task('hello') {
            doLast {
                println project.testExtension.message
            }
        }
        project.android.registerTransform(new PreDexTransform(project))
    }
}