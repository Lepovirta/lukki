package io.lepo.lukki.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project

class GitVersionPlugin implements Plugin<Project> {
    def pattern = /v(?<major>\d+?)\.(?<minor>\d+?)\.(?<patch>\d+?)(?:\-(?<tags>[0-9A-Za-z\.\-]+))?\-(?<commitCount>\d+?)\-g(?<sha>[a-zA-Z0-9]+)/

    static final def gitDescribe(File rootDirectory) {
        def repository = new FileRepositoryBuilder()
                .findGitDir(rootDirectory)
                .build()

        def git = Git.wrap(repository)

        git.describe().setLong(true).call()
    }

    private final def parseGitDescribe(String describeOutput) {
        def matcher = describeOutput =~ pattern
        matcher.matches()
        [
                major: matcher.group('major'),
                minor: matcher.group('minor'),
                patch: matcher.group('patch'),
                commitCount: matcher.group('commitCount'),
                sha: matcher.group('sha'),
                tags: matcher.group('tags')
        ]
    }

    static final def gitVersionToString(v) {
        "${v.major}.${v.minor}.${v.patch}-${v.commitCount}"
    }

    @Override
    void apply(Project project) {
        def describeOut = gitDescribe(project.rootDir)
        def versionMap = parseGitDescribe(describeOut)
        def versionString = gitVersionToString(versionMap)
        project.ext.gitVersion = versionString
    }
}
