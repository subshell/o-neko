## <small>1.5.1 (2022-08-09)</small>

* fix: don't log on trace per default ([b244acd](https://github.com/subshell/o-neko/commit/b244acd))
* fix(i18n): fix container registry tooltip in german translation ([f2486d3](https://github.com/subshell/o-neko/commit/f2486d3))
* chore: update to Angular 14 (#83) ([141b658](https://github.com/subshell/o-neko/commit/141b658)), closes [#83](https://github.com/subshell/o-neko/issues/83)
* chore: update to Java 17, update some dependencies and update Helm and Helm GCS in the Dockerfile (# ([60eb191](https://github.com/subshell/o-neko/commit/60eb191)), closes [#84](https://github.com/subshell/o-neko/issues/84)

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
