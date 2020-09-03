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

        val archive = ArchiveBuilder(mod?.file).outputDirectory(module.modsFolder).build()
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
}