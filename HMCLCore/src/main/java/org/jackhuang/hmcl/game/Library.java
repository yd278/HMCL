/*
 * Hello Minecraft! Launcher.
 * Copyright (C) 2017  huangyuhui <huanghongxun2008@126.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see {http://www.gnu.org/licenses/}.
 */
package org.jackhuang.hmcl.game;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.jackhuang.hmcl.util.Constants;
import org.jackhuang.hmcl.util.OperatingSystem;
import org.jackhuang.hmcl.util.Platform;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A class that describes a Minecraft dependency.
 *
 * @author huangyuhui
 */
public class Library {

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String classifier;
    private final String url;
    private final LibrariesDownloadInfo downloads;
    private final LibraryDownloadInfo download;
    private final ExtractRules extract;
    private final boolean lateload;
    private final Map<OperatingSystem, String> natives;
    private final List<CompatibilityRule> rules;

    private final String path;

    public Library(String groupId, String artifactId, String version) {
        this(groupId, artifactId, version, null, null, null);
    }
    
    public Library(String groupId, String artifactId, String version, String classifier, String url, LibrariesDownloadInfo downloads) {
        this(groupId, artifactId, version, classifier, url, downloads, false);
    }

    public Library(String groupId, String artifactId, String version, String classifier, String url, LibrariesDownloadInfo downloads, boolean lateload) {
        this(groupId, artifactId, version, classifier, url, downloads, lateload, null, null, null);
    }

    public Library(String groupId, String artifactId, String version, String classifier, String url, LibrariesDownloadInfo downloads, boolean lateload, ExtractRules extract, Map<OperatingSystem, String> natives, List<CompatibilityRule> rules) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        if (classifier == null)
            if (natives != null && natives.containsKey(OperatingSystem.CURRENT_OS))
                this.classifier = natives.get(OperatingSystem.CURRENT_OS).replace("${arch}", Platform.PLATFORM.getBit());
            else
                this.classifier = null;
        else
            this.classifier = classifier;
        this.url = url;
        this.downloads = downloads;
        this.extract = extract;
        this.lateload = lateload;
        this.natives = natives;
        this.rules = rules;

        LibraryDownloadInfo temp = null;
        if (downloads != null)
            if (isNative())
                temp = downloads.getClassifiers().get(this.classifier);
            else
                temp = downloads.getArtifact();

        if (temp != null && temp.getPath() != null)
            path = temp.getPath();
        else
            path = String.format("%s/%s/%s/%s-%s", groupId.replace(".", "/"), artifactId, version, artifactId, version)
                    + (this.classifier == null ? "" : "-" + this.classifier) + ".jar";

        download = new LibraryDownloadInfo(path,
                Optional.ofNullable(temp).map(LibraryDownloadInfo::getUrl).orElse(Optional.ofNullable(url).orElse(Constants.DEFAULT_LIBRARY_URL) + path),
                temp != null ? temp.getSha1() : null,
                temp != null ? temp.getSize() : 0
        );
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public ExtractRules getExtract() {
        return extract == null ? ExtractRules.EMPTY : extract;
    }

    public boolean appliesToCurrentEnvironment() {
        return CompatibilityRule.appliesToCurrentEnvironment(rules);
    }

    public boolean isNative() {
        return natives != null && appliesToCurrentEnvironment();
    }

    public String getPath() {
        return path;
    }

    public LibraryDownloadInfo getDownload() {
        return download;
    }

    public boolean isLateload() {
        return lateload;
    }

    @Override
    public String toString() {
        return "Library[" + groupId + ":" + artifactId + ":" + version + "]";
    }

    public static Library fromName(String name) {
        return fromName(name, null, null, null, null, null);
    }

    public static Library fromName(String name, String url, LibrariesDownloadInfo downloads, ExtractRules extract, Map<OperatingSystem, String> natives, List<CompatibilityRule> rules) {
        String[] arr = name.split(":", 4);
        if (arr.length != 3 && arr.length != 4)
            throw new IllegalArgumentException("Library name is malformed. Correct example: group:artifact:version(:classifier).");

        return new Library(arr[0].replace("\\", "/"), arr[1], arr[2], arr.length >= 4 ? arr[3] : null, url, downloads, false, extract, natives, rules);
    }

    public static class Serializer implements JsonDeserializer<Library>, JsonSerializer<Library> {

        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public Library deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            if (json == null || json == JsonNull.INSTANCE)
                return null;
            JsonObject jsonObject = json.getAsJsonObject();
            if (!jsonObject.has("name"))
                throw new JsonParseException("Library name not found.");
            return fromName(
                    jsonObject.get("name").getAsString(),
                    jsonObject.has("url") ? jsonObject.get("url").getAsString() : null,
                    context.deserialize(jsonObject.get("downloads"), LibrariesDownloadInfo.class),
                    context.deserialize(jsonObject.get("extract"), ExtractRules.class),
                    context.deserialize(jsonObject.get("natives"), new TypeToken<Map<OperatingSystem, String>>() {
                    }.getType()),
                    context.deserialize(jsonObject.get("rules"), new TypeToken<List<CompatibilityRule>>() {
                    }.getType()));
        }

        @Override
        public JsonElement serialize(Library src, Type type, JsonSerializationContext context) {
            if (src == null)
                return JsonNull.INSTANCE;
            JsonObject obj = new JsonObject();
            obj.addProperty("name", src.groupId + ":" + src.artifactId + ":" + src.version);
            obj.addProperty("url", src.url);
            obj.add("downloads", context.serialize(src.downloads));
            obj.add("extract", context.serialize(src.extract));
            obj.add("natives", context.serialize(src.natives, new TypeToken<Map<OperatingSystem, String>>() {
            }.getType()));
            obj.add("rules", context.serialize(src.rules, new TypeToken<List<CompatibilityRule>>() {
            }.getType()));
            return obj;
        }

    }
}
