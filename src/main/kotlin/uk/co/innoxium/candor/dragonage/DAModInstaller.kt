package uk.co.innoxium.candor.dragonage

import com.google.gson.JsonArray
import org.apache.commons.io.FileUtils
import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.Node
import org.dom4j.io.OutputFormat
import org.dom4j.io.SAXReader
import org.dom4j.io.XMLWriter
import uk.co.innoxium.candor.mod.Mod
import uk.co.innoxium.candor.module.AbstractModInstaller
import uk.co.innoxium.candor.module.AbstractModule
import uk.co.innoxium.cybernize.archive.Archive
import uk.co.innoxium.cybernize.archive.ArchiveBuilder
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream


class DAModInstaller(module: AbstractModule?) : AbstractModInstaller(module) {

    override fun canInstall(mod: Mod): Boolean {

        return true
    }

    override fun install(mod: Mod): Boolean {

        return if(mod.file?.extension.equals("dazip", ignoreCase = true)) {

            // Things we need to do, merge the manifest with the addins.xml in the documents folder
            // Copy the contents of the "Contents" folder to the "Bioware/DragonAge" folder
            if(!installDAZip(mod)) {

                // If installing fails, restore AddIns.xml from the backed up AddIns.xml.bak
                val modsFolder = module.modsFolder
                FileUtils.copyFile(File(modsFolder, "Settings/AddIns.xml.bak"), File(modsFolder, "Settings/AddIns.xml"))
            }
            true
        } else {

            installStandardMod(mod)
        }
    }

    override fun uninstall(mod: Mod): Boolean {

        // Always remove associated files
        mod.associatedFiles!!.forEach {

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

    // Standard archive mod
    private fun installStandardMod(mod: Mod): Boolean {

        val randomAccessArchive = ArchiveBuilder(mod.file).build()
        val outputDir = File(module.modsFolder, getOutputDirForMod(randomAccessArchive))
        val archive = ArchiveBuilder(mod.file).outputDirectory(outputDir).build()

        return archive.extract()
    }

    // code for handling a .dazip
    private fun installDAZip(mod: Mod): Boolean {

        // Create backup of addins
        val modsFolder = module.modsFolder
        FileUtils.copyFile(File(modsFolder, "Settings/AddIns.xml"), File(modsFolder, "Settings/AddIns.xml.bak"))

        // Copy Files
        copyDaZipFiles(mod)

        // Merge Manifest

        return mergeManifest(mod)
    }

    private fun mergeManifest(mod: Mod): Boolean {

        val saxReader = SAXReader.createDefault()
        saxReader.encoding = StandardCharsets.UTF_8.name()

        val manifestNode = getModManifest(mod, saxReader)
        val addInsElement = getAddInsElement(saxReader)

//        println(manifestNode.asXML())

        writeAddIns(manifestNode, addInsElement)

        return true
    }

    private fun getAddInsElement(saxReader: SAXReader): Element {

        val doc = saxReader.read(File(module.modsFolder, "Settings/AddIns.xml"))
        return doc.rootElement
    }

    private fun getModManifest(mod: Mod, reader: SAXReader): Node {

        val dazip = ZipFile(mod.file)
//        val zipInStream = ZipInputStream(FileInputStream(mod!!.file))
        val manifest = dazip.getEntry("Manifest.xml")
        val manifestInStream = dazip.getInputStream(manifest)

        val ret = reader.read(manifestInStream).selectSingleNode("//AddInsList")

        manifestInStream.close()

        return ret
    }

    private fun writeAddIns(manifest: Node, addIns: Element) {

        addIns.appendContent(manifest as Element)

        val file = File(module.modsFolder, "Settings/AddIns.xml")
        val outWiter = FileWriter(file)
        val format = OutputFormat()
        format.encoding = StandardCharsets.UTF_8.name()
        val xmlWriter = XMLWriter(outWiter, format)
        xmlWriter.write(addIns.document)
        xmlWriter.flush()
        xmlWriter.close()
    }

    private fun copyDaZipFiles(mod: Mod) {

        val tmpFolder = Files.createTempDirectory("candorTemp")
        println(tmpFolder.toAbsolutePath())

        val modArchive = ArchiveBuilder(mod.file).outputDirectory(tmpFolder.toFile()).type(ArchiveBuilder.ArchiveType.SEVEN_ZIP).build()
        modArchive.extract()
        val modContents = File(tmpFolder.toFile(), "Contents")
        val array = JsonArray()
        for(item in modArchive.allArchiveItems) {

            // ignore manifest.xml
            if(!(item.filePath.toLowerCase().endsWith("manifest.xml"))) {

                array.add(item.filePath.replace("Contents\\", ""))
            }
        }
        FileUtils.copyDirectory(modContents, module.modsFolder)
        mod.associatedFiles = array
    }

    // End code for handling .dazip
}