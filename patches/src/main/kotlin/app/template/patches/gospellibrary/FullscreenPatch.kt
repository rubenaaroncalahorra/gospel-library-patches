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
        val parentOf = HashMap<String, String>()
        classDefForEach { c -> c.superclass?.let { parentOf[c.type] = it } }

        fun descendsFrom(type: String, root: String): Boolean {
            var t: String? = type
            var depth = 0
            while (t != null && depth++ < 64) {
                if (t == root) return true
                t = parentOf[t]
            }
            return false
        }

        // Hooks every onCreate with the given parameters in classes descending from root.
        // The extension ignores repeated calls, so hooking several levels of a chain is safe.
        fun hook(root: String, parameters: List<String>, call: String): Int {
            var count = 0
            for (type in parentOf.keys.filter { descendsFrom(it, root) }) {
                val hasMethod = classDefBy(type).methods.any {
                    it.name == "onCreate" && it.returnType == "V" &&
                        it.parameterTypes.map(CharSequence::toString) == parameters &&
                        it.implementation != null
                }
                if (!hasMethod) continue

                mutableClassDefBy(type).methods.first {
                    it.name == "onCreate" && it.returnType == "V" &&
                        it.parameterTypes.map(CharSequence::toString) == parameters &&
                        it.implementation != null
                }.addInstruction(0, call)
                count++
            }
            return count
        }

        val applicationHooks = hook(
            "Landroid/app/Application;",
            emptyList(),
            "invoke-static { p0 }, $EXTENSION_CLASS->register(Landroid/app/Application;)V"
        )

        // Activity hook is the fallback when the app's Application class has no onCreate.
        // Reading mode runs in an Activity, so this also covers it directly.
        val activityHooks = hook(
            "Landroid/app/Activity;",
            listOf("Landroid/os/Bundle;"),
            "invoke-static { p0 }, $EXTENSION_CLASS->onActivityCreate(Landroid/app/Activity;)V"
        )

        if (applicationHooks + activityHooks == 0) {
            throw PatchException("No Application or Activity onCreate found")
        }
    }
}
