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
import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor
import javassist.CtMethod
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
                injectDir(directoryInput.file.absolutePath)
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


    private void injectDir(String path) {
        ClassPool classPool = ClassPool.getDefault();

        File dir = new File(path)
//        classPool.appendClassPath(path)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->

                String filePath = file.path
                // 这里我们指定修改TestInjectModel.class字节码，在构造函数中增加一行i will inject
                if (filePath.endsWith('.class')
                        && filePath.endsWith('TestInjectModel.class')) {

                    int index = filePath.lastIndexOf('/')
                    index++
                    int end = filePath.length() - 6 // '.class'.length = 6
                    println("injectDir filepath $filePath lastindex $index")
//                    String className = filePath.substring(index, end)
//                            .replace('\\', '.')
//                            .replace('/', '.')
                    String className = filePath.substring(0, end)
                    println("injectDir className $className")
                    try {
                        // 开始修改class文件
                        className = "com/example/gradleplugindemo/TestInjectModel"
                        CtClass ctClass = classPool.get(className)

                        // 拿到CtClass后可以对 class 做修改操作（addField addMethod ..）
                        if (ctClass.isFrozen()) {
                            ctClass.defrost()
                        }


                        ctClass.addField(CtField.make("private int age;", ctClass))
                        ctClass.addMethod(CtMethod.make("public void setAge(int age){this.age = age;}", ctClass))
                        ctClass.addMethod(CtMethod.make("public int getAge(){return this.age;}", ctClass))

//                        CtConstructor[] constructors = ctClass.getDeclaredConstructors()
//                        if (null == constructors || constructors.length == 0) {
//                            // 手动创建一个构造函数
//                            CtConstructor constructor = new CtConstructor(new CtClass[0], ctClass)
//                            constructor.insertBeforeBody(injectStr)
//                        } else {
//                            constructors[0].insertBeforeBody(injectStr)
//                        }
                        ctClass.writeFile(path)
                        ctClass.detach()
                    } catch (Exception e) {
                        println("injectDir Exception " + e.toString())
                    }

                }
            }
        }
    }
}
