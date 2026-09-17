package mg.example.electeurs

// ---------------------------------------------------------------------------
// Table centrale hiérarchique de Madagascar :
//
//   Région
//     └── District
//           └── Fokontany
//                 └── Bureaux de vote (plusieurs possibles par fokontany)
//
// Cette structure permet d'alimenter les menus déroulants en cascade
// dans le formulaire d'ajout d'un électeur.
// ---------------------------------------------------------------------------

/** Un fokontany avec la liste des bureaux de vote associés. */
data class FokontanyInfo(
    val nom: String,
    val bureauxVote: List<String>,
)

/** Un district avec sa liste de fokontany. */
data class DistrictInfo(
    val nom: String,
    val fokontany: List<FokontanyInfo>,
)

/** Une région avec son code postal et sa liste de districts. */
data class RegionInfo(
    val nom: String,
    val codePostal: String,
    val districts: List<DistrictInfo>,
)

// ---------------------------------------------------------------------------
// Helper interne pour construire rapidement un FokontanyInfo
// ---------------------------------------------------------------------------
private fun fok(nom: String, vararg bureaux: String) =
    FokontanyInfo(nom, bureaux.toList())

// ---------------------------------------------------------------------------
// LA TABLE COMPLÈTE
// (échantillon représentatif — tu peux bien sûr enrichir chaque district)
// ---------------------------------------------------------------------------

