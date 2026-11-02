package app.template.patches.gospellibrary

import app.morphe.patcher.Fingerprint

/**
 * Application.onCreate. Hilt apps declare the direct Application subclass as a generated
 * base class, so matching on the direct superclass finds an onCreate that always runs.
 */
object ApplicationOnCreateFingerprint : Fingerprint(
    name = "onCreate",
    returnType = "V",
    parameters = listOf(),
    custom = { _, classDef -> classDef.superclass == "Landroid/app/Application;" }
)
