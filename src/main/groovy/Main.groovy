import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import kong.unirest.core.*
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import se.stuxnet.gitea.migrations.bitbucket.BitbucketRepo
import se.stuxnet.gitea.migrations.bitbucket.GiteaOrg
import se.stuxnet.gitea.migrations.bitbucket.GiteaRepo
import java.util.concurrent.TimeoutException


String bbBaseUrl = System.getenv("bbBaseUrl") ?: "http://bitbucket.domain.local"
boolean trustAllBbCerts = true //Should all certs be trusted when making  HTTP request
String bbUser = System.getenv("bbUser") ?: "admin"
String bbPwOrToken = System.getenv("bbPwOrToken") ?: "some token or password" //Testing has mainly been done with token, but password might work



String giteaBaseUrl =  System.getenv("giteaBaseUrl") ?: "http://gitea.domain.local"
String giteaToken = System.getenv("giteaToken") ?: "some token or password" //Testing has mainly been done with token, but password might work
String giteaUser = System.getenv("giteaUser") ?: "admin"
boolean trustAllGiteaCerts = true //Should all certs be trusted when making  HTTP request


BitbucketRest bbRest = new BitbucketRest(bbBaseUrl, bbPwOrToken, trustAllBbCerts)
GiteaRest gRest = new GiteaRest(giteaBaseUrl, giteaToken, giteaUser, trustAllGiteaCerts)


ArrayList<BitbucketRepo>bbRepos = bbRest.getAllRepos()


bbRepos.each { bitbucketRepo ->

    gRest.migrateBitbucketRepo(bitbucketRepo, bbUser, bbPwOrToken, false)

}


//Restart migration from a specific repo in the list

/*
bbRepos.subList(bbRepos.findIndexOf {it.name == "Some BB Repo Name" } , bbRepos.size()).each { bitbucketRepo ->

    gRest.migrateBitbucketRepo(bitbucketRepo, bbUser, bbPwOrToken, false)

}

 */







@Slf4j
class GiteaRest {

    String baseUrl
    String token
    String username
    boolean trustAllCerts
    UnirestInstance unirest

    ObjectMapper objectMapper = new ObjectMapper()


    GiteaRest(String baseUrl, String token, String username , boolean trustAllCerts) {
        this.baseUrl = baseUrl
        this.token = token
        this.username = username
        this.trustAllCerts = trustAllCerts


        this.unirest = Unirest.spawnInstance()
        this.unirest.config().defaultBaseUrl(baseUrl).addDefaultHeader("Authorization", "token $token").verifySsl(!trustAllCerts)


        //this.unirest.config().proxy("localhost", 8082).connectTimeout(5 * 60000) //Enable proxy




    }

    void migrateBitbucketRepo(BitbucketRepo bitbucketRepo, String bitbucketUser, String bitbucketPassword, boolean deleteRepoInDestination) {

        log.info("Migrating BB Repo:" + bitbucketRepo.toString() + " " + bitbucketRepo.httpCloneLink)



        GiteaOrg giteaOrg = getOrgs().find { it.username == sanitizeName( bitbucketRepo.project.name) } ?: createOrg(new GiteaOrg(description: bitbucketRepo.project.description, fullName: bitbucketRepo.project.name, repoAdminChangeTeamAccess: true, username: sanitizeName( bitbucketRepo.project.name), visibility: "private"))


        GiteaRepo giteaRepo = getRepo(giteaOrg.username, bitbucketRepo.name)

        if (!giteaRepo) {
            giteaRepo = createRepo(giteaOrg.username, bitbucketRepo.name, bitbucketRepo.description)
        } else {

            log.warn("Git repo already exists:" + giteaRepo.htmlUrl)
            if (deleteRepoInDestination) {
                log.warn("\tDeleting duplicate repo, and creating a new one")
                deleteRepo(giteaRepo.owner.username, giteaRepo.name)
                giteaRepo = createRepo(giteaOrg.username, bitbucketRepo.name, bitbucketRepo.description)

            }
        }


        log.info("\tUsing gitea repo:" + giteaRepo.toString())

        File tempDir = LocalGit.mirrorRemote(bitbucketRepo.httpCloneLink, bitbucketUser, bitbucketPassword, true, giteaRepo.cloneUrl)

        log.info("\tDownloaded BB repo to : file:///" + tempDir.absolutePath)

        assert LocalGit.pushAll(tempDir, username, this.token): "Error pushing to new Gitea Repo"


        assert tempDir.deleteDir(): "Error deleting temp dir:" + tempDir.absolutePath

        log.info("\tFinished migrating to:" + giteaRepo.toString())

        ""

    }


