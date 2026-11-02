package app.template.patches.gospellibrary

import app.morphe.patcher.Fingerprint

/**
 * Application.onCreate. The app's Application class may extend an intermediate base class
 * (Hilt generated, MultiDexApplication, etc), so match any non-abstract class whose direct
 * superclass name ends in "Application;". Such an onCreate always runs at process start.
 */
object ApplicationOnCreateFingerprint : Fingerprint(
    name = "onCreate",
    returnType = "V",
    parameters = listOf(),
    custom = { _, classDef -> classDef.superclass?.endsWith("Application;") == true }
)
