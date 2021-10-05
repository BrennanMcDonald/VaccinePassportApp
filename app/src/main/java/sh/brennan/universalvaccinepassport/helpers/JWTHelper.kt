package sh.brennan.universalvaccinepassport.helpers

import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jose.jwk.JWK
import sh.brennan.universalvaccinepassport.analyzers.SHCAnalyzer
import sh.brennan.universalvaccinepassport.analyzers.SHCAnalyzer.Companion.gson
import sh.brennan.universalvaccinepassport.classes.JWT
import sh.brennan.universalvaccinepassport.classes.atoms.Resource
import sh.brennan.universalvaccinepassport.classes.room.Key

class JWTHelper {
    companion object {
        fun patientInfo(jwt: JWT): List<String> {
            val patients = jwt
                .payload
                .vc
                .credentialSubject
                .fhirBundle
                .entry
                .filter() { entry -> entry.resource.resourceType == "Patient" }
                .map() { it.resource }

            return patients.map() { "${it.name[0].given.joinToString(separator = " ")} ${it.name[0].family} Born ${it.birthDate}" }
        }

        fun patientName(jwt: JWT): List<String> {
            val patients = jwt
                .payload
                .vc
                .credentialSubject
                .fhirBundle
                .entry
                .filter() { entry -> entry.resource.resourceType == "Patient" }
                .map() { it.resource }

            return patients.map() {
                "${it.name[0].given.joinToString(separator = " ")} ${it.name[0].family}"
            }
        }

        fun vaccineCount(jwt: JWT): Int {
            return jwt.payload.vc.credentialSubject.fhirBundle.entry.filter() {
                it.resource.resourceType == "Immunization"
            }.count()
        }
    }
}