    GiteaRepo createRepo(String owner, String name, String description = "") {

        HttpResponse<GiteaRepo> repo = unirest.post("/api/v1/orgs/$owner/repos").contentType("application/json").body([name: sanitizeName( name), description: description, private: true]).asObject(GiteaRepo)

        if (repo.status == 201) {
            return repo.body
        } else {
            log.error(repo.rawBody)
            return null
        }


    }

    GiteaRepo getRepo(String owner, String name) {


        HttpResponse<GiteaRepo> repo = unirest.get("/api/v1/repos/$owner/${sanitizeName(name)}").asObject(GiteaRepo)

        if (repo.status == 200) {
            return repo.body
        } else {
            return null
        }

    }



    boolean deleteRepo(String owner, String repo) {


        HttpResponse response = unirest.delete("/api/v1/repos/$owner/$repo").asEmpty()


        return response.status == 204

    }

    GiteaOrg createOrg(GiteaOrg newOrg) {

        log.info("Creating new Org:" + newOrg.username)
        Map postMap = objectMapper.convertValue(newOrg, Map)

        HttpResponse<GiteaOrg> response = unirest.post("/api/v1/orgs").contentType("application/json").body(postMap).asObject(GiteaOrg)

        assert response.status == 201: "Error creating Gitea Org:" + response?.rawBody
        return response.body

    }

    ArrayList<GiteaOrg> getOrgs() {

        //Does not appear to respect limit, always returns all
        HttpResponse<ArrayList<GiteaOrg>> response = unirest.get("/api/v1/orgs").asObject(new GenericType<ArrayList<GiteaOrg>>() {
        })

        return response.body
    }


    static String sanitizeName(String name) {

        return name.replaceAll(" " , "_").replaceAll(", ", " ").replaceAll(",", "")

    }

    ArrayList<GiteaRepo>getReposInOrg(String org) {

        HttpResponse<ArrayList<GiteaRepo>> response = unirest.get("/api/v1/orgs/$org/repos").queryString(["limit": 9999]).asObject(new GenericType<ArrayList<GiteaRepo>>(){})

        assert response.status == 200 : "Error getting repos in org:" + org

        return response.body

    }

    boolean deleteOrg(String orgName, boolean deleteAllRepos = false) {

        log.info("Deleting GiteaOrg:" + orgName + ", will also delete all repos:" + deleteAllRepos)

        if (deleteAllRepos) {
            ArrayList<GiteaRepo> reposInOrg = getReposInOrg(orgName)
            log.debug("\tGot ${reposInOrg.size()} repos to delete in $orgName")

            reposInOrg.each {repo ->
                assert deleteRepo(repo.owner.username, repo.name) : "Error deleting repo:" + repo.toString()
            }
        }

        HttpResponse response = unirest.delete("/api/v1/orgs/$orgName").asEmpty()

        assert response.status == 204 : "Error deleting org $orgName"

        return true



    }





}

@Slf4j
//https://developer.atlassian.com/server/bitbucket/rest/v815/api-group-repository/#api-api-latest-repos-get
class BitbucketRest {

    String baseUrl
    String token
    boolean trustAllCerts
    UnirestInstance unirest

    ObjectMapper objectMapper = new ObjectMapper()

    BitbucketRest(String baseUrl, String token, boolean trustAllCerts) {
        this.baseUrl = baseUrl
        this.token = token
        this.trustAllCerts = trustAllCerts


        this.unirest = Unirest.spawnInstance()
        this.unirest.config().defaultBaseUrl(baseUrl).addDefaultHeader("Authorization", "Bearer $token").verifySsl(!trustAllCerts)
        //this.unirest.config().proxy("localhost", 8082) //Enable proxy


    }

    ArrayList<BitbucketRepo> getAllRepos() {

        PagedList<Map> responses = getPagedResponse(unirest.get("/rest/api/latest/repos").queryString(["archived": "all"]), Map) as PagedList<Map>

        ArrayList<BitbucketRepo> repos = []

        responses.body.each {

            repos.addAll(objectMapper.convertValue(it.values, new TypeReference<List<BitbucketRepo>>() {}))
        }


        return repos

    }

    PagedList getPagedResponse(GetRequest getRequest, Class returnType) {

        String baseRequestUrl = getRequest.url

        PagedList response = getRequest.asPaged({ it.asObject(returnType) }, { pager(it, baseRequestUrl) })


        return response
    }


