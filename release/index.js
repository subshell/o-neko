const path = require("path");
const childProcess = require("child_process");
const readline = require("readline");
const git = require("simple-git/promise")();

(async () => {
  const rootDir = path.join(__dirname, "..");
  const packageJsonDir = path.join(rootDir, "frontend");
  const packageJsonFilePath = path.join(packageJsonDir, "package.json");
  const pomXmlDir = rootDir;
  const pomXmlFilePath = path.join(rootDir, "pom.xml");
  const [, , versionId] = process.argv;

  if (!/^(v[0-9]+(\.[0-9]+)*|major|minor|patch)$/.test(versionId)) {
    throw new Error(
      "Version identifier is missing or invalid. Possible values are: [<v[major.minor.patch]> | major | minor | patch]"
    );
  }

  await git.cwd(rootDir);
  const status = await git.status();

  if (status.files.length > 0) {
    throw new Error(
      "Working directory is not clean. Please commit the changes before creating a release."
    );
  }

  const currentVersion = (await require(packageJsonFilePath)).version;

  // package JSON
  const nextVersionTag = await setAndGetNewPackageJsonVersion(
    packageJsonDir,
    versionId
  );
  const nextVersion = nextVersionTag.substr(1);

  // pom.xml
  await setPomXmlVersion(pomXmlDir, nextVersionTag);

  console.log(`
  ===  
  old version: v${currentVersion}
  new version: v${nextVersion}

  This will create a new release with the git tag v${nextVersion} and push it to origin master. 
  ==
  `);

  const confirmed = await askForConfirmation();
  if (confirmed) {
    await git.commit(`Release ${nextVersionTag}`, [
      packageJsonFilePath,
      pomXmlFilePath,
    ]);
    await git.addTag(nextVersionTag)
    await git.push("origin master");
    await git.pushTags();

    console.log("\nSUCCESS!")
  } else {
    try {
      await git.checkout(["--", packageJsonFilePath, pomXmlFilePath]);
    } catch(e) {
      console.error(e)
    }
  }
})().catch((err) => console.error(err));

const setAndGetNewPackageJsonVersion = (packageJsonDir, versionId) =>
  new Promise((res, rej) => {
    childProcess.exec(
      `cd ${packageJsonDir} && npm version ${versionId}`,
      (e, version) => (e ? rej(e) : res(version))
    );
  }).then(s => s.replace(/(\r\n|\n|\r)/gm, ""));

const setPomXmlVersion = (pomXmlDir, versionId) => {
  new Promise((res, rej) => {
    childProcess.exec(
      `cd ${pomXmlDir} && mvn versions:set -DnewVersion="${versionId}" -DgenerateBackupPoms=false`,
      (e, version) => (e ? rej(e) : res(version))
    );
  });
};

const askForConfirmation = () =>
  new Promise((res) => {
    const readStream = readline.createInterface({
      input: process.stdin,
      output: process.stdout,
    });
    readStream.question("Does this look right? (y/N) ", (answer) => {
      if (!answer || answer === "n") {
        res(false);
        readStream.close();
      } else if (answer.toLowerCase() === "y") {
        res(true);
        readStream.close();
      }
    });
  });
