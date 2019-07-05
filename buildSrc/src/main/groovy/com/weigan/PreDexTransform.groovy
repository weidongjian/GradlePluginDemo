package com.weigan

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Project
import org.gradle.internal.FileUtils
import org.gradle.internal.impldep.org.apache.commons.codec.digest.DigestUtils

public class PreDexTransform extends Transform {

    def NAME = "Greeting"

    public PreDexTransform(Project project) {

    }

    @Override
    String getName() {
        return NAME
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
//        return QualifiedContent.DefaultContentType.CLASSES
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.PROJECT_ONLY
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
//        super.transform(transformInvocation)

        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()

        transformInvocation.inputs.each { TransformInput input ->

            println('TransformInput version 3 ')

            input.directoryInputs.each { DirectoryInput directoryInput ->
//                println(directoryInput.name)
                File des = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                println('I am in input each des file ' + des.path + " input " + directoryInput.file)

                com.android.utils.FileUtils.copyDirectory(directoryInput.file, des)
            }

            println('jarinputs size ' + input.jarInputs.size())

            input.jarInputs.each { JarInput jarInput ->
                def name = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (name.endsWith('.jar')) {
                    name = jarName.substring(0, jarName.length() - 4)
                }
                File dest = outputProvider.getContentLocation(name + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                println('name ' + name + " file " + jarInput.file)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }
}
