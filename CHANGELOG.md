# [2.0.0](https://github.com/gravitee-io/gravitee-policy-assign-content/compare/1.7.0...2.0.0) (2023-07-18)


### Bug Fixes

* use new execution mode ([91bba78](https://github.com/gravitee-io/gravitee-policy-assign-content/commit/91bba785f4a53acea75c26a730291012eb56a8fc))


### chore

* **deps:** update gravitee-parent ([d7c1221](https://github.com/gravitee-io/gravitee-policy-assign-content/commit/d7c122120b4c9010a10c5e932bb776f4c8004604))


### Features

* clean and validate json schema for v4 ([dc6eca2](https://github.com/gravitee-io/gravitee-policy-assign-content/commit/dc6eca2fd86be00e9dc64bc1c4240a107006bfc5))
* make the policy compatible with V4 API (Proxy & Message) ([33fba04](https://github.com/gravitee-io/gravitee-policy-assign-content/commit/33fba042326d280a1e90865b0c2f46aa8353b0a1))


### BREAKING CHANGES

* **deps:** require Java17
* this policy is now using the V4 interfaces

fix APIM-1622

# [2.0.0-alpha.3](https://github.com/gravitee-io/gravitee-policy-assign-content/compare/2.0.0-alpha.2...2.0.0-alpha.3) (2023-06-29)


### Bug Fixes

* use new execution mode ([91bba78](https://github.com/gravitee-io/gravitee-policy-assign-content/commit/91bba785f4a53acea75c26a730291012eb56a8fc))

# [2.0.0-alpha.2](https://github.com/gravitee-io/gravitee-policy-assign-content/compare/2.0.0-alpha.1...2.0.0-alpha.2) (2023-06-28)


### Features

* clean and validate json schema for v4 ([dc6eca2](https://github.com/gravitee-io/gravitee-policy-assign-content/commit/dc6eca2fd86be00e9dc64bc1c4240a107006bfc5))

# [2.0.0-alpha.1](https://github.com/gravitee-io/gravitee-policy-assign-content/compare/1.7.0...2.0.0-alpha.1) (2023-06-23)


### Features

* make the policy compatible with V4 API (Proxy & Message) ([33fba04](https://github.com/gravitee-io/gravitee-policy-assign-content/commit/33fba042326d280a1e90865b0c2f46aa8353b0a1))


### BREAKING CHANGES

* this policy is now using the V4 interfaces

fix APIM-1622

# [1.7.0](https://github.com/gravitee-io/gravitee-policy-assign-content/compare/1.6.0...1.7.0) (2022-01-21)


### Bug Fixes

* **assign-content:** Do not allow template injection ([bc6595d](https://github.com/gravitee-io/gravitee-policy-assign-content/commit/bc6595d8d1249b1e68d26052167ed5adeaace309)), closes [gravitee-io/issues#5033](https://github.com/gravitee-io/issues/issues/5033)
* upgrade org.freemarker:freemarker from 2.3.30 to 2.3.31 ([75f1c72](https://github.com/gravitee-io/gravitee-policy-assign-content/commit/75f1c72a2c62848898d2938fe37d3efbca6e660d))


### Features

* **headers:** Internal rework and introduce HTTP Headers API ([a63b6f6](https://github.com/gravitee-io/gravitee-policy-assign-content/commit/a63b6f6e2d5466467c16389d9b190365fb5f7df0)), closes [gravitee-io/issues#6772](https://github.com/gravitee-io/issues/issues/6772)
* **perf:** adapt policy for new classloader system ([56aa796](https://github.com/gravitee-io/gravitee-policy-assign-content/commit/56aa796d1a47cf2601db5ecf4b709576a9ca5bab)), closes [gravitee-io/issues#6758](https://github.com/gravitee-io/issues/issues/6758)
