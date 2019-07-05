package com.weigan

import org.gradle.api.Plugin
import org.gradle.api.Project

class GreetImpl implements Plugin<Project>{

    @Override
    void apply(Project project) {
        project.task('hello') {
            doLast {
                println 'Hello from the GreetingPlugin'
            }
        }
        project.android.registerTransform(new PreDexTransform(project))
    }
}