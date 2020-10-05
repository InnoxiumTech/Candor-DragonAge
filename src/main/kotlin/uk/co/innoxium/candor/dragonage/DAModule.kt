package uk.co.innoxium.candor.dragonage

import com.github.f4b6a3.uuid.util.UuidConverter
import uk.co.innoxium.candor.Settings
import uk.co.innoxium.candor.game.GamesList
import uk.co.innoxium.candor.module.AbstractModInstaller
import uk.co.innoxium.candor.module.AbstractModule
import uk.co.innoxium.candor.module.RunConfig
import uk.co.innoxium.cybernize.archive.ArchiveItem
import java.io.File
import javax.swing.filechooser.FileSystemView

class DAModule : AbstractModule() {

    private var game: File? = null

    override fun getGame(): File {

        return game!!
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

        return "7z,zip,rar,dazip"
    }

    override fun getDefaultRunConfig(): RunConfig? {

        return object : RunConfig() {

            override fun getStartCommand(): String {

                val game = GamesList.getGameFromUUID(UuidConverter.fromString(Settings.lastGameUuid))

                return game.gameExe
            }

            override fun getProgramArgs(): String {

                return ""
            }

            override fun getWorkingDir(): String? {

                return null
            }
        }
    }

    override fun isCritical(archiveItem: ArchiveItem): Boolean {

        return archiveItem.isDirectory &&
                (archiveItem.filePath.endsWith("override") || archiveItem.filePath.endsWith("core") || archiveItem.filePath.endsWith("packages"))
    }
}