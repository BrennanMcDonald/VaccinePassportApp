package sh.brennan.universalvaccinepassport.helpers

import sh.brennan.universalvaccinepassport.classes.room.Key

class KnownKeys {
    companion object {
        val KnownKeyMap = mapOf(
            // BC
            "Nqa1zvChOkoA46B5ZM_oK3MDhL3-mnLERV_30vkHQIc" to Key(
                "Nqa1zvChOkoA46B5ZM_oK3MDhL3-mnLERV_30vkHQIc",
                "EC",
                "sig",
                "ES256",
                "P-256",
                "XSxuwW_VI_s6lAw6LAlL8N7REGzQd_zXeIVDHP_j_Do",
                "88-aI4WAEl4YmUpew40a9vq_w5OcFvsuaKMxJRLRLL0"
            ),
            // Quebec
            "XCqxdhhS7SWlPqihaUXovM_FjU65WeoBFGc_ppent0Q" to Key(
                "XCqxdhhS7SWlPqihaUXovM_FjU65WeoBFGc_ppent0Q",
                "EC",
                "sig",
                "ES256",
                "P-256",
                "xscSbZemoTx1qFzFo-j9VSnvAXdv9K-3DchzJvNnwrY",
                "jA5uS5bz8R2nxf_TU-0ZmXq6CKWZhAG1Y4icAx8a9CA"
            ),
            // California
            "7JvktUpf1_9NPwdM-70FJT3YdyTiSe2IvmVxxgDSRb0" to Key(
                "7JvktUpf1_9NPwdM-70FJT3YdyTiSe2IvmVxxgDSRb0",
                "EC",
                "sig",
                "ES256",
                "P-256",
                "3dQz5ZlbazChP3U7bdqShfF0fvSXLXD9WMa1kqqH6i4",
                "FV4AsWjc7ZmfhSiHsw2gjnDMKNLwNqi2jMLmJpiKWtE"
            )
        )
    }
}