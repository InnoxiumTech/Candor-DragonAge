package uk.co.innoxium.candor.dragonage

import org.apache.commons.io.FileUtils
import uk.co.innoxium.candor.mod.Mod
import uk.co.innoxium.candor.module.AbstractModInstaller
import uk.co.innoxium.candor.module.AbstractModule
import uk.co.innoxium.cybernize.archive.Archive
import uk.co.innoxium.cybernize.archive.ArchiveBuilder
import java.io.File


class DAModInstaller(module: AbstractModule?) : AbstractModInstaller(module) {

    override fun canInstall(mod: Mod?): Boolean {

        return true
    }

    override fun install(mod: Mod?): Boolean {

        return if(mod?.file?.extension.equals("dazip", ignoreCase = true)) {

            // Things we need to do, merge the manifest with the addins.xml in the documents folder
            // Copy the contents of the "Contents" folder to the "Bioware/DragonAge" folder
            false
        } else {

            installStandardMod(mod)
        }
    }

    override fun uninstall(mod: Mod?): Boolean {

        mod!!.associatedFiles!!.forEach {

            val toDelete = File(module.modsFolder, it.asString)
            println("DAI: Deleting file - " + toDelete.absolutePath)
            FileUtils.deleteQuietly(toDelete)
        }
        return true;
    }

    private fun getOutputDirForMod(archive: Archive): String {

        val outputDir: String?
        var isFirstDirectory: Boolean? = false
        var firstDirectory: String? = ""

        archive.allArchiveItems.forEach { item ->

            // Determine what the root directory in the archive is, so we can accurately place the mod files in the correct location
            if(item.isDirectory && !isFirstDirectory!! && isRelatedDirectory(item.filePath)) {

                isFirstDirectory = true
                firstDirectory = item.filePath
            }
        }

        // Determine where to extract the mod to
        outputDir = when(firstDirectory) {

            "packages" -> ""
            "core" -> "packages"
            "override" -> "core\\override"
            else -> "packages\\core\\override"
        }

        return outputDir
    }

    private fun isRelatedDirectory(filePath: String): Boolean {

        // Only return true if the base directory, and if it is one of the directories we are able to install to
        return (filePath.endsWith("override") || filePath.endsWith("core") || filePath.endsWith("packages")) &&
                !(filePath.contains("\\") || filePath.contains("/"))
    }

    private fun installStandardMod(mod: Mod?): Boolean {

        val randomAccessArchive = ArchiveBuilder(mod?.file).build()
        val outputDir = File(module.modsFolder, getOutputDirForMod(randomAccessArchive))
        val archive = ArchiveBuilder(mod?.file).outputDirectory(outputDir).build()

        return archive.extract()
    }

    private fun installDAZip(mod: Mod?): Boolean {


    }
}