val REGIONS_MADAGASCAR: List<RegionInfo> = listOf(

    // ─────────────────────────── ANALAMANGA ───────────────────────────
    RegionInfo(
        nom = "Analamanga",
        codePostal = "101",
        districts = listOf(
            DistrictInfo(
                nom = "Antananarivo Renivohitra",
                fokontany = listOf(
                    fok("Isoraka", "EPP Isoraka", "CEG Isoraka"),
                    fok("Analakely", "EPP Analakely"),
                    fok("Ankadifotsy", "EPP Ankadifotsy", "Lycée Andohalo"),
                    fok("Analamahitsy", "EPP Analamahitsy"),
                ),
            ),
            DistrictInfo(
                nom = "Avaradrano",
                fokontany = listOf(
                    fok("Sabotsy Namehana", "CEG Sabotsy Namehana"),
                    fok("Ambohimanga", "EPP Ambohimanga"),
                ),
            ),
            DistrictInfo(
                nom = "Atsimondrano",
                fokontany = listOf(
                    fok("Ankadikely", "EPP Ankadikely"),
                    fok("Andoharanofotsy", "EPP Andoharanofotsy"),
                ),
            ),
            DistrictInfo(
                nom = "Ambohidratrimo",
                fokontany = listOf(
                    fok("Ivato", "EPP Ivato", "Aéroport Ivato"),
                    fok("Ambohidratrimo Centre", "EPP Ambohidratrimo"),
                ),
            ),
            DistrictInfo(
                nom = "Manjakandriana",
                fokontany = listOf(
                    fok("Ambohibary", "EPP Ambohibary"),
                    fok("Manjakandriana Centre", "EPP Manjakandriana"),
                ),
            ),
        ),
    ),

    // ───────────────────────── VAKINANKARATRA ─────────────────────────
    RegionInfo(
        nom = "Vakinankaratra",
        codePostal = "110",
        districts = listOf(
            DistrictInfo(
                nom = "Antsirabe I",
                fokontany = listOf(
                    fok("Antsenakely", "Lycée Antsirabe"),
                    fok("Mahazoarivo", "EPP Mahazoarivo"),
                ),
            ),
            DistrictInfo(
                nom = "Antsirabe II",
                fokontany = listOf(
                    fok("Betafo", "EPP Betafo"),
                ),
            ),
            DistrictInfo(
                nom = "Betafo",
                fokontany = listOf(
                    fok("Betafo Centre", "EPP Betafo Centre"),
                ),
            ),
            DistrictInfo(
                nom = "Faratsiho",
                fokontany = listOf(
                    fok("Ambohidava", "EPP Ambohidava"),
                ),
            ),
        ),
    ),

    // ───────────────────────────── ITASY ──────────────────────────────
    RegionInfo(
        nom = "Itasy",
        codePostal = "112",
        districts = listOf(
            DistrictInfo(
                nom = "Miarinarivo",
                fokontany = listOf(
                    fok("Andolofotsy", "EPP Andolofotsy"),
                ),
            ),
            DistrictInfo(
                nom = "Arivonimamo",
                fokontany = listOf(
                    fok("Ambaravarana", "EPP Ambaravarana"),
                ),
            ),
            DistrictInfo(
                nom = "Soavinandriana",
                fokontany = listOf(
                    fok("Soavinandriana Centre", "EPP Soavinandriana"),
                ),
            ),
        ),
    ),

    // ─────────────────────────── BONGOLAVA ────────────────────────────
    RegionInfo(
        nom = "Bongolava",
        codePostal = "113",
        districts = listOf(
            DistrictInfo(
                nom = "Tsiroanomandidy",
                fokontany = listOf(
                    fok("Ampefy", "EPP Ampefy"),
                ),
            ),
            DistrictInfo(
                nom = "Fenoarivobe",
                fokontany = listOf(
                    fok("Ambohipandrano", "EPP Ambohipandrano"),
                ),
            ),
        ),
    ),

    // ──────────────────────── HAUTE MATSIATRA ─────────────────────────
    RegionInfo(
        nom = "Haute Matsiatra",
        codePostal = "301",
        districts = listOf(
            DistrictInfo(
                nom = "Fianarantsoa I",
                fokontany = listOf(
                    fok("Tanà Ambony", "EPP Tanà Ambony"),
                    fok("Kianjasoa", "EPP Kianjasoa"),
                ),
            ),
            DistrictInfo(
                nom = "Fianarantsoa II",
                fokontany = listOf(
                    fok("Isandra", "EPP Isandra"),
                ),
            ),
            DistrictInfo(
                nom = "Ambalavao",
                fokontany = listOf(
                    fok("Ambohimandroso", "EPP Ambohimandroso"),
                ),
            ),
        ),
    ),

    // ──────────────────────── AMORON'I MANIA ──────────────────────────
    RegionInfo(
        nom = "Amoron'i Mania",
        codePostal = "306",
        districts = listOf(
            DistrictInfo(
                nom = "Ambositra",
                fokontany = listOf(
                    fok("Tandrokely", "EPP Tandrokely"),
                ),
            ),
            DistrictInfo(
                nom = "Fandriana",
                fokontany = listOf(
                    fok("Sahambavy", "EPP Sahambavy"),
                ),
            ),
            DistrictInfo(
                nom = "Ambatofinandrahana",
                fokontany = listOf(
                    fok("Ambatofinandrahana Centre", "EPP Ambatofinandrahana"),
                ),
            ),
        ),
    ),

    // ──────────────────────────── VATOVAVY ────────────────────────────
    RegionInfo(
        nom = "Vatovavy",
        codePostal = "311",
        districts = listOf(
            DistrictInfo(
                nom = "Manakara",
                fokontany = listOf(
                    fok("Tanambao Nord", "EPP Tanambao Nord"),
                ),
            ),
            DistrictInfo(
                nom = "Mananjary",
                fokontany = listOf(
                    fok("Ambodinondry", "EPP Ambodinondry"),
                ),
            ),
        ),
    ),

    // ─────────────────────────── FITOVINANY ───────────────────────────
    RegionInfo(
        nom = "Fitovinany",
        codePostal = "313",
        districts = listOf(
            DistrictInfo(
                nom = "Vohipeno",
                fokontany = listOf(
                    fok("Ankarongana", "EPP Ankarongana"),
                ),
            ),
            DistrictInfo(
                nom = "Ikongo",
                fokontany = listOf(
                    fok("Ikongo Centre", "EPP Ikongo"),
                ),
            ),
        ),
    ),

    // ──────────────────────────── IHOROMBE ────────────────────────────
    RegionInfo(
        nom = "Ihorombe",
        codePostal = "315",
        districts = listOf(
            DistrictInfo(
                nom = "Ihosy",
                fokontany = listOf(
                    fok("Mahatsinjo", "EPP Mahatsinjo"),
                ),
            ),
            DistrictInfo(
                nom = "Ivohibe",
                fokontany = listOf(
                    fok("Ivohibe Centre", "EPP Ivohibe"),
                ),
            ),
        ),
    ),

    // ─────────────────────── ATSIMO-ATSINANANA ────────────────────────
    RegionInfo(
        nom = "Atsimo-Atsinanana",
        codePostal = "317",
        districts = listOf(
            DistrictInfo(
                nom = "Farafangana",
                fokontany = listOf(
                    fok("Ampasimanjeha", "EPP Ampasimanjeha"),
                ),
            ),
            DistrictInfo(
                nom = "Vangaindrano",
                fokontany = listOf(
                    fok("Vangaindrano Centre", "EPP Vangaindrano"),
                ),
            ),
            DistrictInfo(
                nom = "Midongy",
                fokontany = listOf(
                    fok("Midongy Atsimo", "EPP Midongy Atsimo"),
                ),
            ),
        ),
    ),

    // ─────────────────────────── ATSINANANA ───────────────────────────
    RegionInfo(
        nom = "Atsinanana",
        codePostal = "501",
        districts = listOf(
            DistrictInfo(
                nom = "Toamasina I",
                fokontany = listOf(
                    fok("Tanambao V", "EPP Tanambao V"),
                    fok("Ambolomadinika", "EPP Ambolomadinika"),
                ),
            ),
            DistrictInfo(
                nom = "Toamasina II",
                fokontany = listOf(
                    fok("Mahavelona", "EPP Mahavelona"),
                ),
            ),
            DistrictInfo(
                nom = "Vatomandry",
                fokontany = listOf(
                    fok("Tsarasoa", "EPP Tsarasoa"),
                ),
            ),
            DistrictInfo(
                nom = "Brickaville",
                fokontany = listOf(
                    fok("Brickaville Centre", "EPP Brickaville"),
                ),
            ),
        ),
    ),

    // ────────────────────────── ANALANJIROFO ──────────────────────────
    RegionInfo(
        nom = "Analanjirofo",
        codePostal = "502",
        districts = listOf(
            DistrictInfo(
                nom = "Fénérive Est",
                fokontany = listOf(
                    fok("Tanambao", "EPP Tanambao"),
                ),
            ),
            DistrictInfo(
                nom = "Sainte-Marie",
                fokontany = listOf(
                    fok("Ambodifotatra", "EPP Ambodifotatra"),
                ),
            ),
            DistrictInfo(
                nom = "Soanierana Ivongo",
                fokontany = listOf(
                    fok("Soanierana Centre", "EPP Soanierana"),
                ),
            ),
        ),
    ),

    // ──────────────────────── ALAOTRA-MANGORO ─────────────────────────
    RegionInfo(
        nom = "Alaotra-Mangoro",
        codePostal = "503",
        districts = listOf(
            DistrictInfo(
                nom = "Ambatondrazaka",
                fokontany = listOf(
                    fok("Ambohijanahary", "EPP Ambohijanahary"),
                ),
            ),
            DistrictInfo(
                nom = "Moramanga",
                fokontany = listOf(
                    fok("Ambohibola", "EPP Ambohibola"),
                ),
            ),
            DistrictInfo(
                nom = "Amparafaravola",
                fokontany = listOf(
                    fok("Amparafaravola Centre", "EPP Amparafaravola"),
                ),
            ),
        ),
    ),

    // ───────────────────────────── BOENY ──────────────────────────────
    RegionInfo(
        nom = "Boeny",
        codePostal = "401",
        districts = listOf(
            DistrictInfo(
                nom = "Mahajanga I",
                fokontany = listOf(
                    fok("Mangarivotra", "CEG Mangarivotra"),
                    fok("Mahajanga Be", "EPP Mahajanga Be"),
                ),
            ),
            DistrictInfo(
                nom = "Mahajanga II",
                fokontany = listOf(
                    fok("Mahajanga II Centre", "EPP Mahajanga II"),
                ),
            ),
            DistrictInfo(
                nom = "Mitsinjo",
                fokontany = listOf(
                    fok("Katsepy", "EPP Katsepy"),
                ),
            ),
            DistrictInfo(
                nom = "Marovoay",
                fokontany = listOf(
                    fok("Marovoay Centre", "EPP Marovoay"),
                ),
            ),
        ),
    ),

    // ───────────────────────────── SOFIA ──────────────────────────────
    RegionInfo(
        nom = "Sofia",
        codePostal = "402",
        districts = listOf(
            DistrictInfo(
                nom = "Antsohihy",
                fokontany = listOf(
                    fok("Ankerika", "EPP Ankerika"),
                ),
            ),
            DistrictInfo(
                nom = "Befandriana Avaratra",
                fokontany = listOf(
                    fok("Antetezambato", "EPP Antetezambato"),
                ),
            ),
            DistrictInfo(
                nom = "Bealanana",
                fokontany = listOf(
                    fok("Bealanana Centre", "EPP Bealanana"),
                ),
            ),
        ),
    ),

    // ───────────────────────────── MELAKY ─────────────────────────────
    RegionInfo(
        nom = "Melaky",
        codePostal = "403",
        districts = listOf(
            DistrictInfo(
                nom = "Maintirano",
                fokontany = listOf(
                    fok("Andovoka", "EPP Andovoka"),
                ),
            ),
            DistrictInfo(
                nom = "Morafenobe",
                fokontany = listOf(
                    fok("Morafenobe Centre", "EPP Morafenobe"),
                ),
            ),
        ),
    ),

    // ────────────────────────────── DIANA ─────────────────────────────
    RegionInfo(
        nom = "Diana",
        codePostal = "202",
        districts = listOf(
            DistrictInfo(
                nom = "Antsiranana I",
                fokontany = listOf(
                    fok("Mahajamba", "CEG Mahajamba"),
                    fok("Tanambao Diego", "EPP Tanambao Diego"),
                ),
            ),
            DistrictInfo(
                nom = "Antsiranana II",
                fokontany = listOf(
                    fok("Antsiranana II Centre", "EPP Antsiranana II"),
                ),
            ),
            DistrictInfo(
                nom = "Nosy Be",
                fokontany = listOf(
                    fok("Hell-Ville", "EPP Hell-Ville"),
                    fok("Ambatoloaka", "EPP Ambatoloaka"),
                ),
            ),
            DistrictInfo(
                nom = "Ambanja",
                fokontany = listOf(
                    fok("Ankatafa", "EPP Ankatafa"),
                ),
            ),
        ),
    ),

    // ────────────────────────────── SAVA ──────────────────────────────
    RegionInfo(
        nom = "SAVA",
        codePostal = "201",
        districts = listOf(
            DistrictInfo(
                nom = "Sambava",
                fokontany = listOf(
                    fok("Anosibe", "EPP Anosibe"),
                ),
            ),
            DistrictInfo(
                nom = "Antalaha",
                fokontany = listOf(
                    fok("Ampanefena", "EPP Ampanefena"),
                ),
            ),
            DistrictInfo(
                nom = "Vohemar",
                fokontany = listOf(
                    fok("Antakotaka", "EPP Antakotaka"),
                ),
            ),
            DistrictInfo(
                nom = "Andapa",
                fokontany = listOf(
                    fok("Andapa Centre", "EPP Andapa"),
                ),
            ),
        ),
    ),

    // ───────────────────────────── MENABE ─────────────────────────────
    RegionInfo(
        nom = "Menabe",
        codePostal = "601",
        districts = listOf(
            DistrictInfo(
                nom = "Morondava",
                fokontany = listOf(
                    fok("Ankaboka", "EPP Ankaboka"),
                ),
            ),
            DistrictInfo(
                nom = "Miandrivazo",
                fokontany = listOf(
                    fok("Ambakivao", "EPP Ambakivao"),
                ),
            ),
            DistrictInfo(
                nom = "Belo-sur-Tsiribihina",
                fokontany = listOf(
                    fok("Belo Centre", "EPP Belo"),
                ),
            ),
        ),
    ),

    // ─────────────────────── ATSIMO-ANDREFANA ─────────────────────────
    RegionInfo(
        nom = "Atsimo-Andrefana",
        codePostal = "602",
        districts = listOf(
            DistrictInfo(
                nom = "Toliara I",
                fokontany = listOf(
                    fok("Besakoa", "EPP Besakoa"),
                ),
            ),
            DistrictInfo(
                nom = "Toliara II",
                fokontany = listOf(
                    fok("Toliara II Centre", "EPP Toliara II"),
                ),
            ),
            DistrictInfo(
                nom = "Betioky Sud",
                fokontany = listOf(
                    fok("Behabatsy", "EPP Behabatsy"),
                ),
            ),
            DistrictInfo(
                nom = "Morombe",
                fokontany = listOf(
                    fok("Andranopasy", "EPP Andranopasy"),
                ),
            ),
        ),
    ),

    // ───────────────────────────── ANDROY ─────────────────────────────
    RegionInfo(
        nom = "Androy",
        codePostal = "603",
        districts = listOf(
            DistrictInfo(
                nom = "Ambovombe",
                fokontany = listOf(
                    fok("Ampanihy", "EPP Ampanihy"),
                ),
            ),
            DistrictInfo(
                nom = "Bekily",
                fokontany = listOf(
                    fok("Bekily Centre", "EPP Bekily"),
                ),
            ),
            DistrictInfo(
                nom = "Beloha",
                fokontany = listOf(
                    fok("Beloha Centre", "EPP Beloha"),
                ),
            ),
        ),
    ),

    // ────────────────────────────── ANOSY ─────────────────────────────
    RegionInfo(
        nom = "Anosy",
        codePostal = "604",
        districts = listOf(
            DistrictInfo(
                nom = "Fort-Dauphin",
                fokontany = listOf(
                    fok("Tanambao", "EPP Tanambao"),
                ),
            ),
            DistrictInfo(
                nom = "Amboasary",
                fokontany = listOf(
                    fok("Amboasary Centre", "EPP Amboasary"),
                ),
            ),
            DistrictInfo(
                nom = "Betroka",
                fokontany = listOf(
                    fok("Betroka Centre", "EPP Betroka"),
                ),
            ),
        ),
    ),
)

