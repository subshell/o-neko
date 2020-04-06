# Releases
    
Releases are created with Docker. Alternatively, directly execute the `./release/release.sh` script. First, create the Docker image:

    docker build ./release -t oneko-release

Then, prepare the release by running the Docker container and push the changes to the master branch:     

    docker run -it -v ${PWD}:/workspace oneko-release [<v[major.minor.patch]> | major | minor | patch]
    git push --tags origin master