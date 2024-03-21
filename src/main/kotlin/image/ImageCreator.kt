package image

import java.awt.AlphaComposite
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO
import javax.xml.bind.DatatypeConverter

enum class ImageType { PNG, JPG }

class ImageCreator(private val image: ByteArray) {

    private var imageType: ImageType = ImageType.PNG
    private var maxImage: Int = 800

    fun imageType(i: ImageType) = apply { imageType = i }
    fun maxSize(i: Int) = apply { maxImage = i }

    fun compress(): ByteArray {
        if (String(image).trim() == "") {
            throw Exception("failed compress image")
        }
        val bufferedImage = bufferedImage()
        val resizeImage = resizeImage(bufferedImage)
        return Base64.getDecoder().decode(Base64.getEncoder().encode(resizeImage.toByteArray()))
    }

    fun crop(scalaHeight: Int = 1, scalaWidth: Int = 1, scalaResize: Int = 200): ByteArray {
        val bufferedImage = bufferedImage()
        return cropImage(bufferedImage, scalaHeight, scalaWidth, scalaResize)
    }

    fun bufferedImage(): BufferedImage {
        val imageBytes = DatatypeConverter.parseBase64Binary(Base64.getEncoder().encodeToString(image))
        val inputStream = ByteArrayInputStream(imageBytes)
        val img = ImageIO.read(inputStream)
        inputStream.close()
        return img
    }

    private fun drawImage(
        type: String, resizedImage: BufferedImage, bufferedImage: BufferedImage, width: Int,
        height: Int, landscape: Boolean = true, resize: Int = 0, originalResize: Int = 0
    ) {
        val g = resizedImage.createGraphics()
        when (type) {
            "compress" -> g.drawImage(bufferedImage, 0, 0, width, height, Color.WHITE, null)
            else -> {
                if (landscape) g.drawImage(
                    bufferedImage, 0, 0, originalResize * width,
                    resize * height, Color.WHITE, null
                )
                else g.drawImage(
                    bufferedImage, 0, 0, resize * width,
                    originalResize * height, Color.WHITE, null
                )
            }
        }

        g.dispose()
        g.composite = AlphaComposite.Src
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    }

    private fun resizeImage(bufferedImage: BufferedImage): ByteArrayOutputStream {
        val outputStream = ByteArrayOutputStream()
        val resize = bufferedImage.width > maxImage
        val originalWidth = if (resize) maxImage else bufferedImage.width
        val originalHeight = if (resize) {
            val percentage = ((bufferedImage.width - maxImage).toDouble() / bufferedImage.width) * 100
            (bufferedImage.height - (bufferedImage.height * (percentage / 100))).toInt()
        } else bufferedImage.height

        val resizedImage = BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_RGB)
        drawImage("compress", resizedImage, bufferedImage, originalWidth, originalHeight)
        ImageIO.write(resizedImage, imageType.name.lowercase(), outputStream)
        outputStream.flush()
        return outputStream
    }

    private fun cropImage(
        bufferedImage: BufferedImage, scalaHeight: Int, scalaWidth: Int, scalaResize: Int
    ): ByteArray {
        val isLandscape = bufferedImage.width > bufferedImage.height
        val resize = if (isLandscape) scalaResize * scalaHeight else scalaResize * scalaWidth

        val percentage = if (isLandscape) (resize.toDouble() / bufferedImage.height) * 100
        else (resize.toDouble() / bufferedImage.width) * 100

        val originalResize = if (isLandscape) (bufferedImage.width * (percentage / 100)).toInt()
        else (bufferedImage.height * (percentage / 100)).toInt()

        val resizedImage = if (isLandscape) BufferedImage(
            originalResize * scalaWidth,
            resize * scalaHeight,
            BufferedImage.TYPE_INT_RGB
        )
        else BufferedImage(resize * scalaWidth, originalResize * scalaHeight, BufferedImage.TYPE_INT_RGB)
        drawImage("crop", resizedImage, bufferedImage, scalaWidth, scalaHeight, isLandscape, resize, originalResize)
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(resizedImage, "png", outputStream)
        outputStream.flush()
        val y = if (isLandscape) (resizedImage.width / 2) - (resize / 2)
        else (resizedImage.height / 2) - (resize / 2)

        val imgSize = if (isLandscape) if (resize > resizedImage.width) resizedImage.width else resize
        else if (resize > resizedImage.height) resizedImage.height else resize

        val yCrop = if (y < 0) 0 else y

        val bufferedCrop = if (isLandscape) ImageIO.read(ByteArrayInputStream(outputStream.toByteArray()))
            .getSubimage(yCrop, 0, imgSize * scalaWidth, resize * scalaHeight)
        else ImageIO.read(ByteArrayInputStream(outputStream.toByteArray()))
            .getSubimage(0, yCrop, resize * scalaWidth, imgSize * scalaHeight)
        val outputStreamCrop = ByteArrayOutputStream()
        ImageIO.write(bufferedCrop, "png", outputStreamCrop)
        val imageInByte = outputStreamCrop.toByteArray()
        outputStreamCrop.flush()
        return imageInByte
    }

}