// ---------------------------------------------------------------------------
// FONCTIONS UTILITAIRES (helpers en cascade)
// ---------------------------------------------------------------------------

/** Liste simple des noms de régions (pour les menus déroulants). */
val NOMS_REGIONS: List<String> = REGIONS_MADAGASCAR.map { it.nom }

/** Retourne les noms de districts d'une région donnée. */
fun districtsDeRegion(region: String): List<String> =
    REGIONS_MADAGASCAR
        .firstOrNull { it.nom.equals(region.trim(), ignoreCase = true) }
        ?.districts
        ?.map { it.nom }
        ?: emptyList()

/** Retourne les noms de fokontany d'un couple (région, district). */
fun fokontanyDeDistrict(region: String, district: String): List<String> =
    REGIONS_MADAGASCAR
        .firstOrNull { it.nom.equals(region.trim(), ignoreCase = true) }
        ?.districts
        ?.firstOrNull { it.nom.equals(district.trim(), ignoreCase = true) }
        ?.fokontany
        ?.map { it.nom }
        ?: emptyList()

/** Retourne les bureaux de vote associés à un fokontany donné. */
fun bureauxDeFokontany(region: String, district: String, fokontany: String): List<String> =
    REGIONS_MADAGASCAR
        .firstOrNull { it.nom.equals(region.trim(), ignoreCase = true) }
        ?.districts
        ?.firstOrNull { it.nom.equals(district.trim(), ignoreCase = true) }
        ?.fokontany
        ?.firstOrNull { it.nom.equals(fokontany.trim(), ignoreCase = true) }
        ?.bureauxVote
        ?: emptyList()