    Closure pager = { HttpResponse<Map> response, String url ->


        log.info("Getting next bitbucket page")

        assert response.status < 400: "Error was returned by bitbucket"
        if (response.body.isLastPage) {
            log.info("\tHave already reached last page")
            return null
        }

        Integer nextStart = response.body.nextPageStart as Integer
        log.debug("\tNext page starts with $nextStart")

        String nextUrl = url + "&start=$nextStart"
        log.info("\tNext url is:" + nextUrl)

        return nextUrl


    }

}


@Slf4j
class LocalGit {


    static boolean pushAll(File dir, String username, String password) {

        Git git = Git.open(dir)


        git.remoteList().call().each { remote ->
            log.info("Pushing to ${remote.name}")
            log.debug("\t" + remote.getURIs().collect { it.toString() }.join(","))



            BashOutput allOut = runBash("git -c credential.helper='!f() { echo \"username=$username\"; echo \"password=$password\"; }; f' push --mirror  ${remote.name}", dir)
            assert allOut.success: "Error pusing branches to ${remote.name}"

            BashOutput tagsOut = runBash("git -c credential.helper='!f() { echo \"username=$username\"; echo \"password=$password\"; }; f' push  --tags  ${remote.name}", dir)
            assert tagsOut.success: "Error pusing tags to ${remote.name}"


            BashOutput lfsOut = runBash("git lfs push --all origin", dir)
            assert lfsOut.success: "Error pushing lfs to ${remote.name}"


        }

        return true

    }


    static class BashOutput {

        ArrayList<String> stdOut
        ArrayList<String> errOut

        int exitValue

        boolean getSuccess() {

            return exitValue == 0
        }

        BashOutput(ArrayList<String> stdOut, ArrayList<String> errOut, int exitValue) {

            this.stdOut = stdOut
            this.errOut = errOut
            this.exitValue = exitValue
        }

    }

    static BashOutput runBash(String cmd, File cwd) {


        String path = System.getenv("PATH")

        ArrayList<String> stdOut = []
        ArrayList<String> errOut = []
        StringBuffer stdOutBuffer = new StringBuffer()
        StringBuffer errOutBuffer = new StringBuffer()

        Process bashProcess = ["bash", "-c", cmd + " && sleep 1"].execute(["PATH=$path"], cwd)
        bashProcess.consumeProcessOutput(stdOutBuffer, errOutBuffer)


        synchronized (bashProcess) {
            long timeout = 5 * 60000
            long start = System.currentTimeMillis()
            boolean alive = true


            while (alive) {


                errOutBuffer.eachLine { line, count ->
                    log.error("${line}")
                    errOut.add(line)
                    errOutBuffer.setLength(0)
                }
                stdOutBuffer.eachLine { line, count ->
                    log.info("${line}")
                    stdOut.add(line)
                    stdOutBuffer.setLength(0)
                }


                bashProcess.wait(1000)
                if (!bashProcess.alive) {

                    break
                }

                if (System.currentTimeMillis() > (start + timeout)) {
                    bashProcess.waitForOrKill(1)
                    throw new TimeoutException("Error waiting for bash CMD")
                }
            }
        }


        return new BashOutput(stdOut, errOut, bashProcess.exitValue())


    }

    static File mirrorRemote(String url, String userName, String password, boolean removeRemotes = true, String newRemote = "") {

        File tempDir = File.createTempDir()
        log.info("Mirroring repo to file:///" + tempDir.absolutePath)
        try {


            CredentialsProvider credentials = new UsernamePasswordCredentialsProvider(userName, password)


            Git git = Git.cloneRepository()
                    .setCloneAllBranches(true)
                    .setCloneSubmodules(true)
                    .setGitDir(tempDir)
                    .setURI(url)
                    .setCredentialsProvider(credentials)
                    .setMirror(true).call()


            BashOutput lsfFetchOut = runBash("git -c credential.helper='!f() { echo \"username=$userName\"; echo \"password=$password\"; }; f' lfs fetch --all", tempDir)


            assert lsfFetchOut.success

            git.remoteList().call().each { remote ->
                git.remoteRemove().setRemoteName(remote.name).call()

            }

            git.remoteAdd().setName("origin").setUri(new URIish(newRemote)).call()

            log.info("Mirrored repo to file:///" + tempDir.absolutePath)
            return tempDir

        } catch (Throwable ex) {
            log.error("Error mirroring remote git:" + ex.message)
            ex.stackTrace.each { log.error(it.toString()) }
        }

        return tempDir

    }


}

