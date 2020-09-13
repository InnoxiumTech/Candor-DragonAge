package uk.co.innoxium.candor.dragonage

import net.sf.sevenzipjbinding.IInArchive
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import org.apache.commons.io.FileUtils
import uk.co.innoxium.candor.mod.Mod
import uk.co.innoxium.candor.module.AbstractModInstaller
import uk.co.innoxium.candor.module.AbstractModule
import uk.co.innoxium.cybernize.archive.Archive
import uk.co.innoxium.cybernize.archive.ArchiveBuilder
import java.io.File
import java.io.RandomAccessFile
import java.util.function.Consumer


class DAModInstaller(module: AbstractModule?) : AbstractModInstaller(module) {

    override fun canInstall(mod: Mod?): Boolean {

        return true
    }

    override fun install(mod: Mod?): Boolean {

        val randomAccessArchive = ArchiveBuilder(mod?.file).build()
        val outputDir = File(module.modsFolder, getOutputDirForMod(randomAccessArchive))
        val archive = ArchiveBuilder(mod?.file).outputDirectory(outputDir).build()

//        return true
        return archive.extract()
    }

    override fun uninstall(mod: Mod?): Boolean {

        mod!!.associatedFiles!!.forEach {

            val toDelete = File(module.modsFolder, it.asString)
            println("DAI: Deleting file - " + toDelete.absolutePath)
            FileUtils.deleteQuietly(toDelete)
        }
        return true;
    }

    fun getOutputDirForMod(archive: Archive): String {

        var outputDir: String?
        var isFirstDirectory: Boolean? = false
        var firstDirectory: String? = ""

        archive.allArchiveItems.forEach { item ->

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
}