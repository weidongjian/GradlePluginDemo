import org.gradle.api.Project

class MyExtensions{

    String message
    Boolean isDebug

    public MyExtensions(Project project) {

    }

    String getMessage() {
        return message
    }

    void setMessage(String message) {
        println "set message = "+message
        this.message = message
    }
}