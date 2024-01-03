package se.stuxnet.gitea.migrations.bitbucket

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

class GiteaRepo {

    @JsonProperty("id")
    public Integer id
    @JsonProperty("owner")
    public GiteaOrg owner
    @JsonProperty("name")
    public String name
    @JsonProperty("full_name")
    public String fullName
    @JsonProperty("description")
    public String description
    @JsonProperty("empty")
    public Boolean empty
    @JsonProperty("private")
    public Boolean _private
    @JsonProperty("fork")
    public Boolean fork
    @JsonProperty("template")
    public Boolean template
    @JsonProperty("parent")
    public Object parent
    @JsonProperty("mirror")
    public Boolean mirror
    @JsonProperty("size")
    public Integer size
    @JsonProperty("language")
    public String language
    @JsonProperty("languages_url")
    public String languagesUrl
    @JsonProperty("html_url")
    public String htmlUrl
    @JsonProperty("url")
    public String url
    @JsonProperty("link")
    public String link
    @JsonProperty("ssh_url")
    public String sshUrl
    @JsonProperty("clone_url")
    public String cloneUrl
    @JsonProperty("original_url")
    public String originalUrl
    @JsonProperty("website")
    public String website
    @JsonProperty("stars_count")
    public Integer starsCount
    @JsonProperty("forks_count")
    public Integer forksCount
    @JsonProperty("watchers_count")
    public Integer watchersCount
    @JsonProperty("open_issues_count")
    public Integer openIssuesCount
    @JsonProperty("open_pr_counter")
    public Integer openPrCounter
    @JsonProperty("release_counter")
    public Integer releaseCounter
    @JsonProperty("default_branch")
    public String defaultBranch
    @JsonProperty("archived")
    public Boolean archived
    @JsonProperty("created_at")
    public String createdAt
    @JsonProperty("updated_at")
    public String updatedAt
    @JsonProperty("archived_at")
    public String archivedAt
    @JsonProperty("permissions")
    public Map permissions
    @JsonProperty("has_issues")
    public Boolean hasIssues
    @JsonProperty("internal_tracker")
    public Map internalTracker
    @JsonProperty("has_wiki")
    public Boolean hasWiki
    @JsonProperty("has_pull_requests")
    public Boolean hasPullRequests
    @JsonProperty("has_projects")
    public Boolean hasProjects
    @JsonProperty("has_releases")
    public Boolean hasReleases
    @JsonProperty("has_packages")
    public Boolean hasPackages
    @JsonProperty("has_actions")
    public Boolean hasActions
    @JsonProperty("ignore_whitespace_conflicts")
    public Boolean ignoreWhitespaceConflicts
    @JsonProperty("allow_merge_commits")
    public Boolean allowMergeCommits
    @JsonProperty("allow_rebase")
    public Boolean allowRebase
    @JsonProperty("allow_rebase_explicit")
    public Boolean allowRebaseExplicit
    @JsonProperty("allow_squash_merge")
    public Boolean allowSquashMerge
    @JsonProperty("allow_rebase_update")
    public Boolean allowRebaseUpdate
    @JsonProperty("default_delete_branch_after_merge")
    public Boolean defaultDeleteBranchAfterMerge
    @JsonProperty("default_merge_style")
    public String defaultMergeStyle
    @JsonProperty("default_allow_maintainer_edit")
    public Boolean defaultAllowMaintainerEdit
    @JsonProperty("avatar_url")
    public String avatarUrl
    @JsonProperty("internal")
    public Boolean internal
    @JsonProperty("mirror_interval")
    public String mirrorInterval
    @JsonProperty("mirror_updated")
    public String mirrorUpdated
    @JsonProperty("repo_transfer")
    public Object repoTransfer
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>()

    @JsonAnyGetter
    Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties
    }

    @JsonAnySetter
    void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value)
    }


    String toString() {
        return owner.username + "\\" + name + " (${htmlUrl})"
    }
}
