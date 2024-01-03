# Background  & Use case

Migrates Bitbucket Server/DC projects and repos to Gitea.
The native Gitea migrator does not currently handle LFS files but this migrator does.

The migration can be run several time, if the destination repo already exists a push will still be attempted and if nothing has 
changed in the destination the push should succeed. 

## Requirements: 

 * git and git-lfs installed and available with the env PATH variable
 * Groovy


## Known issues:
 1. Cloning empty bitbucket repos does not fail gracefully
 2. Currently, cant handle a collision between a bbProject.name and an existing giteaUser.name

## What it does

### In Bitbucket:

All repos available to the supplied user will be checked out including all branches, tags (mirror) and LFS resources to a temporary directory


### In gitea:

 If it doesn't already exist an organization is created with the following metadata mappings:
 * description: bbRepo/bbProject/description
 * fullName: bbRepo/bbProject/name
 * repoAdminChangeTeamAccess: true
 * username: bbRepo/bbProject/name
 * visibility: private

 If it doesn't already exist a repo is created with the following metadata mappings:
 * owner: bbRepo/bbProject/name
 * name: bbRepo/name
 * description: bbRepo/description

#### Note:
New gitea repos and orgs will have the following characters in their userNames replaced by "_"
 " " (space)
 ","

 The local temp file with all branches/tags/lfs-resources are pushed to the new gitea repo.
 The Temp file is deleted
 
