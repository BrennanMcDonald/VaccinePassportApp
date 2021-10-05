package sh.brennan.universalvaccinepassport.analyzers

import sh.brennan.universalvaccinepassport.classes.JWTPayload
import sh.brennan.universalvaccinepassport.classes.JWTHeader
import sh.brennan.universalvaccinepassport.exceptions.WrongFormatException
import android.util.Base64
import com.google.gson.Gson
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.JWK
import sh.brennan.universalvaccinepassport.classes.JWT
import sh.brennan.universalvaccinepassport.classes.room.Key
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

class SHCAnalyzer {

    private fun parsePayload(body: String): JWTPayload {
        val payload = Base64.decode(body, Base64.URL_SAFE);
        val payloadJson = decompress(payload)
        return gson.fromJson(payloadJson, JWTPayload::class.java)
    }

    private fun parseHeader(header: String): JWTHeader {
        val headerString = Base64.decode(header, Base64.DEFAULT);
        val headerJsonString = String(headerString);
        return gson.fromJson(headerJsonString, JWTHeader::class.java)
    }

    // From https://gist.github.com/eren/2924b47db28fa18d021777151751176f
    private fun decompress(content: ByteArray) : String {
        val inflater = Inflater(true) // SHC uses raw zlib data
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)

        inflater.setInput(content)

        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            outputStream.write(buffer, 0, count)
        }

        outputStream.close()

        return outputStream.toString("UTF-8")
    }

    // From https://gist.github.com/eren/2924b47db28fa18d021777151751176f
    private fun compress(input: String) : ByteArray {
        val input = input.toByteArray(charset("UTF-8"))

        // Compress the bytes
        // 1 to 4 bytes/char for UTF-8
        val output = ByteArray(input.size * 4)
        val compressor = Deflater().apply {
            setInput(input)
            finish()
        }
        val compressedDataLength: Int = compressor.deflate(output)
        return output.copyOfRange(0, compressedDataLength)
    }

    // shc:/567629095243206034602924374044603122295953265460346029254077280433602870286471674522280928613331456437653141590640220306450459085643550341424541364037063665417137241236380304375622046737407532323925433443326057360106452931531270742428395038692212766566046938775507593966070420405229673107363333693803425720586229543633123300103877712022113608123739554159750065216576244136280332243607756009665859260023605803272728675721123035212164252561302458076644543210260735435742003171574339537172115043532966334536735428113958605523117307120954595940355931687040114532417303424039651066632209425741412212703267325456534137225708120573042126716936576331122727086139744341560859437500341264706904260465583253732838100829123422126208775858655545243477645920507637325929080373682704763707057100412927585265582550667760036577704021621136444536642771754403736036776160662638060600324033352612760522074054305466502054376441420410096741425206682467446356124533125330537750124227303028400359750054286111584333366267631155530460382155690074592636502724332654657410557730225306356323745403423657422565211128283333346832702663720538744360722833654566035731067308726224373476706465293608106711742940525720295456606873533426066835102443280923746156754126314130556412726961056137220606047644413737522667302267377250590432617356226443096866703304680076745369120674030573312336127454012437565572373107004250255426335362717057650427082543112329402404675520202939260458387410313866226959567029050800693059723470237666045873346268737738504572586731601071064358
    companion object {
        val gson: Gson = Gson();
        var regex: Regex = Regex("(..?)")
        var analyzer: SHCAnalyzer = SHCAnalyzer();


        fun SHCtoJWT(barcode: String): String {
            if (!barcode.contains("shc:/")) {
                throw WrongFormatException("Barcode is not in SHC format");
            }
            try {
                val rawSHC = barcode.split("/")[1];
                return regex
                    .findAll(rawSHC)
                    .map { (it.value.toInt() + 45).toChar() }
                    .joinToString(separator = "")
            } catch(exception: Exception) {
                throw exception
            }
        }

        fun verify(jwt: JWT, key: Key): Boolean {
            return try {
                val jwk = JWK.parse(gson.toJson(key))
                val jws = JWSObject.parse(SHCAnalyzer.SHCtoJWT(jwt.rawSHC))
                val verifier = ECDSAVerifier(jwk.toECKey())
                jws.verify(verifier)
            } catch(ex: Exception) {
                false
            }
        }

        fun analyze(barcode: String): JWT {
            try {
                val jwt = SHCtoJWT(barcode)
                val jwtComponents = jwt.split(".")

                val header = analyzer.parseHeader(jwtComponents[0])
                val payload = analyzer.parsePayload(jwtComponents[1])

                return JWT(header, payload, barcode)
            } catch(exception: WrongFormatException) {
                throw exception
            }
        }
    }
}