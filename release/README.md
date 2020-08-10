# Releases

O-Neko releases follow the [semantic versioning guidelines](https://semver.org/). A release is identified by a git tag, for example `v1.0.0`.
Frontend (`/frontend/package.json`) and backend (`/pom.xml`) are released together and therefore always have the same version. Circle CI creates a new
Docker image for each new git tag with the git tag as its label and publishes it to [Docker Hub](https://hub.docker.com/r/subshellgmbh/o-neko/tags).

## Creating a new release 

To create a release you need NodeJs. First, install the dependencies:

    cd release && npm i

To prepare a release, run the NPM script:     

    npm run release [<v[major.minor.patch]> | major | minor | patch]
    
That's it!🎉🎉🎉
    
 ### Example: Creating a minor release (1.1.12 -> 1.2.0)
    
Make sure the Node dependencies are installed. If not, run `npm i` in the `release` directory. 
 
    npm run release minor
    
    ## Output:
    
    > release@1.0.0 start /home/schoener/gitworkspace/test-private/release
    > node index.js "v1.0.16"
    
      ===  
      old version: v1.1.12
      new version: v1.2.0
    
      This will create a new release with the git tag v1.0.16 and push it to origin master. 
      ==
      
    Does this look right? (y/N) y
    
    SUCCESS!
