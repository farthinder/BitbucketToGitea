package se.stuxnet.gitea.migrations.bitbucket

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty


class BitbucketRepo {

    @JsonProperty("project")
    public Project project
    @JsonProperty("description")
    public String description
    @JsonProperty("hierarchyId")
    public String hierarchyId
    @JsonProperty("statusMessage")
    public String statusMessage
    @JsonProperty("archived")
    public Boolean archived
    @JsonProperty("forkable")
    public Boolean forkable
    @JsonProperty("defaultBranch")
    public String defaultBranch
    @JsonProperty("relatedLinks")
    public RelatedLinks relatedLinks
    @JsonProperty("partition")
    public Integer partition
    @JsonProperty("origin")
    public Map origin
    @JsonProperty("scmId")
    public String scmId
    @JsonProperty("slug")
    public String slug
    @JsonProperty("scope")
    public String scope
    @JsonProperty("name")
    public String name
    @JsonProperty("id")
    public Integer id
    @JsonProperty("state")
    public String state
    @JsonProperty("public")
    public Boolean _public

    @JsonProperty("links")
    public Map links

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

    String getHttpCloneLink() {

        return links.get("clone")?.find {it.name == "http"}?.get("href")
    }

    String toString() {
        project.key + "/" + name
    }



    static class Project {

        @JsonProperty("namespace")
        public String namespace
        @JsonProperty("description")
        public String description
        @JsonProperty("avatar")
        public String avatar
        @JsonProperty("scope")
        public String scope
        @JsonProperty("name")
        public String name
        @JsonProperty("key")
        public String key
        @JsonProperty("id")
        public Integer id
        @JsonProperty("type")
        public String type
        @JsonProperty("public")
        public Boolean _public
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

    }











    static class RelatedLinks {

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

    }

}











