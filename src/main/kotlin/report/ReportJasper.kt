package report

import DtcEngine
import net.sf.jasperreports.engine.JasperCompileManager
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.JasperReport
import net.sf.jasperreports.engine.export.FileHtmlResourceHandler
import net.sf.jasperreports.engine.export.HtmlExporter
import net.sf.jasperreports.engine.export.JRCsvExporter
import net.sf.jasperreports.engine.export.JRPdfExporter
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
import net.sf.jasperreports.engine.util.JRLoader
import net.sf.jasperreports.export.*
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.sql.DataSource

enum class ReportType { SCREEN, PDF, CSV, HTML, XLSX, XLS }
class ReportJasper (private val dataSource: DataSource, private val template: File) {
    private var reportType = ReportType.PDF
    private var parameters = mutableMapOf<String, Any>()
    private var jasper : JasperReport? = null
    private var randomId : String = DtcEngine.randomCode(12)
    private var templateName = template.name.split(".").first()

    fun setReportType(i: ReportType) = apply { reportType = i }
    fun addParameter(i: Pair<String, Any>) = apply { parameters[i.first] = i.second }
    fun addParameter(i: Map<String,Any>)=  apply { i.forEach { (t, u) -> parameters[t] = u } }

    init {
        jasper = if (template.name.endsWith(".jasper"))
            JRLoader.loadObject(template) as JasperReport
        else
            JasperCompileManager.compileReport(template.inputStream())

    }

    fun build(): ByteArray {
        dataSource.connection.use { connection ->
            val jp: JasperPrint = JasperFillManager.fillReport(jasper, parameters, connection)
            val os = ByteArrayOutputStream()

            val exporter = when (reportType) {
                ReportType.PDF, ReportType.SCREEN -> JRPdfExporter()
                ReportType.XLSX, ReportType.XLS -> JRXlsxExporter()
                ReportType.CSV -> {
                    val cs = SimpleCsvExporterConfiguration()
                    cs.forceFieldEnclosure = true
                    val csvExporter = JRCsvExporter()
                    csvExporter.setConfiguration(cs)
                    csvExporter.exporterOutput = SimpleWriterExporterOutput(os)
                    csvExporter
                }
                ReportType.HTML -> {
                    if (!File(".tmp/$randomId").exists()) {
                        File(".tmp/$randomId").mkdirs()
                    }
                    //reportGenerateId
                    val file = File(".tmp/$randomId/$templateName.html")
                    val htmlExp = HtmlExporter()
                    val configuration = SimpleHtmlExporterConfiguration()
                    htmlExp.setConfiguration(configuration)

                    val exporterOutput = SimpleHtmlExporterOutput(file)
                    val resourcesDir = File(file.parent, file.name.toString() + "_images")
                    val pathPattern = resourcesDir.name + "/{0}"
                    exporterOutput.imageHandler = FileHtmlResourceHandler(resourcesDir, pathPattern)
                    htmlExp.exporterOutput = exporterOutput
                    htmlExp
                }
            }
            exporter.setExporterInput(SimpleExporterInput(jp))

            if (!(reportType == ReportType.CSV || reportType == ReportType.HTML)) {
                exporter.exporterOutput = SimpleOutputStreamExporterOutput(os)
            }
            exporter.exportReport()
            if (reportType == ReportType.HTML) {
                pack(".tmp/$randomId", os)
            }
            os.flush()
            os.close()
            return os.toByteArray()
        }
    }

    @Throws(IOException::class)
    private fun pack(sourceDirPath: String, os: OutputStream): OutputStream {
        ZipOutputStream(os).use { zs ->
            val pp: Path = Paths.get(sourceDirPath)
            Files.walk(pp)
                .filter { path -> !Files.isDirectory(path) }
                .forEach { path ->
                    val zipEntry = ZipEntry(pp.relativize(path).toString())
                    try {
                        zs.putNextEntry(zipEntry)
                        Files.copy(path, zs)
                        zs.closeEntry()
                    } catch (_: IOException) {
                    }
                }
        }
        return os
    }


}