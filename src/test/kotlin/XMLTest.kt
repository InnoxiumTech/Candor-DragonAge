import org.dom4j.Element
import org.dom4j.Node
import org.dom4j.io.OutputFormat
import org.dom4j.io.SAXReader
import org.dom4j.io.XMLWriter
import java.io.File
import java.io.FileWriter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files

fun main(args: Array<String>) {

    val da_AddIns = getAddInsElement()
    val mod_AddIns = getModAddInsElement()

    println(da_AddIns.asXML())

    da_AddIns.appendContent(mod_AddIns as Element)

    da_AddIns.elements().forEach { println(it) }

    val newFile = File(".", "/test/newAddins.xml")
    val out = FileWriter(newFile)
    val format = OutputFormat()
    format.encoding = StandardCharsets.UTF_8.name()
    val xmlWriter = XMLWriter(out, format)
    xmlWriter.write(da_AddIns.document)
    xmlWriter.flush()
    xmlWriter.close()
}

fun getModAddInsElement(): Node {

    val reader = SAXReader.createDefault()
    reader.encoding = StandardCharsets.UTF_8.name()
    val document = reader.read(File(".", "test/Manifest.xml"))
    return document.selectSingleNode("//AddInsList")
}

fun getAddInsElement(): Element {

    val reader = SAXReader.createDefault()
    reader.encoding = "UTF-8"
    val document = reader.read(Files.newBufferedReader(File(".", "test/addIns.xml").toPath(), StandardCharsets.UTF_8))
//    val document = reader.read(File(".", "test/addIns.xml"))
    return document.rootElement // Root is the element to add the manifest into
}