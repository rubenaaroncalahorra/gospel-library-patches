package app.template.patches.gospellibrary

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.PatchException
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
        // Class names are obfuscated and the Application class may sit below intermediate
        // base classes, so find every class that descends from android.app.Application.
        val parentOf = HashMap<String, String>()
        classDefForEach { c -> c.superclass?.let { parentOf[c.type] = it } }

        fun isApplication(type: String): Boolean {
            var t: String? = type
            var depth = 0
            while (t != null && depth++ < 64) {
                if (t == "Landroid/app/Application;") return true
                t = parentOf[t]
            }
            return false
        }

        val targets = parentOf.keys.filter { isApplication(it) }
            .mapNotNull { type ->
                classDefBy(type).methods.firstOrNull {
                    it.name == "onCreate" && it.returnType == "V" && it.parameters.isEmpty() &&
                        it.implementation != null
                }?.let { type }
            }

        if (targets.isEmpty()) throw PatchException("No Application.onCreate found")

        // register() is idempotent, so patching every onCreate in the chain is safe.
        targets.forEach { type ->
            val method = mutableClassDefBy(type).methods.first {
                it.name == "onCreate" && it.parameters.isEmpty()
            }
            method.addInstruction(
                0,
                "invoke-static { p0 }, $EXTENSION_CLASS->register(Landroid/app/Application;)V"
            )
        }
    }
}
