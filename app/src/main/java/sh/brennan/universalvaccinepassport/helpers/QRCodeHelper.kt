package sh.brennan.universalvaccinepassport.helpers

import sh.brennan.universalvaccinepassport.qrcodegen.QrCode
import sh.brennan.universalvaccinepassport.qrcodegen.QrSegment
import java.util.*
import java.lang.StringBuilder

class QRCodeHelper {
    companion object {
        fun createQRCode(shc: String): String {
            // Mixed Mode??
            // Split at SHC
            val jwtNumeric = shc.replace("shc:/", "")
            val jwtNumericSeg = QrSegment.makeNumeric(jwtNumeric)

            val proto = "shc:/"
            val protoSeg = QrSegment.makeBytes(proto.toByteArray())

            val segs = Arrays.asList(protoSeg, jwtNumericSeg)
            val qr = QrCode.encodeSegments(segs, QrCode.Ecc.LOW);
            return toSvgString(qr, 5, "#FFFFFF", "#000000")
        }

        /**
         * Returns a string of SVG code for an image depicting the specified QR Code, with the specified
         * number of border modules. The string always uses Unix newlines (\n), regardless of the platform.
         * @param qr the QR Code to render (not `null`)
         * @param border the number of border modules to add, which must be non-negative
         * @param lightColor the color to use for light modules, in any format supported by CSS, not `null`
         * @param darkColor the color to use for dark modules, in any format supported by CSS, not `null`
         * @return a string representing the QR Code as an SVG XML document
         * @throws NullPointerException if any object is `null`
         * @throws IllegalArgumentException if the border is negative
         */
        private fun toSvgString(
            qr: QrCode,
            border: Int,
            lightColor: String,
            darkColor: String
        ): String {
            Objects.requireNonNull(qr)
            Objects.requireNonNull(lightColor)
            Objects.requireNonNull(darkColor)
            require(border >= 0) { "Border must be non-negative" }
            val brd = border.toLong()
            val sb = StringBuilder()
                .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n")
                .append(
                    String.format(
                        "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 %1\$d %1\$d\" stroke=\"none\">\n",
                        qr.size + brd * 2
                    )
                )
                .append("\t<rect width=\"100%\" height=\"100%\" fill=\"$lightColor\"/>\n")
                .append("\t<path d=\"")
            for (y in 0 until qr.size) {
                for (x in 0 until qr.size) {
                    if (qr.getModule(x, y)) {
                        if (x != 0 || y != 0) sb.append(" ")
                        sb.append(String.format("M%d,%dh1v1h-1z", x + brd, y + brd))
                    }
                }
            }
            return sb
                .append("\" fill=\"$darkColor\"/>\n")
                .append("</svg>\n")
                .toString()
        }
    }
}