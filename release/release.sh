#!/bin/bash

# Release script
# ===============
# Sets the new version for the package.json file, pom.xml and as a git tag.

versionIdentifier=$1
if [[ ! $versionIdentifier =~ ^(v[0-9]+(\.[0-9]+)*|major|minor|patch)$ ]]; then
    echo "Version identifier is missing or invalid. Possible values are: [<v[major.minor.patch]> | major | minor | patch]"
    exit 1
fi

if [ -n "$(git status --porcelain)" ]; then
  # Uncommitted changes
  echo "Working directory is not clean. Please commit the changes before creating a release."
  exit 1
fi

rootDir="$(dirname "$0")"/..
frontendDir="${rootDir}"/frontend
pomDir="${rootDir}"

currentVersion=$(node -pe "require('${frontendDir}/package.json').version")

# update version (package.json)
nextTag=$(cd "${frontendDir}" && npm version "${versionIdentifier}")
nextVersion="${nextTag:1}"

# update version (pom.xml)
mvn versions:set -DnewVersion="${nextVersion}" -DgenerateBackupPoms=false

# git tag
git add "${frontendDir}/package.json" "${pomDir}/pom.xml"
git commit -m "Release ${nextTag}"
git tag "${nextTag}"

echo "old version: ${currentVersion}"
echo "new version: ${nextVersion}"
echo "Run 'git push --tags origin master' to deploy."