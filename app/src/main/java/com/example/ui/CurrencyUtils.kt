package com.example.ui

object CurrencyUtils {
    
    // Country flag generator converting country letters (e.g. "AZ") to Emoji Flag
    fun getFlagEmoji(currencyCode: String): String {
        val upperCode = currencyCode.uppercase()
        // Special manual overrides for specific currencies whose first 2 letters do not represent their main country:
        val customCountry = when (upperCode) {
            "EUR" -> "EU"
            "BTC" -> return "🪙"
            "ETH" -> return "🪙"
            "XAF" -> "CF" // Central African CFA franc
            "XOF" -> "SN" // West African CFA franc
            "XPF" -> "PF" // CFP Franc (French Polynesia)
            "XCD" -> "DM" // East Caribbean Dollar
            "ANG" -> "AN" // Netherlands Antilles
            "AED" -> "AE" // United Arab Emirates
            "GEL" -> "GE" // Georgia
            "TRY" -> "TR" // Turkey
            "GBP" -> "GB" // United Kingdom
            "SAR" -> "SA" // Saudi Arabia
            "RUB" -> "RU" // Russia
            else -> {
                if (upperCode.length >= 2) {
                    upperCode.substring(0, 2)
                } else {
                    ""
                }
            }
        }
        
        if (customCountry.length != 2) return "🏳️"
        return try {
            val firstChar = Character.codePointAt(customCountry, 0) - 0x41 + 0x1F1E6
            val secondChar = Character.codePointAt(customCountry, 1) - 0x41 + 0x1F1E6
            String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
        } catch (e: Exception) {
            "🏳️"
        }
    }

    val currencyNamesAz = mapOf(
        "AZN" to "Azərbaycan Manatı",
        "USD" to "ABŞ Dolları",
        "EUR" to "Avro",
        "TRY" to "Türkiyə Lirəsi",
        "RUB" to "Rusiya Rublu",
        "GBP" to "Böyük Britaniya Funt Sterlinqi",
        "CNY" to "Çin Yuanı",
        "JPY" to "Yaponiya Yeni",
        "CHF" to "İsveçrə Frankı",
        "CAD" to "Kanada Dolları",
        "AUD" to "Avstraliya Dolları",
        "GEL" to "Gürcüstan Larisi",
        "AED" to "BƏƏ Dirhəmi",
        "UAH" to "Ukrayna Qrivnası",
        "KZT" to "Qazaxıstan Tengəsi",
        "INR" to "Hindistan Rufisi",
        "SAR" to "Səudiyyə Ərəbistanı Riyalı",
        "IRR" to "İran Rialı",
        "SEK" to "İsveç Kronu",
        "NOK" to "Norveç Kronu",
        "DKK" to "Danimarka Kronu",
        "PLN" to "Polşa Zlotısı",
        "BGN" to "Bolqarıstan Levi",
        "EGP" to "Misir Funtu",
        "ILS" to "İsrail Şekeli",
        "BRL" to "Braziliya Realı",
        "MXN" to "Meksika Pesosu",
        "IDR" to "İndoneziya Rupisi",
        "MYR" to "Malayziya Rinqqiti",
        "NZD" to "Yeni Zelandiya Dolları",
        "PHP" to "Filippin Pesosu",
        "SGD" to "Sinqapur Dolları",
        "THB" to "Tayland Batı",
        "ZAR" to "Cənubi Afrika Randı"
    )

    fun getCurrencyName(code: String): String {
        return currencyNamesAz[code.uppercase()] ?: "Xarici Valyuta"
    }
}
