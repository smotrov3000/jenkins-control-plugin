/*
 * Copyright (c) 2013 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.jenkins.model;

import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

public class FolderJob extends Job {

    private static final Icon FOLDER_ICON = GuiUtil.loadIcon("folder.svg");
    private static final Icon NULL_ICON = GuiUtil.loadIcon("null.png");

    private String clazz;
    private String name;

    private String displayName;
    private String url;

    private String color;
    private boolean inQueue;
    private boolean buildable;
    private boolean fetchBuild = false;

    private Health health;

    private Build lastBuild;

    private List<ShortJobDescription> shortJobDescriptions  = new LinkedList<>();
    private List<Job> jobs = new LinkedList<>();
    private List<Build> lastBuilds = new LinkedList<>();

    private final List<JobParameter> parameters = new LinkedList<>();


    public FolderJob() {
    }

    private FolderJob(String name, String displayName, String color, String url, Boolean inQueue, Boolean buildable) {
        this.name = name;
        this.displayName = displayName;
        this.color = color;
        this.url = url;
        this.inQueue = inQueue;
        this.buildable = buildable;
    }


    public static FolderJob createJob(String jobName, String displayName, String jobColor, String jobUrl, String inQueue, String buildable) {
        return new FolderJob(jobName, displayName, jobColor, jobUrl, Boolean.valueOf(inQueue), Boolean.valueOf(buildable));
    }


    public Icon getStateIcon() {
        return FOLDER_ICON;
    }

    public Icon getHealthIcon() {
        return NULL_ICON;
    }

    public String findHealthDescription() {
        if (health == null) {
            return "";
        }
        return health.getDescription();
    }


    public void updateContentWith(FolderJob updatedJob) {
        this.color = updatedJob.getColor();
        this.health = updatedJob.getHealth();
        this.inQueue = updatedJob.isInQueue();
        this.lastBuild = updatedJob.getLastBuild();
        this.lastBuilds = updatedJob.getLastBuilds();
    }


    public void addParameter(String paramName, String paramType, String defaultValue, String... choices) {
        parameters.add(JobParameter.create(paramName, paramType, defaultValue, choices));
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        if (StringUtils.isEmpty(displayName)) {
            return name;
        }
        return displayName;
    }

    public String getNavigableName() {
        return name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isInQueue() {
        return inQueue;
    }

    public void setInQueue(boolean inQueue) {
        this.inQueue = inQueue;
    }

    public boolean isBuildable() {
        return buildable;
    }

    public void setBuildable(boolean buildable) {
        this.buildable = buildable;
    }

    public Build getLastBuild() {
        return lastBuild;
    }

    public void setLastBuild(Build lastBuild) {
        this.lastBuild = lastBuild;
    }

    public List<Build> getLastBuilds() {
        return lastBuilds;
    }

    public void setLastBuilds(List<Build> builds) {
        lastBuilds = builds;
    }

    Health getHealth() {
        return health;
    }

    public void setHealth(Health health) {
        this.health = health;
    }

    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    public void setFetchBuild(boolean fetchBuild) {
        this.fetchBuild = fetchBuild;
    }

    public boolean isFetchBuild() {
        return fetchBuild;
    }

    public List<ShortJobDescription> getShortJobDescriptions() {
        return shortJobDescriptions;
    }

    public void setShortJobDescriptions(List<ShortJobDescription> shortJobDescriptions) {
        this.shortJobDescriptions = shortJobDescriptions;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public List<JobParameter> getParameters() {
        return parameters;
    }

    public boolean hasParameter(String name) {
        if (hasParameters()) {
            for(JobParameter parameter: parameters) {
                if (parameter.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setParameter(JobParameter jobParameter) {
        if (parameters.size() > 0) {
            for(JobParameter parameter: parameters) {
                if (parameter.getName().equals(jobParameter.getName())) {
                    parameters.set(parameters.indexOf(parameter), jobParameter);
                }
            }
        }
    }

    public void addParameters(List<JobParameter> jobParameters) {
        parameters.addAll(jobParameters);
    }

    // TODO assign Folder its own class
    public boolean isFolder() {
        return "com.cloudbees.hudson.plugins.folder.Folder".equals(clazz);
    }

    @Override
    public String toString() {
        return "Job{" +
                "name='" + name + '\'' +
                '}';
    }
}
