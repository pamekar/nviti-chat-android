# Release process and registry status

Source releases are published on GitHub. A GitHub release/tag does not mean a
package was uploaded to npm, pub.dev or Maven Central.

## Before a registry release

1. Run CI, clean-checkout examples and package-content checks.
2. Review dependency audits and platform/device limitations.
3. Select a version, update the changelog and publish an immutable Git tag.
4. Authenticate to the intended registry and verify namespace ownership.
5. Publish from a clean checkout, then install the registry artifact in a fresh app.

Never reuse a version for different contents or force-move a published tag.

## Registry-specific gates

- npm: an authenticated publisher with access to `@nviti`, required 2FA or
  trusted publishing, and reviewed package contents. Use `npm pack --dry-run`
  before publishing. [Official scoped-package guide](https://docs.npmjs.com/creating-and-publishing-scoped-public-packages/).
- pub.dev: authenticate the intended publisher and validate with
  `flutter pub publish --dry-run`. [Official publication guide](https://dart.dev/tools/pub/publishing).
- Maven Central: verify the `ng.nviti` namespace, supply publishing credentials
  and signing keys, and complete required source/documentation artifacts and
  POM metadata. The current Gradle project can build an AAR; that alone is not
  a Central publication pipeline. [Central requirements](https://central.sonatype.org/publish/requirements/).
- Swift Package Manager resolves public Git source/tags; it does not need an
  npm/pub.dev/Maven account. Validate Xcode builds and simulator tests first.

## Current preview boundary

Android, Flutter and React Native have local SDK tests and public CI. The iOS
package also passed its macOS Xcode simulator build/tests on 7 September 2026.
Physical-device acceptance and host-specific media/push implementations remain
integration responsibilities.

The React Native 0.76 validation/demo dependency tree has outstanding security
advisories. Do not expose Metro to untrusted networks or process untrusted
build assets. Upgrade and re-audit the host toolchain before a production
release; passing unit tests is not a clean security audit.
