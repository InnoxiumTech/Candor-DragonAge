package uk.co.innoxium.candor.dragonage.dazip

import com.google.gson.JsonArray
import org.apache.commons.io.FileUtils
import org.dom4j.Element
import org.dom4j.Node
import org.dom4j.io.OutputFormat
import org.dom4j.io.SAXReader
import org.dom4j.io.XMLWriter
import uk.co.innoxium.candor.dragonage.DAModule
import uk.co.innoxium.candor.mod.Mod
import uk.co.innoxium.cybernize.archive.ArchiveBuilder
import java.io.File
import java.io.FileWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.zip.ZipFile

class DAZipInstaller(private val module: DAModule) {

    fun installDAZip(mod: Mod): Boolean {

        // Create backup of addins
        val modsFolder = module.modsFolder
        FileUtils.copyFile(File(modsFolder, "Settings/AddIns.xml"), File(modsFolder, "Settings/AddIns.xml.bak"))

        // Copy Files
        copyDaZipFiles(mod)

        // Merge Manifest
        return mergeManifest(mod)
    }

    private fun mergeManifest(mod: Mod): Boolean {

        // The "<AddInsList>" node in the mod Manifest.xml
        val manifestNode = getModManifest(mod)
        // The "<AddInsList>" node in the game AddIns.xml
        val addInsElement = getAddInsElement()

        // Add the content of the mod Manifest, to the game AddIns.xml
        addInsElement.appendContent(manifestNode as Element)

        // Write the changes to disk
        writeAddIns(addInsElement)

        // Return true if no IOException occurred
        return true
    }

    private fun getAddInsElement(): Element {

        // Returns the root element in the AddIns.xml as this is the "<AddInsList>" node
        val doc = getSAXReader().read(File(module.modsFolder, "Settings/AddIns.xml"))
        return doc.rootElement
    }

    private fun getModManifest(mod: Mod): Node {

        // Load the file as a zip in memory
        val dazip = ZipFile(mod.file)
//        val zipInStream = ZipInputStream(FileInputStream(mod!!.file))
        // We only want the manifest currently
        val manifest = dazip.getEntry("Manifest.xml")
        // Create an input stream from the entry
        val manifestInStream = dazip.getInputStream(manifest)

        // Get the information we need from the file
        val ret = getSAXReader().read(manifestInStream).selectSingleNode("//AddInsList")

        // Close the stream and zip file, releasing the memory and file lock
        manifestInStream.close()
        dazip.close()

        return ret
    }

    private fun writeAddIns(addIns: Element) {

        // Define the file we will write to
        val file = File(module.modsFolder, "Settings/AddIns.xml")
        // Create writer and format
        val outWiter = FileWriter(file)
        val format = OutputFormat()
        format.encoding = StandardCharsets.UTF_8.name()
        // Add these to an XML writer
        val xmlWriter = XMLWriter(outWiter, format)
        // Write to the file, flush, and close
        xmlWriter.write(addIns.document)
        xmlWriter.flush()
        xmlWriter.close()
    }

    private fun copyDaZipFiles(mod: Mod) {

        // Create a temp directory
        val tmpFolder = Files.createTempDirectory("candorTemp")
        println(tmpFolder.toAbsolutePath())

        // Create an archive for easier extracting
        val modArchive = ArchiveBuilder(mod.file).outputDirectory(tmpFolder.toFile()).type(ArchiveBuilder.ArchiveType.SEVEN_ZIP).build()
        modArchive.extract()
        // Get the contents of the mod and build a json array of them
        val modContents = File(tmpFolder.toFile(), "Contents")
        val array = JsonArray()
        for(item in modArchive.allArchiveItems) {

            // ignore manifest.xml
            if(!(item.filePath.toLowerCase().endsWith("manifest.xml"))) {

                // Remove the "Contents\" at the start of the string, otherwise the files will never be deleted
                array.add(item.filePath.replace("Contents\\", ""))
            }
        }
        // Now copy the the files to the final destination
        FileUtils.copyDirectory(modContents, module.modsFolder)
        mod.associatedFiles = array
        // Delete the temp folder
        FileUtils.deleteDirectory(tmpFolder.toFile())
    }

    fun removeManifestItems(mod: Mod) {

        // Has the add ins element been changed?
        var addInsDirty = false
        // gets the element from the game "AddIns.xml"
        val addInsElement = getAddInsElement()
        // Gets a list of all "<AddInItem>" nodes
        val addInItems = addInsElement.selectNodes("//AddInItem")

        // Gets a list of all "<AddInItem>" nodes from the mod "Manifest.xml"
        val modElement = getModManifest(mod).selectNodes("//AddInItem")
        // For each node in the "AddIns.xml"
        addInItems.listIterator().forEach { gameAddIn ->

            // Gets the UID prop from the node
            val addInItemUID = gameAddIn.valueOf("@UID")

            // Compare that to all the "<AddInItem>" nodes from the mod "Manifest.xml"
            modElement.listIterator().forEach { modAddIn ->

                // Gets the UID prop from the node
                val modAddInUID = modAddIn.valueOf("@UID")
                if(addInItemUID == modAddInUID) {

                    // If the UID's match, remove the node and mark that it has changed
                    var result = addInsElement.remove(gameAddIn)
                    addInsDirty = true
                }
            }
        }

        // Only if the element has changed, should we write the file
        if(addInsDirty) {

            writeAddIns(addInsElement)
        }
    }

    // Creates a SAXReader instance with the correct encoding format
    private fun getSAXReader(): SAXReader {

        val saxReader = SAXReader.createDefault()
        saxReader.encoding = StandardCharsets.UTF_8.name()
        return saxReader
    }
}