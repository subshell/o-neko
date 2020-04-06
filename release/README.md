# Releases

O-Neko releases follow the [semantic versioning guidelines](https://semver.org/). A release is identified by a git tag, for example `v1.0.0`.
A release, the frontend (`.frontend/package.json`) and the backend (`./pom.xml`) share the same version. Circle CI creates a new
Docker image for each new git tag with the git tag as its label and publishes it on [Docker Hub](https://hub.docker.com/r/subshellgmbh/o-neko/tags).

## Creating a new release 

To create a release you need Docker. First, create the Docker image:

    docker build ./release -t oneko-release

The new Docker image `oneko-release` contains all required tools to create releases. 
To prepare a release, run the Docker image and push the changes to the master branch:     

    docker run -it -v ${PWD}:/workspace oneko-release [<v[major.minor.patch]> | major | minor | patch]
    git push --tags origin master
    
That's it!🎉🎉🎉
    
 ### Example: Creating a minor release (1.1.12 -> 1.2.0)
    
Make sure that the Docker image oneko-release exists. If not, read the previous section again. 
 
    docker run -it -v ${PWD}:/workspace oneko-release minor
    git push --tags origin master
    
    # Output:
    [releases-with-git-tags 7057f8b] Release v1.2.0
     1 file changed, 1 insertion(+), 1 deletion(-)
    old version: 1.1.12
    new version: 1.2.0
    Run 'git push --tags origin master' to deploy.
