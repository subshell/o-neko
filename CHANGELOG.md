## 1.8.2

* fix: ensure the API can poll OCI manifests

## 1.8.1 (2025-12-17)

* prevent NullPointerExceptions when filling Immutable collections

## 1.8.0 (2023-10-09)

### Highlights

* O-Neko now has a built-in container logs view. The view also includes a new configuration option which can contain a URL 
  template to an external logging system, which is used to add a direct link to the correct logs.

### What's Changed
* update all backend dependencies by @philmtd in https://github.com/subshell/o-neko/pull/230
* Update frontend dependencies by @philmtd in https://github.com/subshell/o-neko/pull/231
* fixes polling of multiplatform images by @philmtd in https://github.com/subshell/o-neko/pull/232
* feat: add container logs view to frontend by @philmtd in https://github.com/subshell/o-neko/pull/233

**Full Changelog**: https://github.com/subshell/o-neko/compare/1.7.0...1.8.0

## 1.7.0 (2023-04-12)

### Highlights
* O-Neko now has a search functionality, which allows to deploy multiple versions simultaneously (#109, #112, #130)

### What's Changed
* deps: update java dependencies by @philmtd in https://github.com/subshell/o-neko/pull/108
* feat: Implement simple search API by @philmtd in https://github.com/subshell/o-neko/pull/109
* deps: update frontend dependencies by @philmtd in https://github.com/subshell/o-neko/pull/110
* feat: implement search field by @philmtd in https://github.com/subshell/o-neko/pull/112
* chore(deps): bump springdoc.version from 1.6.14 to 1.6.15 by @dependabot in https://github.com/subshell/o-neko/pull/115
* chore(deps): bump caffeine from 3.1.4 to 3.1.5 by @dependabot in https://github.com/subshell/o-neko/pull/119
* chore(deps): bump spring.boot.version from 3.0.3 to 3.0.5 by @dependabot in https://github.com/subshell/o-neko/pull/120
* chore(deps): bump feign.version from 12.1 to 12.2 by @dependabot in https://github.com/subshell/o-neko/pull/118
* chore(deps-dev): bump de.flapdoodle.embed.mongo from 4.5.1 to 4.6.2 by @dependabot in https://github.com/subshell/o-neko/pull/116
* update material icons by @philmtd in https://github.com/subshell/o-neko/pull/114
* chore(deps): bump springdoc.version from 1.6.15 to 1.7.0 by @dependabot in https://github.com/subshell/o-neko/pull/129
* chore(deps): bump kubernetes-client from 6.4.1 to 6.5.1 by @dependabot in https://github.com/subshell/o-neko/pull/128
* chore(deps): bump maven-compiler-plugin from 3.8.1 to 3.11.0 by @dependabot in https://github.com/subshell/o-neko/pull/127
* chore(deps): bump maven-resources-plugin from 3.1.0 to 3.3.1 by @dependabot in https://github.com/subshell/o-neko/pull/126
* chore(deps): bump caffeine from 3.1.5 to 3.1.6 by @dependabot in https://github.com/subshell/o-neko/pull/142
* chore(deps): bump feign.version from 12.2 to 12.3 by @dependabot in https://github.com/subshell/o-neko/pull/140
* feat(search): add search page by @philmtd in https://github.com/subshell/o-neko/pull/130

## 1.6.0 (2023-02-15)

* chore: update backend dependencies (#105) ([afdd7a5](https://github.com/subshell/o-neko/commit/afdd7a5)), closes [#105](https://github.com/subshell/o-neko/issues/105)
* chore(deps): bump engine.io from 6.2.0 to 6.2.1 in /frontend (#98) ([f12932f](https://github.com/subshell/o-neko/commit/f12932f)), closes [#98](https://github.com/subshell/o-neko/issues/98)
* chore(deps): bump http-cache-semantics from 4.1.0 to 4.1.1 in /frontend (#107) ([a94639c](https://github.com/subshell/o-neko/commit/a94639c)), closes [#107](https://github.com/subshell/o-neko/issues/107)
* chore(deps): bump json5 from 2.2.1 to 2.2.3 in /frontend (#101) ([5c82ecd](https://github.com/subshell/o-neko/commit/5c82ecd)), closes [#101](https://github.com/subshell/o-neko/issues/101)
* chore(deps): bump loader-utils from 2.0.2 to 2.0.4 in /frontend (#97) ([dae510e](https://github.com/subshell/o-neko/commit/dae510e)), closes [#97](https://github.com/subshell/o-neko/issues/97)
* chore(deps): bump qs from 6.5.2 to 6.5.3 in /frontend (#100) ([b205108](https://github.com/subshell/o-neko/commit/b205108)), closes [#100](https://github.com/subshell/o-neko/issues/100)
* chore(deps): bump ua-parser-js from 0.7.31 to 0.7.33 in /frontend (#106) ([d70b09d](https://github.com/subshell/o-neko/commit/d70b09d)), closes [#106](https://github.com/subshell/o-neko/issues/106)
* fix: change blocking deploy API calls (#102) ([21ba098](https://github.com/subshell/o-neko/commit/21ba098)), closes [#102](https://github.com/subshell/o-neko/issues/102)
* update angular and some other frontend libraries (#104) ([6b3cc3b](https://github.com/subshell/o-neko/commit/6b3cc3b)), closes [#104](https://github.com/subshell/o-neko/issues/104)
* update docker base image, helm and helm gcs (#103) ([2fe1737](https://github.com/subshell/o-neko/commit/2fe1737)), closes [#103](https://github.com/subshell/o-neko/issues/103)

## <small>1.5.4 (2022-10-21)</small>

* add monaco-editor to dependencies (#94) ([11ef36a](https://github.com/subshell/o-neko/commit/11ef36a)), closes [#94](https://github.com/subshell/o-neko/issues/94)
* update java dependencies (#95) ([7452614](https://github.com/subshell/o-neko/commit/7452614)), closes [#95](https://github.com/subshell/o-neko/issues/95)

## <small>1.5.3 (2022-10-17)</small>

* deps: remove snakeyaml (#92) ([ba22a7b](https://github.com/subshell/o-neko/commit/ba22a7b)), closes [#92](https://github.com/subshell/o-neko/issues/92)
* deps: update commons text (#93) ([3e13dfe](https://github.com/subshell/o-neko/commit/3e13dfe)), closes [#93](https://github.com/subshell/o-neko/issues/93)
* chore(deps): bump snakeyaml from 1.30 to 1.31 ([d65b172](https://github.com/subshell/o-neko/commit/d65b172))


##  <small>1.5.2 (2022-09-09)</small>

* chore: update changelog ([bd6953f](https://github.com/subshell/o-neko/commit/bd6953f))
* chore(deps): bump ini from 1.3.5 to 1.3.8 in /frontend (#87) ([e32f62a](https://github.com/subshell/o-neko/commit/e32f62a)), closes [#87](https://github.com/subshell/o-neko/issues/87)
* chore(deps): bump json-schema from 0.2.3 to 0.4.0 in /frontend (#90) ([cbc7c9e](https://github.com/subshell/o-neko/commit/cbc7c9e)), closes [#90](https://github.com/subshell/o-neko/issues/90)
* chore(deps): bump jszip from 3.4.0 to 3.10.1 in /frontend (#86) ([3b9f43e](https://github.com/subshell/o-neko/commit/3b9f43e)), closes [#86](https://github.com/subshell/o-neko/issues/86)
* chore(deps): bump y18n from 4.0.0 to 4.0.3 in /frontend (#85) ([ae14d4c](https://github.com/subshell/o-neko/commit/ae14d4c)), closes [#85](https://github.com/subshell/o-neko/issues/85)
* add wait option to "helm uninstall" and "helm install" (#89) ([443b8fb](https://github.com/subshell/o-neko/commit/443b8fb)), closes [#89](https://github.com/subshell/o-neko/issues/89)
* remove dockerize_featurebranch step in pipeline ([c9c51f9](https://github.com/subshell/o-neko/commit/c9c51f9))
* Updated description ([0c9e7ea](https://github.com/subshell/o-neko/commit/0c9e7ea))
* Updated footer ([cacf255](https://github.com/subshell/o-neko/commit/cacf255))
* readme: refer to our helm chart for installation instructions (#88) ([0b0d2d0](https://github.com/subshell/o-neko/commit/0b0d2d0)), closes [#88](https://github.com/subshell/o-neko/issues/88)

## <small>1.5.1 (2022-08-09)</small>

* fix: don't log on trace per default ([b244acd](https://github.com/subshell/o-neko/commit/b244acd))
* fix(i18n): fix container registry tooltip in german translation ([f2486d3](https://github.com/subshell/o-neko/commit/f2486d3))
* chore: update to Angular 14 (#83) ([141b658](https://github.com/subshell/o-neko/commit/141b658)), closes [#83](https://github.com/subshell/o-neko/issues/83)
* chore: update to Java 17, update some dependencies and update Helm and Helm GCS in the Dockerfile (# ([60eb191](https://github.com/subshell/o-neko/commit/60eb191)), closes [#84](https://github.com/subshell/o-neko/issues/84)
* update changelog ([f2a81e0](https://github.com/subshell/o-neko/commit/f2a81e0))

## 1.5.0 (2022-07-11)

* feat(metrics): add metrics (#79) ([ce8a360](https://github.com/subshell/o-neko/commit/ce8a360)), closes [#79](https://github.com/subshell/o-neko/issues/79)
* chore(deps): bump eventsource from 1.1.0 to 1.1.2 in /frontend ([dbe11da](https://github.com/subshell/o-neko/commit/dbe11da))
* chore(deps): bump follow-redirects from 1.14.1 to 1.15.1 in /frontend ([bfd295a](https://github.com/subshell/o-neko/commit/bfd295a))
* chore(docs): Update changelog ([67cab5a](https://github.com/subshell/o-neko/commit/67cab5a))
* chore(deps): Bump async from 2.6.3 to 2.6.4 in /frontend ([97084da](https://github.com/subshell/o-neko/commit/97084da))
* chore(deps): Bump gson from 2.8.6 to 2.8.9 ([a93a986](https://github.com/subshell/o-neko/commit/a93a986))
* chore(deps): Bump karma from 6.3.3 to 6.3.16 in /frontend ([4cb05fc](https://github.com/subshell/o-neko/commit/4cb05fc))
* chore(deps): Bump minimist from 1.2.5 to 1.2.6 in /frontend ([adacae2](https://github.com/subshell/o-neko/commit/adacae2))
* chore(deps): Bump nanoid from 3.1.25 to 3.3.4 in /frontend ([5805956](https://github.com/subshell/o-neko/commit/5805956))
* chore(deps): Bump url-parse from 1.5.3 to 1.5.10 in /frontend (#69) ([a44121b](https://github.com/subshell/o-neko/commit/a44121b)), closes [#69](https://github.com/subshell/o-neko/issues/69)
* chore: Delete CNAME ([b836c03](https://github.com/subshell/o-neko/commit/b836c03))
* fix: improved logging, concurrent deployment editing and rollback handling (#78) ([dc2016c](https://github.com/subshell/o-neko/commit/dc2016c)), closes [#78](https://github.com/subshell/o-neko/issues/78) [#77](https://github.com/subshell/o-neko/issues/77)


## 1.4.0 (2021-11-16)

* feat(wakeup): add advanced sleep and wakeup features (#60) ([345e8c6](https://github.com/subshell/o-neko/commit/345e8c6)), closes [#60](https://github.com/subshell/o-neko/issues/60) [#58](https://github.com/subshell/o-neko/issues/58) [#59](https://github.com/subshell/o-neko/issues/59) [#61](https://github.com/subshell/o-neko/issues/61) [#62](https://github.com/subshell/o-neko/issues/62) [#65](https://github.com/subshell/o-neko/issues/65)
* fix(deployment): stop deleted versions and delete stopped deployments (#64) ([cba3d49](https://github.com/subshell/o-neko/commit/cba3d49)), closes [#64](https://github.com/subshell/o-neko/issues/64)
* Add docker version badge ([b4e5274](https://github.com/subshell/o-neko/commit/b4e5274))


## 1.3.0 (2021-08-30)

* feat(helm): Update Helm and base Docker image (#55) ([bee922c](https://github.com/subshell/o-neko/commit/bee922c)), closes [#55](https://github.com/subshell/o-neko/issues/55)
* feat(i18n): Finalize translations (#57) ([f8c1a92](https://github.com/subshell/o-neko/commit/f8c1a92)), closes [#57](https://github.com/subshell/o-neko/issues/57)
* change(helm): shorten length of project version in Helm release name to 20 characters (#56) ([214c9f6](https://github.com/subshell/o-neko/commit/214c9f6)), closes [#56](https://github.com/subshell/o-neko/issues/56)



## 1.2.0 (2021-06-11)

* Bump lodash from 4.17.20 to 4.17.21 in /frontend (#49) ([13a319c](https://github.com/subshell/o-neko/commit/13a319c)), closes [#49](https://github.com/subshell/o-neko/issues/49)
* Structured Logging (#51) ([1bab8c6](https://github.com/subshell/o-neko/commit/1bab8c6)), closes [#51](https://github.com/subshell/o-neko/issues/51)
* update guava + fabric8 k8s client, remove unused docker-client dependency ([b0afab2](https://github.com/subshell/o-neko/commit/b0afab2))
* Update to Angular 12 (#50) ([9625249](https://github.com/subshell/o-neko/commit/9625249)), closes [#50](https://github.com/subshell/o-neko/issues/50)
* feat(templates): add "suffixWithMaxLength" ([ba65546](https://github.com/subshell/o-neko/commit/ba65546)), closes [#53](https://github.com/subshell/o-neko/issues/53)
* fix(event-log): Make the event log pageable and fix websocket connection issues ([64dc452](https://github.com/subshell/o-neko/commit/64dc452))



## <small>1.1.3 (2021-02-19)</small>

* fix scheduler thread pool size (#47) ([9b7929d](https://github.com/subshell/o-neko/commit/9b7929d)), closes [#47](https://github.com/subshell/o-neko/issues/47)
* Increase scheduling thread pool size to 5 per default ([8357c81](https://github.com/subshell/o-neko/commit/8357c81))



## <small>1.1.2 (2021-02-05)</small>

* read project permissions async (#46) ([00454c9](https://github.com/subshell/o-neko/commit/00454c9)), closes [#46](https://github.com/subshell/o-neko/issues/46)



## <small>1.1.1 (2021-02-04)</small>

* Changes to the release name pattern in helm (#45) ([3980c7d](https://github.com/subshell/o-neko/commit/3980c7d)), closes [#45](https://github.com/subshell/o-neko/issues/45)
* push latest docker tag on release ([02d3ab7](https://github.com/subshell/o-neko/commit/02d3ab7))



## 1.1.0 (2021-02-04)

* Change template processor ([213ae2e](https://github.com/subshell/o-neko/commit/213ae2e))
* create latest tag on release and omit v in release tag ([a179b14](https://github.com/subshell/o-neko/commit/a179b14))



## 1.0.0 (2021-02-03)

* First public release
