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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;

import org.codinjutsu.tools.jenkins.util.DateUtil;
import org.codinjutsu.tools.jenkins.util.GuiUtil;

public class Build {

    public static final Map<BuildStatusEnum, Icon> ICON_BY_BUILD_STATUS_MAP = new HashMap<>();

    private String url;
    private Date buildDate;
    private int number;
    private boolean building;
    private String message;
    private Date timestamp;
    private Long duration;

    private BuildStatusEnum status;

    static {
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.SUCCESS, GuiUtil.loadIcon("blue.svg"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.STABLE, GuiUtil.loadIcon("blue.svg"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.FAILURE, GuiUtil.loadIcon("red.svg"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.UNSTABLE, GuiUtil.loadIcon("yellow.svg"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.NULL, GuiUtil.loadIcon("grey.svg"));
        ICON_BY_BUILD_STATUS_MAP.put(BuildStatusEnum.ABORTED, GuiUtil.loadIcon("grey.svg"));
    }


    public static Build createBuildFromWorkspace(String buildUrl, Long number, String status, Boolean isBuilding, String buildDate, Long timestamp, Long duration) {
        return createBuild(buildUrl, number, status, isBuilding, buildDate, DateUtil.WORKSPACE_DATE_FORMAT, null, timestamp, duration);
    }

    public static Build createBuildFromWorkspace(String buildUrl, String number, String status, String isBuilding, String buildDate, Long timestamp, Long duration) {
        return createBuild(buildUrl, Long.parseLong(number), status, Boolean.parseBoolean(isBuilding), buildDate, DateUtil.WORKSPACE_DATE_FORMAT, null, timestamp, duration);
    }

    public static Build createBuildFromRss(String buildUrl, String number, String status, String isBuilding, String buildDate, String message) {
        return createBuild(buildUrl, Long.parseLong(number), status, Boolean.parseBoolean(isBuilding), buildDate, DateUtil.RSS_DATE_FORMAT, message, 0l, 0l);
    }

    private static Build createBuild(String buildUrl, Long number, String status, Boolean isBuilding, String buildDate, SimpleDateFormat simpleDateFormat, String message, Long timestamp, Long duration) {
        BuildStatusEnum buildStatusEnum = BuildStatusEnum.parseStatus(status);
        Date date = DateUtil.parseDate(buildDate, simpleDateFormat);

        return new Build(buildUrl, number.intValue(), date, buildStatusEnum, isBuilding, message, timestamp, duration);
    }

    public Build() {
    }

    private Build(String url, int number, Date buildDate, BuildStatusEnum status, boolean isBuilding, String message, Long timestamp, Long duration) {
        this.url = url;
        this.number = number;
        this.buildDate = buildDate;
        this.status = status;
        this.building = isBuilding;
        this.message = message;
        setTimestamp(timestamp);
        this.duration = duration;
    }


    public static Icon getStateIcon(Job job) {
        BuildStatusEnum[] jobStates = BuildStatusEnum.values();
        for (BuildStatusEnum jobState : jobStates) {
            String stateName = jobState.getColor();
            if (job.getColor().startsWith(stateName)) {
                return ICON_BY_BUILD_STATUS_MAP.get(jobState);
            }
        }

        return ICON_BY_BUILD_STATUS_MAP.get(BuildStatusEnum.NULL);
    }

    public Icon getStateIcon() {
        return ICON_BY_BUILD_STATUS_MAP.get(status);
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public BuildStatusEnum getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = BuildStatusEnum.parseStatus(status);
    }

    public Date getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(String buildDate) {
        this.buildDate = DateUtil.parseDate(buildDate, DateUtil.WORKSPACE_DATE_FORMAT);
    }

    public void setBuildDate(Long buildDate) {
        if (buildDate != null) {
            this.buildDate = new Date(buildDate);
        }
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = new Date(timestamp);
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public boolean isBuilding() {
        return building;
    }

    public void setBuilding(boolean building) {
        this.building = building;
    }

    public boolean isAfter(Build aBuild) {
        return this.getNumber() > aBuild.getNumber();
    }

    public String getMessage() {
        return message;
    }
}
