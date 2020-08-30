package uk.co.innoxium.candor.dragonage

import me.shadowchild.candor.module.AbstractModInstaller
import me.shadowchild.candor.module.AbstractModule
import me.shadowchild.candor.module.RunConfig
import java.io.File
import javax.swing.filechooser.FileSystemView

class DAModule : AbstractModule() {

    private var game: File? = null

    override fun getGame(): File {

        return game!!;
    }

    override fun getModsFolder(): File {

        return File(FileSystemView.getFileSystemView().defaultDirectory.path, "Bioware/Dragon Age");
    }

    override fun setGame(file: File?) {

        game = file
    }

    override fun setModsFolder(file: File?) {
    }

    override fun getModuleName(): String {

        return "DragonAgeO"
    }

    override fun getReadableGameName(): String {

        return "Dragon Age: Origins"
    }

    override fun getModInstaller(): AbstractModInstaller {

        return DAModInstaller(this)
    }

    override fun requiresModFolderSelection(): Boolean {

        return false
    }

    override fun acceptedExe(): Array<String> {

        return arrayOf("daorigins")
    }

    override fun getModFileFilterList(): String {

        return "7z"
    }

    override fun getDefaultRunConfig(): RunConfig? {

        return null
    }
}