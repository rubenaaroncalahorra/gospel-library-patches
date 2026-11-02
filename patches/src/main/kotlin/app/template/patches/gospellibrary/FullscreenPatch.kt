package app.template.patches.gospellibrary

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_GOSPEL_LIBRARY

private const val EXTENSION_CLASS = "Lapp/template/extension/gospellibrary/FullscreenPatch;"

@Suppress("unused")
val fullscreenPatch = bytecodePatch(
    name = "Force fullscreen",
    description = "Hides the status and navigation bars and draws into the display cutout area " +
        "on every screen, including reading mode.",
    default = true
) {
    compatibleWith(COMPATIBILITY_GOSPEL_LIBRARY)

    extendWith("extensions/extension.mpe")

    execute {
        // Registers activity lifecycle callbacks that re-apply fullscreen whenever the app
        // (e.g. reading mode) shows the system bars again.
        ApplicationOnCreateFingerprint.method.addInstruction(
            0,
            "invoke-static { p0 }, $EXTENSION_CLASS->register(Landroid/app/Application;)V"
        )
    }
}
