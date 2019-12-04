package com.snug.propertities;

import lombok.Data;

public class SnugPropertities {
    String projectName;
    String projectPath;
    String projectVersion;

    public SnugPropertities(){

    }
    public SnugPropertities(String projectPath){
        this.setProjectPath(projectPath);
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
        this.projectName = projectPath.substring(projectPath.lastIndexOf("/")+1,projectPath.length());
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }
}
