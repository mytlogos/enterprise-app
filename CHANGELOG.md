## [1.0.1](https://github.com/mytlogos/enterprise-app/compare/v1.0.0...v1.0.1) (2022-12-13)


### Bug Fixes

* **ci:** use correct path for apk ([8a30139](https://github.com/mytlogos/enterprise-app/commit/8a3013939d281fe8940c0b8a0c7bfa22b6c80644))

# 1.0.0 (2022-12-13)


### Bug Fixes

* (BREAK) remove pdf viewer and some dependencies ([47cebbd](https://github.com/mytlogos/enterprise-app/commit/47cebbded80cace9e533ee81e3d948fb301530b2))
* add missing request body parameter ([2051023](https://github.com/mytlogos/enterprise-app/commit/2051023cfa4e82e55f769fecab1d72ba55beacfe))
* allow UDP discovery to be canceled ([9f1df6a](https://github.com/mytlogos/enterprise-app/commit/9f1df6adab59316b2ec98983d88a6e40450a8d58))
* commit missing changes for RoomStorage.kt ([d3745d5](https://github.com/mytlogos/enterprise-app/commit/d3745d5f6c0d75bdc2ad41d751c78ffd0bd68ee8))
* correct variable name ([3bd2224](https://github.com/mytlogos/enterprise-app/commit/3bd2224479b405e8f209da63e9e588dd2825d8e5))
* correct wrong property name ([f0cd8b9](https://github.com/mytlogos/enterprise-app/commit/f0cd8b912247969c4faa40a9ef9b16aed50bcaa0))
* correctly call click listeners ([14479b7](https://github.com/mytlogos/enterprise-app/commit/14479b7faefa85225866c5adf9ef9ec078db1b68))
* do not export broadcast receiver ([84e1180](https://github.com/mytlogos/enterprise-app/commit/84e118007568eea7ed2bc2e0bb0fb42e22eda2fc))
* fix sync changes notification ([0e6d3fc](https://github.com/mytlogos/enterprise-app/commit/0e6d3fc89e1f482492391a8e8d268f9b264ccdd9))
* fix test ([3e214e8](https://github.com/mytlogos/enterprise-app/commit/3e214e89131188af59a96d3fc8b5d00ac4223e83))
* force Dispatchers.IO ([92123d7](https://github.com/mytlogos/enterprise-app/commit/92123d77f3a16247a9ce6d1684f2b35c749f72f1))
* image viewer not opening ([a67d653](https://github.com/mytlogos/enterprise-app/commit/a67d653ca8538f891233f81388ce60ff9c897c62))
* images not downloading ([a16f82b](https://github.com/mytlogos/enterprise-app/commit/a16f82b6010204b4c66c789629d4f37f0839838f))
* missing adapter on recyclerview ([a67d8f2](https://github.com/mytlogos/enterprise-app/commit/a67d8f273327554fecbf35c97f8aa7e80aaf71a8))
* order of default position value ([22745f5](https://github.com/mytlogos/enterprise-app/commit/22745f5c53fc30af9e007a3747a4c14969a58b89))
* prevent room queries with too big collections ([213bc2f](https://github.com/mytlogos/enterprise-app/commit/213bc2fdc59633bd8d3792f4ef8143a4e4af2be4))
* use correct port numbers ([633dfd0](https://github.com/mytlogos/enterprise-app/commit/633dfd0ee768b8957eeade279dc84235d83138fc))
* used wrong parameter in doChunked ([ce3fa67](https://github.com/mytlogos/enterprise-app/commit/ce3fa6779e127183bc17891847959767aa3b4074))
* Worker constructor not instantiable ([660a754](https://github.com/mytlogos/enterprise-app/commit/660a75419e77d32bbc06c85102b633b9bfd20b5b))
* Worker constructor not instantiable ([41aef3e](https://github.com/mytlogos/enterprise-app/commit/41aef3ea3df126cb625e8971836fe02d0314de09))


### Features

* add some indices ([d5275d0](https://github.com/mytlogos/enterprise-app/commit/d5275d069fcd89b224ffbcfe51691a7400142939))
* allow setting server ([fbc7192](https://github.com/mytlogos/enterprise-app/commit/fbc7192d29b51f186b98da95cff64dfc2065adef))
* allow starting CheckSavedWorker via Option Menu ([8bb8aac](https://github.com/mytlogos/enterprise-app/commit/8bb8aac49a111e9d03a2f3494d7b643ff6941ff5))
* display Medium Title in Toc View ([ada0205](https://github.com/mytlogos/enterprise-app/commit/ada0205e64324ce00888537758a91801185c85ec))
* dont run CheckSaved after Download ([9f5132b](https://github.com/mytlogos/enterprise-app/commit/9f5132b5b0263b5f339bcc4b4db06a8526e4e54d))
* mark chapter as read from EpisodeFragment ([e3c0ec2](https://github.com/mytlogos/enterprise-app/commit/e3c0ec29da1b946174d1c2e0cde101874410ded8))
* mark chapter as read from ViewerFragment ([9963220](https://github.com/mytlogos/enterprise-app/commit/9963220f3a576b78f77b60ba3542254f1f82e9c6))
* Migrate to Paging 3 Features in EpisodeFragment.kt ([fe52ebf](https://github.com/mytlogos/enterprise-app/commit/fe52ebf6b3c02fae3d50d75c4ecc962cf08fe831))
* Migrate to Paging 3 Features in MediumListFragment.kt ([d39991e](https://github.com/mytlogos/enterprise-app/commit/d39991e144aa39c128fe93d11db1f5c0974e19bd))
* semantic release ([eb28161](https://github.com/mytlogos/enterprise-app/commit/eb28161aa78590151054eb40cd8317b6d7bb7711))
* update client api to match web api ([e3b2c5c](https://github.com/mytlogos/enterprise-app/commit/e3b2c5cbb3f86bc765d24bc763f447fb7cd239d6))
* update state instead of deleting local episode ([136b716](https://github.com/mytlogos/enterprise-app/commit/136b7160d495f0e2af5e9205febf1911ded14fa8))
* use coroutines in DownloadWorker.kt ([4fc7929](https://github.com/mytlogos/enterprise-app/commit/4fc7929425b6b464371bb46674b5428c240fff9d))
* use Kotlin features in TocFragment.kt ([bd41070](https://github.com/mytlogos/enterprise-app/commit/bd41070f345ef036674340760095d743c7f4e370))
* use simpler query api for SynchronizeWorker.kt ([44f0c5f](https://github.com/mytlogos/enterprise-app/commit/44f0c5f7be8b967d98b515ab6bcf6f14bc856308))
