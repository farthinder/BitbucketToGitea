package se.stuxnet.gitea.migrations.bitbucket

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

class GiteaOrg {

    @JsonProperty("id")
    public Integer id
    @JsonProperty("name")
    public String name
    @JsonProperty("full_name")
    public String fullName
    @JsonProperty("email")
    public String email
    @JsonProperty("avatar_url")
    public String avatarUrl
    @JsonProperty("description")
    public String description
    @JsonProperty("website")
    public String website
    @JsonProperty("location")
    public String location
    @JsonProperty("visibility")
    public String visibility
    @JsonProperty("repo_admin_change_team_access")
    public Boolean repoAdminChangeTeamAccess
    @JsonProperty("username")
    public String username
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
        return fullName + " ($username)"
    }



    Map toPostMap() {

        Map outMap = this as Map

        return outMap
    }

}